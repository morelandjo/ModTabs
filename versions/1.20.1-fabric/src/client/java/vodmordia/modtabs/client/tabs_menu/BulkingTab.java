package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
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
 * Bulking replaces Diet's screen with {@code BulkingScreen} (same {@code (boolean fromInventory)}
 * constructor). The Fabric DietTab is a stub, so this opens Bulking's screen directly.
 */
@TabConfig(configKey = "bulkingTab", defaultEnabled = true, defaultOrder = 0)
public class BulkingTab extends SimpleItemTab {
    private static final String SCREEN_CLASS = "dev.neire.mc.bulking.client.gui.BulkingScreen";

    public BulkingTab() {
        super(new ItemStack(Items.APPLE));
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (!player.getWorld().isClient) return;
        try {
            boolean fromInventory = MinecraftClient.getInstance().currentScreen instanceof InventoryScreen;
            Screen screen = (Screen) Class.forName(SCREEN_CLASS)
                    .getConstructor(boolean.class).newInstance(fromInventory);
            MinecraftClient.getInstance().setScreen(screen);
        } catch (ReflectiveOperationException e) {
            ModTabs.LOGGER.warn("Failed to open Bulking screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.bulkingTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.BULKING);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return currentScreen != null && currentScreen.getClass().getName().equals(SCREEN_CLASS);
    }

    @Override
    public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.bulking.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder().withDietDimensions().registerAllTabs(SCREEN_CLASS);
    }
}
