package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.SimpleTextureTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

/**
 * Tab for Cobblemon (Fabric 1.20.1, 1.5.x). Opens the party Summary screen via
 * {@code Summary.Companion.open(slots, true, selectedSlot)} when the player has Pokemon,
 * otherwise asks the server for the starter-selection screen (same as the Forge/NeoForge tab).
 * Everything is reflective; there is no compile dependency on Cobblemon.
 */
@TabConfig(configKey = "cobblemonTab", defaultEnabled = true, defaultOrder = 0)
public final class CobblemonTab extends SimpleTextureTab {
    private static final String CLIENT = "com.cobblemon.mod.common.client.CobblemonClient";
    private static final String SUMMARY = "com.cobblemon.mod.common.client.gui.summary.Summary";
    private static final String STARTER_SCREEN = "com.cobblemon.mod.common.client.gui.startselection.StarterSelectionScreen";
    private static final String NETWORK = "com.cobblemon.mod.common.CobblemonNetwork";
    private static final String NETWORK_PACKET = "com.cobblemon.mod.common.api.net.NetworkPacket";
    private static final String REQUEST_STARTER_PACKET =
            "com.cobblemon.mod.common.net.messages.server.starter.RequestStarterScreenPacket";

    public CobblemonTab() {
        super(new Identifier("cobblemon", "textures/item/poke_balls/poke_ball.png"));
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (!player.getWorld().isClient) return;
        try {
            Object storage = field(Class.forName(CLIENT), "storage", null);
            if (storage == null) return;
            Object party = field(storage.getClass(), "party", storage);
            if (party == null) party = field(storage.getClass(), "myParty", storage);
            if (party == null) return;
            Collection<?> slots = slots(party);
            if (slots == null) return;

            Object sel = field(storage.getClass(), "selectedSlot", storage);
            int selected = sel instanceof Integer i && i >= 0 ? i : 0;

            if (slots.stream().anyMatch(p -> p != null)) {
                Class<?> summary = Class.forName(SUMMARY);
                Object companion = summary.getField("Companion").get(null);
                Method open = companion.getClass().getMethod("open", Collection.class, boolean.class, int.class);
                open.invoke(companion, slots, true, selected);
            } else {
                requestStarterScreen();
            }
        } catch (ReflectiveOperationException | LinkageError e) {
            ModTabs.LOGGER.warn("Failed to open Cobblemon screen: " + e);
        }
    }

    private static Object field(Class<?> type, String name, Object target) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(target);
            } catch (NoSuchFieldException ignored) {
            } catch (ReflectiveOperationException e) {
                return null;
            }
        }
        return null;
    }

    private static Collection<?> slots(Object party) {
        Object value = field(party.getClass(), "slots", party);
        if (value instanceof Collection<?> c) return c;
        if (value instanceof Object[] a) return Arrays.asList(a);
        return null;
    }

    private static void requestStarterScreen() throws ReflectiveOperationException {
        Object packet = Class.forName(REQUEST_STARTER_PACKET).getDeclaredConstructor().newInstance();
        Class<?> network = Class.forName(NETWORK);
        Method send = network.getMethod("sendToServer", Class.forName(NETWORK_PACKET));
        try {
            send.invoke(null, packet);
        } catch (NullPointerException npe) {
            // Kotlin object method without @JvmStatic: call on INSTANCE
            send.invoke(network.getField("INSTANCE").get(null), packet);
        }
    }

    @Override public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.cobblemonTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.COBBLEMON);
    }

    @Override public boolean isCurrentlyUsed(Screen screen) {
        if (screen == null) return false;
        String n = screen.getClass().getName();
        return n.equals(SUMMARY) || n.equals(STARTER_SCREEN);
    }

    @Override public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.cobblemon.description");
    }

    @Override public void initTabOnScreens() {
        ScreenRegistry.builder().withStandardDimensions().inverted().atTop().registerAllTabs(SUMMARY, STARTER_SCREEN);
    }
}
