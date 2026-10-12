package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ConfigurableItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@TabConfig(configKey = "eccentricTomeTab", defaultEnabled = true, defaultOrder = 0)
public class EccentricTomeTab extends ConfigurableItemTab {

    public EccentricTomeTab() {
        super(() -> getCurrentTomeOrBookItem(), Config.Baked.eccentricTomeTabCustomIcon, "eccentricTome");
    }

    // Returns the tome if unconverted, or the converted book if converted
    private static ItemStack getCurrentTomeOrBookItem() {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return getTomeItem();
        }

        try {
            // Check if player has a converted book (non-tome eccentric tome item)
            ItemStack convertedBook = findConvertedBook(player);
            if (!convertedBook.isEmpty()) {
                return convertedBook;
            }

            // Otherwise return the regular tome
            return getTomeItem();
        } catch (Exception e) {
            return getTomeItem();
        }
    }

    private static ItemStack getTomeItem() {
        // Try to get Eccentric Tome's tome item via reflection, fallback to vanilla item
        try {
            Class<?> eccentricTomeClass = Class.forName("website.eccentric.tome.EccentricTome");
            Field tomeField = eccentricTomeClass.getField("TOME");
            Object registryObject = tomeField.get(null);
            Method getMethod = registryObject.getClass().getMethod("get");
            Item tomeItem = (Item) getMethod.invoke(registryObject);
            return new ItemStack(tomeItem);
        } catch (Exception e) {
            // Fallback to vanilla item
            return new ItemStack(Items.BOOK);
        }
    }

    // Find a book that was converted from the tome
    private static ItemStack findConvertedBook(Player player) {
        try {
            // Check inventory for items that are NOT tomes but ARE from eccentric tome
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && isConvertedFromTome(stack)) {
                    return stack;
                }
            }

            // Check offhand
            ItemStack offhand = player.getOffhandItem();
            if (!offhand.isEmpty() && isConvertedFromTome(offhand)) {
                return offhand;
            }
        } catch (Exception e) {
            // Ignore
        }
        return ItemStack.EMPTY;
    }

    // Check if an item was converted from the tome (has tome data but isn't a tome)
    private static boolean isConvertedFromTome(ItemStack stack) {
        try {
            // If it's a tome itself, return false
            if (isTome(stack)) {
                return false;
            }

            // Check if it has eccentric tome data components (meaning it was converted)
            Class<?> tomeUtilsClass = Class.forName("website.eccentric.tome.TomeUtils");
            Method getModsBooksMethod = tomeUtilsClass.getMethod("getModsBooks", ItemStack.class);
            Object modsBooks = getModsBooksMethod.invoke(null, stack);

            // If it has mods books data, it was converted from tome
            return modsBooks != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            // Check if player has a converted book
            ItemStack convertedBook = findConvertedBook(player);
            if (!convertedBook.isEmpty()) {
                // Open the converted book's screen
                openConvertedBookScreen(player, convertedBook);
                return;
            }

            // Otherwise, find and open tome
            ItemStack tomeStack = findTomeInInventory(player);
            if (tomeStack.isEmpty()) {
                return; // No tome found
            }

            // Create original TomeScreen
            Class<?> tomeScreenClass = Class.forName("website.eccentric.tome.client.TomeScreen");
            Object originalTomeScreen = tomeScreenClass.getDeclaredConstructor(ItemStack.class)
                .newInstance(tomeStack);

            // Initialize the original screen properly (set minecraft instance)
            Method initMethod = Screen.class.getDeclaredMethod("init", net.minecraft.client.Minecraft.class, int.class, int.class);
            initMethod.setAccessible(true);
            var minecraft = Minecraft.getInstance();
            initMethod.invoke(originalTomeScreen, minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());

            // Create a wrapper that delegates to original but overrides mouseClicked
            TomeScreenWrapper wrapper = new TomeScreenWrapper((Screen) originalTomeScreen, player);

            // Open the GUI
            Minecraft.getInstance().setScreen(wrapper);

        } catch (Exception e) {
            ModTabs.LOGGER.error("Error opening TomeScreen", e);
        }
    }

    private void openConvertedBookScreen(Player player, ItemStack bookStack) {
        try {
            // Use the book item's use() method to open its screen
            bookStack.getItem().use(player.level(), player, net.minecraft.world.InteractionHand.MAIN_HAND);
        } catch (Exception e) {
            ModTabs.LOGGER.error("Error opening converted book screen", e);
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        boolean configEnabled = Config.Baked.eccentricTomeTabEnabled;
        boolean modLoaded = ModIntegrationManager.isModLoaded(ModIntegration.ECCENTRIC_TOME);
        boolean hasTheTome = hasTome(player);
        boolean hasConvertedBook = !findConvertedBook(player).isEmpty();

        // Show tab if player has either the tome or a converted book
        return configEnabled && modLoaded && (hasTheTome || hasConvertedBook);
    }

    private boolean hasTome(Player player) {
        try {
            // Check main inventory and hotbar
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && isTome(stack)) {
                    return true;
                }
            }

            // Check offhand
            ItemStack offhandStack = player.getOffhandItem();
            if (!offhandStack.isEmpty() && isTome(offhandStack)) {
                return true;
            }

            // Check Curios if available
            if (ModIntegrationManager.isModLoaded(ModIntegration.CURIOS)) {
                try {
                    Class<?> curiosAPIClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                    Method getCuriosInventoryMethod = curiosAPIClass.getDeclaredMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);
                    Object optionalCuriosInventory = getCuriosInventoryMethod.invoke(null, player);

                    Method isPresentMethod = optionalCuriosInventory.getClass().getDeclaredMethod("isPresent");
                    boolean isPresent = (Boolean) isPresentMethod.invoke(optionalCuriosInventory);

                    if (isPresent) {
                        Method getMethod = optionalCuriosInventory.getClass().getDeclaredMethod("get");
                        Object curiosInventory = getMethod.invoke(optionalCuriosInventory);

                        Method findCuriosMethod = curiosInventory.getClass().getDeclaredMethod("findCurios", java.util.function.Predicate.class);

                        java.util.function.Predicate<ItemStack> tomePredicate = stack ->
                            !stack.isEmpty() && isTome(stack);

                        @SuppressWarnings("unchecked")
                        java.util.List<Object> curioResults = (java.util.List<Object>) findCuriosMethod.invoke(curiosInventory, tomePredicate);

                        if (!curioResults.isEmpty()) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                    // Silently ignore Curios errors
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private ItemStack findTomeInInventory(Player player) {
        // Check hands first
        if (isTome(player.getMainHandItem())) {
            return player.getMainHandItem();
        }
        if (isTome(player.getOffhandItem())) {
            return player.getOffhandItem();
        }

        // Check inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && isTome(stack)) {
                return stack;
            }
        }

        // Check Curios if available
        if (ModIntegrationManager.isModLoaded(ModIntegration.CURIOS)) {
            try {
                Class<?> curiosAPIClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                Method getCuriosInventoryMethod = curiosAPIClass.getDeclaredMethod("getCuriosInventory", net.minecraft.world.entity.LivingEntity.class);
                Object optionalCuriosInventory = getCuriosInventoryMethod.invoke(null, player);

                Method isPresentMethod = optionalCuriosInventory.getClass().getDeclaredMethod("isPresent");
                boolean isPresent = (Boolean) isPresentMethod.invoke(optionalCuriosInventory);

                if (isPresent) {
                    Method getMethod = optionalCuriosInventory.getClass().getDeclaredMethod("get");
                    Object curiosInventory = getMethod.invoke(optionalCuriosInventory);

                    Method findCuriosMethod = curiosInventory.getClass().getDeclaredMethod("findCurios", java.util.function.Predicate.class);

                    java.util.function.Predicate<ItemStack> tomePredicate = stack ->
                        !stack.isEmpty() && isTome(stack);

                    @SuppressWarnings("unchecked")
                    java.util.List<Object> curioResults = (java.util.List<Object>) findCuriosMethod.invoke(curiosInventory, tomePredicate);

                    if (!curioResults.isEmpty()) {
                        // Get the first result and extract the ItemStack
                        Object curioResult = curioResults.get(0);
                        Method getStackMethod = curioResult.getClass().getDeclaredMethod("stack");
                        return (ItemStack) getStackMethod.invoke(curioResult);
                    }
                }
            } catch (Exception e) {
                // Silently ignore Curios errors
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean isTome(ItemStack stack) {
        return vodmordia.modtabs.utils.ClassCache.isInstance(vodmordia.modtabs.utils.ScreenClasses.ECCENTRIC_TOME_ITEM, stack.getItem());
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return vodmordia.modtabs.utils.ClassCache.isInstance(vodmordia.modtabs.utils.ScreenClasses.ECCENTRIC_TOME_SCREEN, currentScreen);
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("tooltip." + ModTabs.MOD_ID + ".tab.eccentric_tome.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder()
            .withStandardDimensions()
            .inverted()
            .registerAllTabs("website.eccentric.tome.client.TomeScreen");
    }

    // Wrapper that delegates to original TomeScreen but overrides mouseClicked for custom conversion
    private static class TomeScreenWrapper extends Screen {
        private final Screen originalTomeScreen;
        private final Player player;

        public TomeScreenWrapper(Screen originalTomeScreen, Player player) {
            super(Component.empty());
            this.originalTomeScreen = originalTomeScreen;
            this.player = player;
        }

        @Override
        protected void init() {
            super.init();
            // Make sure the original screen is also initialized
            try {
                // Call the protected init() method from Screen class
                Method initMethod = Screen.class.getDeclaredMethod("init", net.minecraft.client.Minecraft.class, int.class, int.class);
                initMethod.setAccessible(true);
                var minecraft = Minecraft.getInstance();
                initMethod.invoke(originalTomeScreen, minecraft, minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
                ModTabs.LOGGER.info("Successfully initialized original TomeScreen");
            } catch (Exception e) {
                ModTabs.LOGGER.error("Error initializing original screen", e);
            }
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            if (button == 0) { // LEFT_CLICK
                try {
                    // On Forge 1.20.1, TomeScreen.render() stores the hovered entry in its private
                    // "hoveredIndex"/"hoveredBook" fields (there is no "book" field). Read them BEFORE
                    // delegating: the original mouseClicked sends Eccentric Tome's own ConvertMessage
                    // and closes the screen.
                    Field indexField = originalTomeScreen.getClass().getDeclaredField("hoveredIndex");
                    indexField.setAccessible(true);
                    int hoveredIndex = indexField.getInt(originalTomeScreen);
                    Field bookField = originalTomeScreen.getClass().getDeclaredField("hoveredBook");
                    bookField.setAccessible(true);
                    ItemStack selectedBook = hoveredIndex >= 0 ? (ItemStack) bookField.get(originalTomeScreen) : null;

                    if (selectedBook != null && !selectedBook.isEmpty()
                            && convertTomeNotInHand(selectedBook, x, y, button)) {
                        return true;
                    }
                } catch (Exception e) {
                    ModTabs.LOGGER.error("Error in custom mouseClicked", e);
                }
            }

            // Tome is held (or nothing was selected): Eccentric Tome's own ConvertMessage
            // converts the tome in the player's hand, so let the original screen handle it.
            return originalTomeScreen.mouseClicked(x, y, button);
        }

        /**
         * Eccentric Tome's ConvertMessage only converts a tome held in the main/off hand
         * (Tome.inHand). When the tab opened a tome from elsewhere in the inventory,
         * convert that slot instead. Returns false to fall back to the mod's own handling.
         */
        private boolean convertTomeNotInHand(ItemStack selectedBook, double x, double y, int button) {
            if (isTome(player.getMainHandItem()) || isTome(player.getOffhandItem())) {
                return false; // Held: the mod's own packet handles it.
            }

            int tomeSlot = findTomeSlot(player);
            if (tomeSlot == -1) {
                ModTabs.LOGGER.warn("Eccentric Tome not found in player inventory; cannot convert");
                return false;
            }

            Minecraft minecraft = Minecraft.getInstance();
            var connection = minecraft.getConnection();
            if (connection != null && ModTabs.CHANNEL.isRemotePresent(connection.getConnection())) {
                // Server has ModTabs: it validates the slot and converts it in place.
                ModTabs.CHANNEL.sendToServer(
                    new vodmordia.modtabs.network.TomeConvertPayload(tomeSlot, selectedBook.copy()));
                minecraft.setScreen(null);
                return true;
            }

            // Server without ModTabs (client-only install): temporarily swap the tome into the
            // main hand with vanilla inventory clicks, let the mod's own ConvertMessage convert
            // it, then swap back so the converted book ends up in the tome's original slot.
            return convertViaHandSwap(tomeSlot, x, y, button);
        }

        private boolean convertViaHandSwap(int tomeSlot, double x, double y, int button) {
            Minecraft minecraft = Minecraft.getInstance();
            int menuSlot = toInventoryMenuSlot(tomeSlot);
            if (minecraft.gameMode == null || menuSlot == -1 || player.containerMenu != player.inventoryMenu) {
                ModTabs.LOGGER.warn("Cannot convert Eccentric Tome in slot {} without server-side ModTabs", tomeSlot);
                return false;
            }

            int containerId = player.inventoryMenu.containerId;
            int hotbarSlot = player.getInventory().selected;
            minecraft.gameMode.handleInventoryMouseClick(containerId, menuSlot, hotbarSlot,
                net.minecraft.world.inventory.ClickType.SWAP, player);
            // Sends Eccentric Tome's ConvertMessage (converts the main hand) and closes the screen.
            originalTomeScreen.mouseClicked(x, y, button);
            minecraft.gameMode.handleInventoryMouseClick(containerId, menuSlot, hotbarSlot,
                net.minecraft.world.inventory.ClickType.SWAP, player);
            if (minecraft.screen == this) {
                minecraft.setScreen(null);
            }
            return true;
        }

        /** Maps a player Inventory index to its InventoryMenu slot index, or -1 if unsupported. */
        private static int toInventoryMenuSlot(int inventorySlot) {
            if (inventorySlot >= 0 && inventorySlot < 9) {
                return 36 + inventorySlot; // Hotbar
            }
            if (inventorySlot >= 9 && inventorySlot < 36) {
                return inventorySlot; // Main inventory
            }
            return -1; // Armor/offhand (offhand is handled as "held")
        }

        private int findTomeSlot(Player player) {
            // Check main hand (slot depends on selected hotbar slot)
            if (isTome(player.getMainHandItem())) {
                return player.getInventory().selected; // Hotbar slot
            }

            // Check offhand
            if (isTome(player.getOffhandItem())) {
                return 40; // Offhand slot
            }

            // Check inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty() && isTome(stack)) {
                    return i;
                }
            }

            return -1; // Not found
        }

        // Delegate all other methods to the original screen
        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            try {
                originalTomeScreen.render(guiGraphics, mouseX, mouseY, partialTick);
            } catch (Exception e) {
                ModTabs.LOGGER.error("Error in original screen render", e);
            }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return originalTomeScreen.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean isPauseScreen() {
            return originalTomeScreen.isPauseScreen();
        }

        @Override
        public void onClose() {
            originalTomeScreen.onClose();
            super.onClose();
        }
    }
}