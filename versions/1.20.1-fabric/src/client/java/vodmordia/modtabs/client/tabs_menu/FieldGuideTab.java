package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.SimpleItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabPositioning;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

/**
 * Tab for evanbones' Field Guide. Mirrors the mod's inventory-button behaviour: reopen
 * {@code BookScreen.lastOpenedScreen} when {@code ClientConfig.defaultScreen} is
 * {@code "last_opened_screen"}, otherwise open a fresh {@code FieldGuideCategoryScreen()}.
 * Note: the mod's own inventory button is not suppressed on Fabric (no mixins in this tree).
 */
@TabConfig(configKey = "fieldGuideTab", defaultEnabled = true, defaultOrder = 0)
public final class FieldGuideTab extends SimpleItemTab {
    private static final String PKG = "com.evandev.fieldguide.client.gui.screens.";
    private static final String CATEGORY_SCREEN = PKG + "FieldGuideCategoryScreen";
    private static final String ENTRY_SCREEN = PKG + "FieldGuideEntryScreen";
    private static final String JOURNAL_SCREEN = PKG + "FieldGuideJournalScreen";
    private static final String BOOK_SCREEN = PKG + "BookScreen";
    private static final String CLIENT_CONFIG = "com.evandev.fieldguide.config.ClientConfig";

    public FieldGuideTab() {
        super(FieldGuideTab::getBookIcon);
    }

    private static ItemStack getBookIcon() {
        var item = Registries.ITEM.get(new Identifier("fieldguide", "field_guide"));
        return item != Items.AIR ? new ItemStack(item) : new ItemStack(Items.KNOWLEDGE_BOOK);
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (!player.getWorld().isClient) return;
        try {
            Screen target = resolveLastOpenedScreen();
            if (target == null) {
                target = (Screen) Class.forName(CATEGORY_SCREEN).getConstructor().newInstance();
            }
            MinecraftClient.getInstance().setScreen(target);
        } catch (ReflectiveOperationException | LinkageError e) {
            ModTabs.LOGGER.warn("Failed to open Field Guide: " + e);
        }
    }

    private static Screen resolveLastOpenedScreen() {
        try {
            Object cfg = Class.forName(CLIENT_CONFIG).getMethod("get").invoke(null);
            if (cfg == null) return null;
            Object mode = cfg.getClass().getField("defaultScreen").get(cfg);
            if (!"last_opened_screen".equals(mode)) return null;
            Object last = Class.forName(BOOK_SCREEN).getField("lastOpenedScreen").get(null);
            if (last instanceof Screen s) return s;
        } catch (ReflectiveOperationException | LinkageError ignored) {
        }
        return null;
    }

    @Override public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.fieldGuideTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.FIELD_GUIDE);
    }

    @Override public boolean isCurrentlyUsed(Screen screen) {
        if (screen == null) return false;
        String n = screen.getClass().getName();
        return n.equals(CATEGORY_SCREEN) || n.equals(ENTRY_SCREEN) || n.equals(JOURNAL_SCREEN);
    }

    @Override public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.field_guide.description");
    }

    @Override public void initTabOnScreens() {
        ScreenRegistry.builder().withDimensions(p -> 300, p -> 200).withPositioning(TabPositioning.GUI_RELATIVE)
                .registerAllTabs(CATEGORY_SCREEN, ENTRY_SCREEN, JOURNAL_SCREEN);
    }
}
