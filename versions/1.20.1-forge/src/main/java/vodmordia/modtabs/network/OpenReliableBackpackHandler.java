package vodmordia.modtabs.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;

/**
 * Server handler for {@link OpenReliableBackpackPayload} (Forge 1.20.1).
 *
 * Replicates the body of {@code EntityInteractionEvents.onEntityInteract} from Reliable
 * Backpacks minus the {@code isBehind} proximity check, so the player can open their own
 * backpack from a tab click. The mod's {@code BackpackItemContainer} (persistence via
 * {@code BlockEntityTag}) and {@code BackpackMenu} are used reflectively so the row count
 * follows the mod's config.
 *
 * If the backpack is in the inventory but not equipped, it is swapped into the chest slot
 * first so the mod's own container lookup finds it.
 */
public class OpenReliableBackpackHandler {

    private static final String RELIABLE_BACKPACKS_MENU =
            "com.evandev.reliable_backpacks.common.menus.BackpackMenu";

    public static void handle(OpenReliableBackpackPayload payload, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer == null) return;

            try {
                Class<?> backpackItemClass = ClassCache.resolve(ScreenClasses.RELIABLE_BACKPACKS_ITEM);
                Class<?> containerClass = ClassCache.resolve(ScreenClasses.RELIABLE_BACKPACKS_CONTAINER);
                Class<?> menuClass = ClassCache.resolve(RELIABLE_BACKPACKS_MENU);
                if (backpackItemClass == null || containerClass == null || menuClass == null) return;

                ItemStack equipped = ensureBackpackEquipped(serverPlayer, backpackItemClass);
                if (equipped.isEmpty()) return;

                Constructor<?> ctor = containerClass.getDeclaredConstructor(LivingEntity.class, Player.class);
                Object containerObj = ctor.newInstance(serverPlayer, serverPlayer);
                if (!(containerObj instanceof SimpleContainer container)) return;

                Constructor<?> menuCtor = menuClass.getConstructor(int.class, Inventory.class, Container.class);
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> newMenu(menuCtor, id, inv, container),
                        Component.translatable("container.backpack")
                ));
            } catch (Exception e) {
                ModTabs.LOGGER.debug("Failed to open Reliable Backpack: " + e.getMessage());
            }
        });
        ctx.setPacketHandled(true);
    }

    private static AbstractContainerMenu newMenu(Constructor<?> ctor, int id, Inventory inv, Container container) {
        try {
            return (AbstractContainerMenu) ctor.newInstance(id, inv, container);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Failed to create Reliable Backpack menu: " + e.getMessage());
            return null;
        }
    }

    /**
     * Make sure a Reliable Backpacks backpack is in the chest slot. If one is in the
     * inventory or offhand, swap it with whatever is currently in the chest slot.
     * Returns the equipped backpack stack, or empty if no backpack was found anywhere.
     */
    private static ItemStack ensureBackpackEquipped(ServerPlayer player, Class<?> backpackItemClass) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.isEmpty() && backpackItemClass.isInstance(chest.getItem())) {
            return chest;
        }

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack stack = inventory.items.get(i);
            if (!stack.isEmpty() && backpackItemClass.isInstance(stack.getItem())) {
                inventory.items.set(i, chest);
                player.setItemSlot(EquipmentSlot.CHEST, stack);
                return stack;
            }
        }

        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && backpackItemClass.isInstance(offhand.getItem())) {
            player.setItemSlot(EquipmentSlot.OFFHAND, chest);
            player.setItemSlot(EquipmentSlot.CHEST, offhand);
            return offhand;
        }

        return ItemStack.EMPTY;
    }
}
