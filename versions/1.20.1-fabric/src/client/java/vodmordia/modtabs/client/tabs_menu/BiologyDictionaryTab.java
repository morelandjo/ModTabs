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
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.lang.reflect.Method;

/**
 * Biology Dictionary. Mirrors the mod's own keybind permission rule: shown in creative, when the
 * server's {@code bookItemRequired} is off, or when the dictionary book is in the inventory.
 * Detection and opening go through the mod's static helpers so its gating is honored.
 */
@TabConfig(configKey = "biologyDictionaryTab", defaultEnabled = true, defaultOrder = 0)
public class BiologyDictionaryTab extends SimpleItemTab {
    private static final String PKG = "io.github.xienaoban.biologydictionary.";
    private static final String ITEM_CLASS = PKG + "core.BiologyDictionaryItem";
    private static final String EVENT_CLASS = PKG + "client.BiologyDictionaryEvent";
    private static final String CONFIGS_MANAGER = PKG + "config.ConfigsManager";
    private static final String[] SCREENS = {
            PKG + "gui.screen.BdHomeScreen",
            PKG + "gui.screen.BdAboutScreen",
            PKG + "gui.screen.BdConfigScreen",
            PKG + "gui.screen.BdEntityOverviewScreen",
            PKG + "gui.screen.BdEntityDetailScreen"
    };

    public BiologyDictionaryTab() {
        super(() -> getBookIcon());
    }

    private static ItemStack getBookIcon() {
        try {
            Object stack = Class.forName(ITEM_CLASS).getMethod("createBook").invoke(null);
            if (stack instanceof ItemStack s) return s;
        } catch (Exception ignored) {}
        return new ItemStack(Items.WRITABLE_BOOK);
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (!player.getWorld().isClient) return;
        try {
            Class.forName(EVENT_CLASS).getMethod("openBookScreen", MinecraftClient.class)
                    .invoke(null, MinecraftClient.getInstance());
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Biology Dictionary screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.biologyDictionaryTabEnabled
                && ModIntegrationManager.isModLoaded(ModIntegration.BIOLOGY_DICTIONARY)
                && canOpenBook(player);
    }

    private static boolean canOpenBook(PlayerEntity player) {
        if (player.getAbilities().creativeMode) return true;
        if (!isBookItemRequired()) return true;
        return hasBook(player);
    }

    /** {@code ConfigsManager.getServer().isBookItemRequired()}; defaults to true (require book) on failure. */
    private static boolean isBookItemRequired() {
        try {
            Object server = Class.forName(CONFIGS_MANAGER).getMethod("getServer").invoke(null);
            if (server == null) return true;
            Object result = server.getClass().getMethod("isBookItemRequired").invoke(server);
            return !(result instanceof Boolean b) || b;
        } catch (Exception e) {
            return true;
        }
    }

    private static boolean hasBook(PlayerEntity player) {
        Method isBook;
        try {
            isBook = Class.forName(ITEM_CLASS).getMethod("isBook", ItemStack.class);
        } catch (ReflectiveOperationException e) {
            return false;
        }
        for (ItemStack stack : player.getInventory().main) {
            if (!stack.isEmpty() && isBookStack(isBook, stack)) return true;
        }
        ItemStack offhand = player.getOffHandStack();
        return !offhand.isEmpty() && isBookStack(isBook, offhand);
    }

    private static boolean isBookStack(Method isBook, ItemStack stack) {
        try {
            return Boolean.TRUE.equals(isBook.invoke(null, stack));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;
        String name = currentScreen.getClass().getName();
        for (String s : SCREENS) if (s.equals(name)) return true;
        return false;
    }

    @Override
    public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.biology_dictionary.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder().withStandardDimensions().inverted().atTop().registerAllTabs(SCREENS);
    }
}
