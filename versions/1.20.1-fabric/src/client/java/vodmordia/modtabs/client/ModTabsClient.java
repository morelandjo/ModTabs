package vodmordia.modtabs.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.client.events.FabricScreenEvents;
import vodmordia.modtabs.client.keybinds.ModKeybinds;
import vodmordia.modtabs.client.tabs_menu.*;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.config.CustomTabDefinition;
import vodmordia.modtabs.utils.CustomTabLoader;

import java.lang.reflect.Method;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ModTabsClient implements ClientModInitializer {

    private static boolean customTabsLoaded = false;

    @Override
    public void onInitializeClient() {
        ModTabs.LOGGER.info("========== ModTabsClient.onInitializeClient() CALLED ==========");

        Config.Baked.bakeClient();

        // Register keybinds
        ModKeybinds.register();

        // Register keybind handler for tab cycling
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ModKeybinds.TAB_CYCLE.wasPressed()) {
                if (client.currentScreen != null) {
                    TabsMenu.cycleToNextTab(client.currentScreen);
                }
            }
        });

        // Register Fabric screen events using global registration
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            FabricScreenEvents.onScreenInit(screen);
        });

        // Register all tabs - each tab's isEnabled() method handles mod detection via @TabConfig
        TabsMenu.register(new InventoryTab());
        TabsMenu.register(new BackpackedTab());
        TabsMenu.register(new TravelersBackpackTab());
        TabsMenu.register(new BodyDamageTab());

        // Run inspection for FTB Quests if needed
        try {
            Class<?> inspectorClass = Class.forName("vodmordia.modtabs.utils.FTBQuestsInspector");
            Method inspectMethod = inspectorClass.getMethod("inspect");
            inspectMethod.invoke(null);
        } catch (Exception e) {
            ModTabs.LOGGER.warn("Failed to run FTB Quests inspection: " + e.getMessage());
        }
        TabsMenu.register(new FtbQuestsTab());

        // Run inspection for FTB Teams if needed
        try {
            Class<?> inspectorClass = Class.forName("vodmordia.modtabs.utils.FTBTeamsInspector");
            Method inspectMethod = inspectorClass.getMethod("inspectModClasses");
            inspectMethod.invoke(null);
        } catch (Exception e) {
            ModTabs.LOGGER.warn("Failed to run FTB Teams inspection: " + e.getMessage());
        }
        TabsMenu.register(new FtbTeamsTab());

        TabsMenu.register(new ReskillableReimaginedTab());
        TabsMenu.register(new MapAtlasesTab());
        TabsMenu.register(new XaerosMapTab());
        TabsMenu.register(new JourneyMapTab());
        TabsMenu.register(new DietTab());
        TabsMenu.register(new PassiveSkillTreeTab());
        TabsMenu.register(new PufferfishsSkillsTab());
        TabsMenu.register(new L2HostilityDifficultyTab());
        TabsMenu.register(new L2AttributeTab());
        TabsMenu.register(new L2ArtifactsTab());
        TabsMenu.register(new SophisticatedBackpacksTab());
        TabsMenu.register(new CosmeticArmorTab());
        TabsMenu.register(new CobblemonTab());
        TabsMenu.register(new DraconicEvolutionTab());
        TabsMenu.register(new ModularGolemsTab());

        // Run inspection for Ars Elixirum if needed
        try {
            Class<?> inspectorClass = Class.forName("vodmordia.modtabs.utils.ArsElixirumInspector");
            Method inspectMethod = inspectorClass.getMethod("inspectModClasses");
            inspectMethod.invoke(null);
        } catch (Exception e) {
            ModTabs.LOGGER.warn("Failed to run Ars Elixirum inspection: " + e.getMessage());
        }
        TabsMenu.register(new ArsElixirumTab());

        TabsMenu.register(new ArsNouveauTab());
        TabsMenu.register(new AdvancementsTab());
        TabsMenu.register(new OriginsTab());

        // Yttr: screen-only support (tab bar shown on its inventory-style handled screens).
        registerYttrScreens();
        registerItemCollectorsScreens();

        // Wait for Patchouli books to load, then load custom tabs
        waitForPatchouliAndLoadCustomTabs();

        // Finalize all pending screen registrations now that all tabs are registered
        TabsMenu.finalizePendingRegistrations();

    }

    /**
     * Item Collectors: its GUIs are generic SuperMartijn642 corelib wrapper screens, so the wrapper
     * classes are registered with a predicate that only passes when the wrapped widget belongs to
     * itemcollectors. The layout is keyed by wrapper class (shared with other corelib screens' key,
     * which never show tabs).
     */
    private static void registerItemCollectorsScreens() {
        if (!vodmordia.modtabs.integration.ModIntegrationManager.isModLoaded(vodmordia.modtabs.integration.ModIntegration.ITEM_COLLECTORS)) return;
        java.util.function.Predicate<net.minecraft.client.gui.screen.Screen> isIc = screen -> {
            try {
                Object w = screen.getClass().getMethod("getWidget").invoke(screen);
                return w != null && w.getClass().getName().startsWith("com.supermartijn642.itemcollectors.");
            } catch (ReflectiveOperationException e) {
                return false;
            }
        };
        vodmordia.modtabs.api.tabs_menu.ScreenRegistry.builder().withDimensions(p -> 202, p -> 82).withPredicate(isIc)
                .registerAllTabs("com.supermartijn642.core.gui.WidgetScreen");
        vodmordia.modtabs.api.tabs_menu.ScreenRegistry.builder().withDimensions(p -> 224, p -> 206).withPredicate(isIc)
                .registerAllTabs("com.supermartijn642.core.gui.WidgetContainerScreen");
    }

    /** Yttr (fabric): tab bar on its handled (inventory-style) screens; classes resolved by name, no compile dependency. */
    private static void registerYttrScreens() {
        String pkg = "diy.y2k.yttr.client.screen.handled.";
        Object[][] screens = {
                {"AmmoPackScreen", 176, 163}, {"BoxeScreen", 176, 148}, {"CanFillerScreen", 176, 136},
                {"CentrifugeScreen", 176, 201}, {"DSUScreen", 176, 186}, {"IndexScreen", 176, 222},
                {"MagtankScreen", 176, 194}, {"RafterScreen", 201, 194}, {"SSDScreen", 176, 117},
                {"SuitStationScreen", 199, 175}, {"VoidFilterScreen", 176, 166}
        };
        for (Object[] e : screens) {
            int w = (Integer) e[1];
            int h = (Integer) e[2];
            vodmordia.modtabs.api.tabs_menu.ScreenRegistry.builder()
                    .withDimensions(p -> w, p -> h)
                    .registerAllTabs(pkg + e[0]);
        }
    }

    /**
     * Wait for Patchouli books to be loaded, then load custom tabs
     */
    private static void waitForPatchouliAndLoadCustomTabs() {
        // Run in a separate thread to avoid blocking the mod loading
        new Thread(() -> {
            try {

                // Wait for Patchouli books to be loaded using their synchronization mechanism
                waitForPatchouliBooksLoaded();


                customTabsLoaded = true;
                loadCustomTabs();

            } catch (Exception e) {
                ModTabs.LOGGER.error("Error waiting for Patchouli books: " + e.getMessage());
                if (Config.Baked.customTabsDebugLogging) {
                    e.printStackTrace();
                }
            }
        }, "PatchouliCustomTabsLoader").start();
    }

    /**
     * Wait for Patchouli books to be loaded using reflection to access their synchronization mechanism
     */
    private static void waitForPatchouliBooksLoaded() {
        try {
            // Check if Patchouli is loaded first
            if (!FabricLoader.getInstance().isModLoaded("patchouli")) {
                return;
            }

            // Try to access Patchouli's Fabric client initializer for synchronization
            // This may need to be updated based on Patchouli's Fabric implementation
            try {
                Class<?> clientInitClass = Class.forName("vazkii.patchouli.fabric.client.FabricClientInitializer");
                // Similar synchronization mechanism as before, but adapted for Fabric
                // The exact implementation will depend on how Patchouli handles book loading in Fabric

                // For now, we'll use a simple wait approach
                Thread.sleep(3000); // Wait 3 seconds for books to load

            } catch (ClassNotFoundException e) {
                Thread.sleep(3000);
            }

        } catch (Exception e) {
            ModTabs.LOGGER.warn("Failed to wait for Patchouli books: " + e.getMessage());
            if (Config.Baked.customTabsDebugLogging) {
                e.printStackTrace();
            }
            // Fallback: wait a short time
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Public method to manually trigger custom tabs loading (for debugging)
     */
    public static void forceLoadCustomTabs() {
        if (!customTabsLoaded) {
            customTabsLoaded = true;
            loadCustomTabs();
        } else {
        }
    }

    /**
     * Load and register custom tabs from JSON configuration files
     */
    private static void loadCustomTabs() {

        if (!Config.Baked.customTabsEnabled) {
            return;
        }

        try {

            List<CustomTabDefinition> customTabDefinitions = CustomTabLoader.loadCustomTabs();


            // TabsMenu state is not thread-safe: register + finalize on the client thread.
            // File loading above stays off-thread.
            net.minecraft.client.MinecraftClient.getInstance().execute(() -> {
            try {
            for (CustomTabDefinition definition : customTabDefinitions) {
                try {
                    CustomJsonTab customTab = new CustomJsonTab(definition);
                    TabsMenu.register(customTab);
                } catch (Exception e) {
                    ModTabs.LOGGER.error("Failed to register custom tab " + definition.tabId + ": " + e.getMessage());
                    if (Config.Baked.customTabsDebugLogging) {
                        e.printStackTrace();
                    }
                }
            }


            // Re-finalize registrations to ensure custom tabs are properly integrated
            TabsMenu.finalizePendingRegistrations();

            // Refresh the current screen to show newly loaded custom tabs
            refreshCurrentScreenForNewTabs();
            } catch (Exception e) {
                ModTabs.LOGGER.error("Error registering custom tabs: " + e.getMessage());
            }
            });

        } catch (Exception e) {
            ModTabs.LOGGER.error("Error loading custom tabs: " + e.getMessage());
            if (Config.Baked.customTabsDebugLogging) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Refresh the current screen to show newly loaded custom tabs
     */
    private static void refreshCurrentScreenForNewTabs() {
        try {
            net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
            if (minecraft.currentScreen != null) {
                // Get the current screen
                net.minecraft.client.gui.screen.Screen currentScreen = minecraft.currentScreen;

                // Check if this screen supports tabs
                if (TabsMenu.hasTabsForScreen(currentScreen)) {

                    // Force a refresh by closing and reopening the screen
                    // This will trigger the tab building process again
                    minecraft.setScreen(null);
                    minecraft.setScreen(currentScreen);
                }
            }
        } catch (Exception e) {
            ModTabs.LOGGER.warn("Failed to refresh current screen for new custom tabs: " + e.getMessage());
            if (Config.Baked.customTabsDebugLogging) {
                e.printStackTrace();
            }
        }
    }
}
