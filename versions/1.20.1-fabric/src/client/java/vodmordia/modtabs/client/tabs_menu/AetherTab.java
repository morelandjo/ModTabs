package vodmordia.modtabs.client.tabs_menu;

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

import java.lang.reflect.Method;

/**
 * Tab for The Aether's accessory inventory (Fabric 1.20.1 port, mod id {@code aether}).
 * Same flow as pressing the mod's own key: send {@code OpenAccessoriesPacket(ItemStack.EMPTY)}
 * to the server through Aether's {@code AetherPacketHandler.INSTANCE} (a Simple Networking
 * channel) using Nitrogen's {@code PacketRelay.sendToServer}; the server opens the
 * {@code AccessoriesMenu}. Everything is resolved reflectively (no compile dependency).
 */
@TabConfig(configKey = "aetherTab", defaultEnabled = true, defaultOrder = 0)
public final class AetherTab extends SimpleItemTab {
    private static final String ACCESSORIES_SCREEN = "com.aetherteam.aether.client.gui.screen.inventory.AccessoriesScreen";
    private static final String OPEN_ACCESSORIES_PACKET = "com.aetherteam.aether.network.packet.serverbound.OpenAccessoriesPacket";
    private static final String PACKET_HANDLER = "com.aetherteam.aether.network.AetherPacketHandler";
    private static final String PACKET_RELAY = "com.aetherteam.nitrogen.network.PacketRelay";

    public AetherTab() {
        super(AetherTab::getRingIcon);
    }

    private static ItemStack getRingIcon() {
        var item = Registries.ITEM.get(new Identifier("aether", "iron_ring"));
        return item != Items.AIR ? new ItemStack(item) : new ItemStack(Items.GOLD_INGOT);
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (!player.getWorld().isClient) return;
        try {
            Class<?> packetClass = Class.forName(OPEN_ACCESSORIES_PACKET);
            Object packet = packetClass.getConstructor(ItemStack.class).newInstance(ItemStack.EMPTY);
            Object channel = Class.forName(PACKET_HANDLER).getField("INSTANCE").get(null);
            Class<?> relay = Class.forName(PACKET_RELAY);
            for (Method m : relay.getMethods()) {
                if (m.getName().equals("sendToServer") && m.getParameterCount() == 2) {
                    m.invoke(null, channel, packet);
                    return;
                }
            }
            ModTabs.LOGGER.warn("Aether PacketRelay.sendToServer not found");
        } catch (ReflectiveOperationException | LinkageError e) {
            ModTabs.LOGGER.warn("Failed to open Aether accessories: " + e);
        }
    }

    @Override public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.aetherTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.AETHER);
    }

    @Override public boolean isCurrentlyUsed(Screen screen) {
        return screen != null && screen.getClass().getName().equals(ACCESSORIES_SCREEN);
    }

    @Override public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.aether.description");
    }

    @Override public void initTabOnScreens() {
        ScreenRegistry.builder().withStandardDimensions().withPositioning(TabPositioning.GUI_RELATIVE)
                .withActualGeometry().registerAllTabs(ACCESSORIES_SCREEN);
    }
}
