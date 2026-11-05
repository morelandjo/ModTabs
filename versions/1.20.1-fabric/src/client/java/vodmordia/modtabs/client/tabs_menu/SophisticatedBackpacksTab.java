package vodmordia.modtabs.client.tabs_menu;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.api.tabs_menu.SimpleItemTab;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SophisticatedBackpacksTab extends SimpleItemTab {

    public SophisticatedBackpacksTab() {
        super(() -> getBackpackItem());
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        try {
            // Create BackpackOpenMessage
            Class<?> messageClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.network.BackpackOpenMessage");
            Object message = messageClass.getDeclaredConstructor().newInstance();

            // Use SBPPacketHandler.sendToServer()
            Class<?> packetHandlerClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.network.SBPPacketHandler");
            java.lang.reflect.Method sendToServerMethod = packetHandlerClass.getMethod("sendToServer", Object.class);
            sendToServerMethod.invoke(null, message);

        } catch (Exception e) {
            vodmordia.modtabs.ModTabs.LOGGER.error("Failed to open Sophisticated Backpacks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Find the slot number where the backpack is located
     */
    private int findBackpackSlot(PlayerEntity player) {
        try {
            Class<?> backpackItemClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem");

            // Check main inventory (slots 0-35 in the main inventory)
            for (int i = 0; i < player.getInventory().main.size(); i++) {
                ItemStack stack = player.getInventory().main.get(i);
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return i;
                }
            }

            // Check armor slots (slots 36-39)
            for (int i = 0; i < player.getInventory().armor.size(); i++) {
                ItemStack stack = player.getInventory().armor.get(i);
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return 36 + i; // Armor slots start at 36
                }
            }

            // Check offhand (slot 40)
            ItemStack offhandStack = player.getOffHandStack();
            if (!offhandStack.isEmpty() && backpackItemClass.isInstance(offhandStack.getItem())) {
                return 40;
            }

        } catch (Exception e) {
            // If reflection fails, return -1
        }

        return -1; // No backpack found
    }

    /**
     * Find the first backpack in the player's inventory
     */
    private ItemStack findPlayerBackpack(PlayerEntity player) {
        try {
            Class<?> backpackItemClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem");

            // Check main inventory
            for (ItemStack stack : player.getInventory().main) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return stack;
                }
            }

            // Check armor slots
            for (ItemStack stack : player.getInventory().armor) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return stack;
                }
            }

            // Check offhand
            ItemStack offhandStack = player.getOffHandStack();
            if (!offhandStack.isEmpty() && backpackItemClass.isInstance(offhandStack.getItem())) {
                return offhandStack;
            }

        } catch (Exception e) {
            // If reflection fails, return null
        }

        return null;
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModIntegrationManager.isModLoaded(ModIntegration.SOPHISTICATED_BACKPACKS) && hasBackpack(player);
    }

    private boolean hasBackpack(PlayerEntity player) {
        try {
            Class<?> backpackItemClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem");

            // Check main inventory (using correct Fabric method)
            for (ItemStack stack : player.getInventory().main) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return true;
                }
            }

            // Check armor slots
            for (ItemStack stack : player.getInventory().armor) {
                if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                    return true;
                }
            }

            // Check offhand
            ItemStack offhandStack = player.getOffHandStack();
            if (!offhandStack.isEmpty() && backpackItemClass.isInstance(offhandStack.getItem())) {
                return true;
            }

        } catch (Exception e) {
            // If reflection fails, return false
        }

        return false;
    }

    private static final String BACKPACK_SCREEN = "net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen";
    private static final String STORAGE_SCREEN_BASE = "net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase";
    /** SB's StorageScreenBase.HEIGHT_WITHOUT_STORAGE_SLOTS (title + player inventory) and slot pitch. */
    private static final int SB_HEIGHT_WITHOUT_STORAGE_SLOTS = 114;
    private static final int SB_SLOT_SIZE = 18;
    /** Never trade away more storage rows than this to make room for the bar. */
    private static final int MIN_VISIBLE_STORAGE_ROWS = 3;
    private static Method updateDimensionsMethod;
    private static boolean updateDimensionsLookupFailed;

    /**
     * SB sizes the backpack GUI to the window: {@code StorageScreenBase.updateDimensionsAndSlotPositions(h)}
     * shows {@code min(rows, (h - 114) / 18)} storage rows, so imageHeight can reach the full
     * scaled window height and topPos drops to 0-8px. Our bar hangs above topPos, so on tall
     * tiers (gold/diamond/netherite at GUI scale 4 on 1080p) it was pushed off the top of
     * the screen. Called from Fabric ScreenEvents.BEFORE_INIT (before SB's init(), after its
     * constructor/resize already sized it; screen.height may still be stale there, hence the
     * explicit scaledHeight),
     * this re-runs SB's own sizing with the bar's headroom taken off both ends (the GUI is
     * vertically centred), so topPos >= headroom. SB then shows a few fewer rows and
     * scrolls; when the GUI already fits, min() leaves it unchanged.
     */
    public static void reserveTabBarHeadroom(Screen screen, int scaledHeight) {
        if (screen == null || !BACKPACK_SCREEN.equals(screen.getClass().getName())) return;
        int headroom = TabsMenu.tabBarHeadroom(screen);
        if (headroom <= 0) return;
        Method m = updateDimensionsMethod();
        if (m == null) return;
        int minHeight = SB_HEIGHT_WITHOUT_STORAGE_SLOTS + MIN_VISIBLE_STORAGE_ROWS * SB_SLOT_SIZE;
        int availableHeight = Math.max(minHeight, scaledHeight - 2 * headroom);
        try {
            m.invoke(screen, availableHeight);
        } catch (Exception e) {
            updateDimensionsLookupFailed = true;
            updateDimensionsMethod = null;
        }
    }

    private static Method updateDimensionsMethod() {
        if (updateDimensionsMethod != null || updateDimensionsLookupFailed) return updateDimensionsMethod;
        try {
            Method m = Class.forName(STORAGE_SCREEN_BASE).getDeclaredMethod("updateDimensionsAndSlotPositions", int.class);
            m.setAccessible(true);
            updateDimensionsMethod = m;
        } catch (Exception e) {
            updateDimensionsLookupFailed = true;
        }
        return updateDimensionsMethod;
    }

    @Override
    public void initTabOnScreens() {
        // Register the backpack screen with tabs
        vodmordia.modtabs.api.tabs_menu.ScreenRegistry.builder()
            .withStandardDimensions()
            .withPositioning(vodmordia.modtabs.api.tabs_menu.TabPositioning.GUI_RELATIVE)
            .withActualGeometry() // backpack GUI size varies by tier; track the real GUI box
            .registerAllTabs("net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen");
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        try {
            Class<?> screenClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen");
            return screenClass.isInstance(currentScreen);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public Text getTooltip() {
        return Text.literal("Sophisticated Backpacks");
    }

    /**
     * Try to get the backpack item using reflection
     * Handles both NeoForge (supplier pattern) and Fabric (direct item pattern)
     */
    private static ItemStack getBackpackItem() {
        try {
            Class<?> itemsClass = Class.forName("net.p3pp3rf1y.sophisticatedbackpacks.init.ModItems");
            Field backpackField = itemsClass.getField("BACKPACK");
            Object backpackFieldValue = backpackField.get(null);

            // Check if this is a direct Item (Fabric pattern)
            if (backpackFieldValue instanceof Item) {
                return new ItemStack((Item) backpackFieldValue);
            }

            // Otherwise try supplier pattern (NeoForge pattern)
            try {
                Method getMethod = backpackFieldValue.getClass().getMethod("get");
                Item backpackItem = (Item) getMethod.invoke(backpackFieldValue);
                return new ItemStack(backpackItem);
            } catch (NoSuchMethodException e) {
                // If it's not a supplier and not an Item, log for debugging
                vodmordia.modtabs.ModTabs.LOGGER.warn("BACKPACK field is neither Item nor Supplier: " + backpackFieldValue.getClass().getName());
                return new ItemStack(Items.BUNDLE);
            }

        } catch (Exception e) {
            // Log the actual error for debugging
            vodmordia.modtabs.ModTabs.LOGGER.warn("Failed to get Sophisticated Backpacks item via reflection: " + e.getMessage());
            return new ItemStack(Items.BUNDLE);
        }
    }
}
