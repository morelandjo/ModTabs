package vodmordia.modtabs.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import vodmordia.modtabs.ModTabs;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

public class TomeConvertHandler {

    public static void handle(TomeConvertPayload payload, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer serverPlayer = ctx.getSender();
            if (serverPlayer == null) return;

            try {
                int tomeSlot = payload.tomeSlot();
                ItemStack selectedBook = payload.selectedBook();

                ModTabs.LOGGER.info("Server received TomeConvertPayload: slot={}, book={}", tomeSlot, selectedBook);

                int containerSize = serverPlayer.getInventory().getContainerSize();
                if (tomeSlot < 0 || tomeSlot >= containerSize) {
                    ModTabs.LOGGER.warn("Rejecting TomeConvertPayload from {}: slot {} out of range [0,{})",
                            serverPlayer.getName().getString(), tomeSlot, containerSize);
                    return;
                }

                if (selectedBook.isEmpty() || selectedBook.getCount() > selectedBook.getMaxStackSize()) {
                    ModTabs.LOGGER.warn("Rejecting TomeConvertPayload from {}: invalid selectedBook {}",
                            serverPlayer.getName().getString(), selectedBook);
                    return;
                }

                ItemStack tomeStack = serverPlayer.getInventory().getItem(tomeSlot);

                if (tomeStack.isEmpty()) {
                    ModTabs.LOGGER.warn("Tome stack is empty at slot {}", tomeSlot);
                    return;
                }

                // Forge 1.20.1 Eccentric Tome exposes its logic on website.eccentric.tome.Tome
                // (getItems / convert(ItemStack, int)); TomeUtils only exists in the NeoForge port.
                Class<?> tomeClass = Class.forName("website.eccentric.tome.Tome");

                // Confirm the slot actually holds a TomeItem before invoking convert —
                // otherwise a malicious client could swap any inventory stack for the selectedBook.
                Class<?> tomeItemClass = Class.forName("website.eccentric.tome.TomeItem");
                if (!tomeItemClass.isInstance(tomeStack.getItem())) {
                    ModTabs.LOGGER.warn("Rejecting TomeConvertPayload from {}: slot {} does not hold a TomeItem (was {})",
                            serverPlayer.getName().getString(), tomeSlot, tomeStack);
                    return;
                }

                // Resolve the requested book to its index in the tome's own item list, so only
                // books the tome actually contains can be produced.
                int bookIndex = findBookIndex(tomeClass, tomeStack, selectedBook);
                if (bookIndex < 0) {
                    ModTabs.LOGGER.warn("Rejecting TomeConvertPayload from {}: tome in slot {} does not contain {}",
                            serverPlayer.getName().getString(), tomeSlot, selectedBook);
                    return;
                }

                Method convertMethod = tomeClass.getMethod("convert", ItemStack.class, int.class);
                ItemStack convertedBook = (ItemStack) convertMethod.invoke(null, tomeStack, bookIndex);
                if (convertedBook == null || convertedBook.isEmpty() || convertedBook == tomeStack) {
                    ModTabs.LOGGER.warn("Tome convert produced no book for slot {} index {}", tomeSlot, bookIndex);
                    return;
                }

                ModTabs.LOGGER.info("Server converted tome to: {}", convertedBook);

                serverPlayer.getInventory().setItem(tomeSlot, convertedBook);
                serverPlayer.containerMenu.broadcastChanges();

                ModTabs.LOGGER.info("Successfully converted tome in slot {} to book {}", tomeSlot, convertedBook.getItem());

            } catch (Exception e) {
                ModTabs.LOGGER.error("Failed to handle tome convert on server", e);
            }
        });
        ctx.setPacketHandled(true);
    }

    /** Index of the stored book matching {@code requested} in Tome.getItems(tome), or -1. */
    private static int findBookIndex(Class<?> tomeClass, ItemStack tomeStack, ItemStack requested) throws Exception {
        Object items = tomeClass.getMethod("getItems", ItemStack.class).invoke(null, tomeStack);
        if (!(items instanceof List<?> list)) {
            return -1;
        }

        int sameItem = -1;
        for (int i = 0; i < list.size(); i++) {
            if (!(list.get(i) instanceof ItemStack stored) || stored.isEmpty()) continue;
            if (ItemStack.isSameItemSameTags(stored, requested)) {
                return i;
            }
            if (sameItem == -1 && ItemStack.isSameItem(stored, requested)) {
                sameItem = i;
            }
        }
        return sameItem;
    }
}
