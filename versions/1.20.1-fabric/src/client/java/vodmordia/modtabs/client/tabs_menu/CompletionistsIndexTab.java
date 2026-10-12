package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.SimpleItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabPositioning;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

/**
 * Tab for Fuzs' Completionist's Index. Mirrors the mod's own inventory button:
 * {@code MinecraftClient.setScreen(new ModsIndexViewScreen(currentScreen))}.
 * Both the top-level mod list and the per-mod item screen count as "current".
 * Note: the mod's own inventory button is not suppressed on Fabric (no mixins in this tree).
 */
@TabConfig(configKey = "completionistsIndexTab", defaultEnabled = true, defaultOrder = 0)
public final class CompletionistsIndexTab extends SimpleItemTab {
    private static final String PKG = "fuzs.completionistsindex.client.gui.screens.inventory.";
    private static final String MODS_SCREEN = PKG + "ModsIndexViewScreen";
    private static final String ITEMS_SCREEN = PKG + "ItemsIndexViewScreen";

    public CompletionistsIndexTab() {
        super(new ItemStack(Items.KNOWLEDGE_BOOK));
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (!player.getWorld().isClient) return;
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            Object screen = Class.forName(MODS_SCREEN).getConstructor(Screen.class).newInstance(mc.currentScreen);
            mc.setScreen((Screen) screen);
        } catch (ReflectiveOperationException | LinkageError e) {
            ModTabs.LOGGER.warn("Failed to open Completionist's Index: " + e);
        }
    }

    @Override public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.completionistsIndexTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.COMPLETIONISTS_INDEX);
    }

    @Override public boolean isCurrentlyUsed(Screen screen) {
        if (screen == null) return false;
        String n = screen.getClass().getName();
        return n.equals(MODS_SCREEN) || n.equals(ITEMS_SCREEN);
    }

    @Override public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.completionists_index.description");
    }

    @Override public void initTabOnScreens() {
        ScreenRegistry.builder().withDimensions(p -> 316, p -> 198).withPositioning(TabPositioning.GUI_RELATIVE)
                .registerAllTabs(MODS_SCREEN, ITEMS_SCREEN);
    }
}
