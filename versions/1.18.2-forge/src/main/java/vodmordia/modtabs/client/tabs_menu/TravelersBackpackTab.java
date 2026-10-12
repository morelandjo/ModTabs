package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Traveler's Backpack tab (1.18.2 Forge, 7.1.x).
 *
 * On 1.18.2 the backpack can only be opened remotely when it is WORN (a Forge capability on the
 * player): the mod's own {@code ServerboundSpecialActionPacket(screenId, OPEN_SCREEN, 0)} makes the
 * server open the worn backpack. There is no packet for opening a backpack that merely sits in the
 * inventory (that only happens by right-clicking it in hand), so the tab is only offered while a
 * backpack is worn.
 */
@TabConfig(configKey = "travelersBackpackTab", defaultEnabled = true, defaultOrder = 0)
public class TravelersBackpackTab extends IntegrationItemTab {
    private static final String CAPABILITY_UTILS_FQN = "com.tiviacz.travelersbackpack.capability.CapabilityUtils";
    private static final String MAIN_CLASS_FQN = "com.tiviacz.travelersbackpack.TravelersBackpack";
    private static final String SPECIAL_ACTION_PACKET_FQN = "com.tiviacz.travelersbackpack.network.ServerboundSpecialActionPacket";

    // Reference.WEARABLE_SCREEN_ID / Reference.OPEN_SCREEN (both bytes) in 7.1.49.
    private static final byte WEARABLE_SCREEN_ID = 2;
    private static final byte OPEN_SCREEN = 4;

    private static final TabSpec SPEC = new TabSpec(
            "travelersBackpackTab",
            ModIntegration.TRAVELERS_BACKPACK,
            () -> Config.Baked.travelersBackpackTabEnabled,
            "travelersBackpack",
            "backpack",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.TRAVELERS_BACKPACK_SCREEN },
            new String[] { ScreenClasses.TRAVELERS_BACKPACK_SCREEN }
    );

    public TravelersBackpackTab() {
        super(SPEC, TravelersBackpackTab::getStandardBackpackItem, Config.Baked.travelersBackpackTabCustomIcon);
    }

    private static ItemStack getStandardBackpackItem() {
        try {
            Class<?> modItemsClass = Class.forName("com.tiviacz.travelersbackpack.init.ModItems");
            Field standardBackpackField = modItemsClass.getDeclaredField("STANDARD_TRAVELERS_BACKPACK");
            standardBackpackField.setAccessible(true);
            Object registryObject = standardBackpackField.get(null);
            Method getMethod = registryObject.getClass().getMethod("get");
            return new ItemStack((Item) getMethod.invoke(registryObject));
        } catch (Exception e) {
            return new ItemStack(Items.LEATHER_CHESTPLATE);
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return super.isEnabled(player) && isWearingBackpackViaCapability(player);
    }

    private boolean isWearingBackpackViaCapability(Player player) {
        try {
            Class<?> capabilityUtilsClass = ClassCache.resolve(CAPABILITY_UTILS_FQN);
            if (capabilityUtilsClass == null) return false;
            Method isWearingBackpack = capabilityUtilsClass.getMethod("isWearingBackpack", Player.class);
            return (Boolean) isWearingBackpack.invoke(null, player);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void initTabOnScreens() {
        // Backpack GUI height grows with the number of rows (3..7), so track the real GUI box.
        ScreenRegistry.builder()
                .withStandardDimensions()
                .withActualGeometry()
                .registerAllTabs(ScreenClasses.TRAVELERS_BACKPACK_SCREEN);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.travelersBackpackTabEnabled || !isWearingBackpackViaCapability(player)) return;
        try {
            Class<?> packetClass = Class.forName(SPECIAL_ACTION_PACKET_FQN);
            // ServerboundSpecialActionPacket(byte screenID, byte typeOfAction, double scrollDelta)
            Constructor<?> ctor = packetClass.getConstructor(byte.class, byte.class, double.class);
            Object packet = ctor.newInstance(WEARABLE_SCREEN_ID, OPEN_SCREEN, 0.0D);

            Class<?> mainClass = Class.forName(MAIN_CLASS_FQN);
            Object channel = mainClass.getField("NETWORK").get(null);
            channel.getClass().getMethod("sendToServer", Object.class).invoke(channel, packet);
        } catch (Exception ignored) {
        }
    }
}
