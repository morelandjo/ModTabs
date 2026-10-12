package vodmordia.modtabs.client.tabs_menu;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.SimpleItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabPositioning;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;
import vodmordia.modtabs.network.OpenReliableBackpackHandler;

/**
 * Reliable Backpacks: visible while the player carries a backpack; a click asks the server
 * (see {@link OpenReliableBackpackHandler}) to open it, since the mod has no open-from-self path.
 */
@TabConfig(configKey = "reliableBackpacksTab", defaultEnabled = true, defaultOrder = 0)
public class ReliableBackpackTab extends SimpleItemTab {
    private static final String ITEM_CLASS = "com.evandev.reliable_backpacks.common.items.BackpackItem";
    private static final String SCREEN_CLASS = "com.evandev.reliable_backpacks.client.gui.BackpackScreen";

    public ReliableBackpackTab() {
        super(() -> getBackpackIcon());
    }

    private static ItemStack getBackpackIcon() {
        Item item = Registries.ITEM.get(new Identifier("reliable_backpacks", "backpack"));
        return item != Items.AIR ? new ItemStack(item) : new ItemStack(Items.LEATHER);
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (!player.getWorld().isClient) return;
        ClientPlayNetworking.send(OpenReliableBackpackHandler.CHANNEL, PacketByteBufs.create());
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.reliableBackpacksTabEnabled
                && ModIntegrationManager.isModLoaded(ModIntegration.RELIABLE_BACKPACKS)
                && hasBackpack(player);
    }

    private static boolean hasBackpack(PlayerEntity player) {
        Class<?> itemClass;
        try {
            itemClass = Class.forName(ITEM_CLASS);
        } catch (ClassNotFoundException e) {
            return false;
        }
        for (ItemStack stack : player.getInventory().main) {
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) return true;
        }
        ItemStack offhand = player.getOffHandStack();
        return !offhand.isEmpty() && itemClass.isInstance(offhand.getItem());
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return currentScreen != null && currentScreen.getClass().getName().equals(SCREEN_CLASS);
    }

    @Override
    public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.reliable_backpacks.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder()
                .withStandardDimensions()
                .withPositioning(TabPositioning.GUI_RELATIVE)
                .withActualGeometry() // row count varies with config
                .registerAllTabs(SCREEN_CLASS);
    }
}
