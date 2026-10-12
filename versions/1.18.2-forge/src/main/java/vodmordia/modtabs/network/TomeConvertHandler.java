package vodmordia.modtabs.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import vodmordia.modtabs.ModTabs;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
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

                // Forge 1.18.2 Eccentric Tome exposes its logic on website.eccentric.tome.Tome
                // (getModsBooks(ItemStack) / convert(tome, book)); there is no TomeUtils.
                Class<?> tomeClass = Class.forName("website.eccentric.tome.Tome");

                // Confirm the slot actually holds a TomeItem before invoking convert -
                // otherwise a malicious client could swap any inventory stack for the selectedBook.
                Class<?> tomeItemClass = Class.forName("website.eccentric.tome.TomeItem");
                if (!tomeItemClass.isInstance(tomeStack.getItem())) {
                    ModTabs.LOGGER.warn("Rejecting TomeConvertPayload from {}: slot {} does not hold a TomeItem (was {})",
                            serverPlayer.getName().getString(), tomeSlot, tomeStack);
                    return;
                }

                // Resolve the requested book to the copy stored inside the tome, so only
                // books the tome actually contains can be produced (the client's NBT is never trusted).
                ItemStack storedBook = findStoredBook(tomeClass, tomeStack, selectedBook);
                if (storedBook == null) {
                    ModTabs.LOGGER.warn("Rejecting TomeConvertPayload from {}: tome in slot {} does not contain {}",
                            serverPlayer.getName().getString(), tomeSlot, selectedBook);
                    return;
                }

                Method convertMethod = tomeClass.getMethod("convert", ItemStack.class, ItemStack.class);
                ItemStack convertedBook = (ItemStack) convertMethod.invoke(null, tomeStack, storedBook.copy());
                if (convertedBook == null || convertedBook.isEmpty() || convertedBook == tomeStack) {
                    ModTabs.LOGGER.warn("Tome convert produced no book for slot {} book {}", tomeSlot, selectedBook);
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

    /** The stored book in Tome.getModsBooks(tome) matching {@code requested}, or null. */
    private static ItemStack findStoredBook(Class<?> tomeClass, ItemStack tomeStack, ItemStack requested) throws Exception {
        Object mods = tomeClass.getMethod("getModsBooks", ItemStack.class).invoke(null, tomeStack);
        if (!(mods instanceof Map<?, ?> map)) {
            return null;
        }

        ItemStack sameItem = null;
        for (Object value : map.values()) {
            if (!(value instanceof List<?> list)) continue;
            for (Object o : list) {
                if (!(o instanceof ItemStack stored) || stored.isEmpty()) continue;
                if (stored.sameItem(requested)) {
                    if (ItemStack.tagMatches(stored, requested)) {
                        return stored;
                    }
                    if (sameItem == null) {
                        sameItem = stored;
                    }
                }
            }
        }
        return sameItem;
    }
}
