package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;

import java.lang.reflect.Method;

/** Opens the equipped Modular Backpacks backpack by sending its open-toggle packet to the server. */
@TabConfig(configKey = "modularBackpacksTab", defaultEnabled = true, defaultOrder = 0)
public final class ModularBackpacksTab extends IntegrationItemTab {
    private static final String SCREEN = "it.mralxart.modular_backpacks.client.gui.BackpackScreen";
    private static final String HELPER = "it.mralxart.modular_backpacks.compat.EquippedBackpackHelper";
    private static final String NETWORKING = "it.mralxart.modular_backpacks.network.Networking";
    private static final String TOGGLE_PACKET = "it.mralxart.modular_backpacks.network.packets.ToggleBackpackPacket";

    private static final TabSpec SPEC = new TabSpec(
            "modularBackpacksTab", ModIntegration.MODULAR_BACKPACKS, () -> ModTabsConfig.modularBackpacksTabEnabled,
            "modularBackpacks", "modular_backpacks", TabSpec.Layout.guiRelative(),
            new String[]{SCREEN}, new String[]{SCREEN});

    public ModularBackpacksTab() {
        super(SPEC, ModularBackpacksTab::icon, ModTabsConfig.modularBackpacksTabCustomIcon);
    }

    private static ItemStack icon() {
        Item item = BuiltInRegistries.ITEM.get(new ResourceLocation("modular_backpacks", "backpack"));
        return item != null && item != Items.AIR ? new ItemStack(item) : new ItemStack(Items.LEATHER_CHESTPLATE);
    }

    @Override
    public boolean isEnabled(Player player) {
        return super.isEnabled(player) && hasBackpack(player);
    }

    private static boolean hasBackpack(Player player) {
        try {
            Class<?> helper = ClassCache.resolve(HELPER);
            return helper != null && Boolean.TRUE.equals(helper.getMethod("has", Player.class).invoke(null, player));
        } catch (ReflectiveOperationException | LinkageError e) {
            return false;
        }
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            Class<?> packetType = ClassCache.resolve(TOGGLE_PACKET);
            Class<?> networking = ClassCache.resolve(NETWORKING);
            if (packetType == null || networking == null) return;
            Object packet = packetType.getConstructor(boolean.class).newInstance(true);
            for (Method m : networking.getMethods()) {
                if (m.getName().equals("sendToServer") && m.getParameterCount() == 1
                        && m.getParameterTypes()[0].isInstance(packet)) {
                    m.invoke(null, packet);
                    return;
                }
            }
            ModTabs.LOGGER.warn("[Modular Backpacks] no compatible sendToServer found");
        } catch (ReflectiveOperationException | LinkageError e) {
            ModTabs.LOGGER.warn("Failed to open Modular Backpacks screen", e);
        }
    }
}
