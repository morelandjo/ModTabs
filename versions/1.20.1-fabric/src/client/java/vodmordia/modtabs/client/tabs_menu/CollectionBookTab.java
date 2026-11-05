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

/**
 * Tab for Skatric's Item Collector (modid {@code itemcollector}, NOT Serilum's "Item Collectors").
 * Mirrors the mod's own open path (keybind B / {@code /collection}):
 * {@code MinecraftClient.setScreen(new CollectionBookScreen())}.
 */
@TabConfig(configKey = "collectionBookTab", defaultEnabled = true, defaultOrder = 0)
public final class CollectionBookTab extends SimpleItemTab {
    private static final String SCREEN = "skatric.itemcollector.client.CollectionBookScreen";

    public CollectionBookTab() {
        super(new ItemStack(Items.WRITTEN_BOOK));
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (!player.getWorld().isClient) return;
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            Object screen = Class.forName(SCREEN).getConstructor().newInstance();
            mc.setScreen((Screen) screen);
        } catch (ReflectiveOperationException | LinkageError e) {
            ModTabs.LOGGER.warn("Failed to open Item Collector's Collection Book: " + e);
        }
    }

    @Override public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.collectionBookTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.ITEM_COLLECTOR);
    }

    @Override public boolean isCurrentlyUsed(Screen screen) {
        if (screen == null) return false;
        String n = screen.getClass().getName();
        return n.equals(SCREEN);
    }

    @Override public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.collection_book.description");
    }

    @Override public void initTabOnScreens() {
        ScreenRegistry.builder().withStandardDimensions().inverted().atTop().registerAllTabs(SCREEN);
    }
}
