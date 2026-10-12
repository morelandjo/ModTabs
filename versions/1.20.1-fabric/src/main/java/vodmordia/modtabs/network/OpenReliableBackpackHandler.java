package vodmordia.modtabs.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.ModTabs;

import java.lang.reflect.Constructor;

/**
 * Server side of the Reliable Backpacks tab. Replicates the body of the mod's
 * {@code EntityInteractionEvents.onEntityInteract} minus the "must be behind" check, so the
 * player can open their own backpack from a tab click. Reliable Backpacks is resolved by name
 * (no compile dependency); if the backpack is carried but not worn it is swapped into the
 * chest slot first, as the mod itself only tracks a worn backpack.
 */
public final class OpenReliableBackpackHandler {
    public static final Identifier CHANNEL = new Identifier(ModTabs.MOD_ID, "open_reliable_backpack");

    private static final String ITEM_CLASS = "com.evandev.reliable_backpacks.common.items.BackpackItem";
    private static final String CONTAINER_CLASS = "com.evandev.reliable_backpacks.common.items.BackpackItemContainer";
    private static final String MENU_CLASS = "com.evandev.reliable_backpacks.common.menus.BackpackMenu";

    private OpenReliableBackpackHandler() {}

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL,
                (server, player, handler, buf, sender) -> server.execute(() -> open(player)));
    }

    private static void open(ServerPlayerEntity player) {
        try {
            Class<?> itemClass = Class.forName(ITEM_CLASS);
            Class<?> containerClass = Class.forName(CONTAINER_CLASS);
            Class<?> menuClass = Class.forName(MENU_CLASS);

            if (ensureBackpackEquipped(player, itemClass).isEmpty()) return;

            Constructor<?> containerCtor = containerClass.getConstructor(LivingEntity.class, PlayerEntity.class);
            Inventory container = (Inventory) containerCtor.newInstance(player, player);
            Constructor<?> menuCtor = menuClass.getConstructor(int.class, PlayerInventory.class, Inventory.class);

            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inv, p) -> {
                        try {
                            return (ScreenHandler) menuCtor.newInstance(syncId, inv, container);
                        } catch (ReflectiveOperationException e) {
                            throw new IllegalStateException(e);
                        }
                    },
                    Text.translatable("container.backpack")));
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Failed to open Reliable Backpack: " + e.getMessage());
        }
    }

    private static ItemStack ensureBackpackEquipped(ServerPlayerEntity player, Class<?> itemClass) {
        ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
        if (!chest.isEmpty() && itemClass.isInstance(chest.getItem())) return chest;

        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.main.size(); i++) {
            ItemStack stack = inventory.main.get(i);
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                inventory.main.set(i, chest);
                player.equipStack(EquipmentSlot.CHEST, stack);
                return stack;
            }
        }

        ItemStack offhand = player.getOffHandStack();
        if (!offhand.isEmpty() && itemClass.isInstance(offhand.getItem())) {
            player.equipStack(EquipmentSlot.OFFHAND, chest);
            player.equipStack(EquipmentSlot.CHEST, offhand);
            return offhand;
        }
        return ItemStack.EMPTY;
    }
}
