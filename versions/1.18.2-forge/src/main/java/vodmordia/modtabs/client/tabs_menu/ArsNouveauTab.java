package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
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

@TabConfig(configKey = "arsNouveauTab", defaultEnabled = true, defaultOrder = 0)
public class ArsNouveauTab extends ConfigurableItemTab {

    public ArsNouveauTab() {
        super(() -> getArchmageSpellbook(), Config.Baked.arsNouveauTabCustomIcon, "arsNouveau");
    }

    private static ItemStack getArchmageSpellbook() {
        // Ars Nouveau 2.x (1.18.2): ItemsRegistry.ARCHMAGE_SPELLBOOK is a plain static Item field.
        try {
            Class<?> itemsRegistryClass = Class.forName("com.hollingsworth.arsnouveau.setup.ItemsRegistry");
            Field archmageSpellbookField = itemsRegistryClass.getField("ARCHMAGE_SPELLBOOK");
            Object value = archmageSpellbookField.get(null);
            if (value instanceof Item item) return new ItemStack(item);
            return new ItemStack((Item) value.getClass().getMethod("get").invoke(value));
        } catch (Exception e) {
            // Fallback to vanilla item
            return new ItemStack(Items.ENCHANTED_BOOK);
        }
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            // Find any spellbook in player's inventory
            ItemStack spellbookStack = findSpellbookInInventory(player);
            if (spellbookStack.isEmpty()) {
                // No spellbook: fall back to an Ars Zero staff/circlet held in a hand
                openArsZeroDevice(player);
                return;
            }

            // Temporarily put spellbook in hand for constructor, then restore
            ItemStack originalMainHand = player.getMainHandItem().copy();
            boolean needsRestore = true;

            try {
                // Temporarily set the spellbook in main hand for the constructor
                player.getInventory().setItem(player.getInventory().selected, spellbookStack);

                // Ars Nouveau 2.x has no public GuiSpellBook(InteractionHand) constructor; the book's own
                // open-menu keybind calls SpellBook.onOpenBookMenuKeyPressed(ItemStack, Player), which
                // resolves the glyph list + tier and calls GuiSpellBook.open(stack, tier).
                Item bookItem = spellbookStack.getItem();
                bookItem.getClass().getMethod("onOpenBookMenuKeyPressed", ItemStack.class, Player.class)
                        .invoke(bookItem, spellbookStack, player);

                // Restore original item in hand
                player.getInventory().setItem(player.getInventory().selected, originalMainHand);
                needsRestore = false;


            } finally {
                // Make sure we restore the original item even if something goes wrong
                if (needsRestore) {
                    player.getInventory().setItem(player.getInventory().selected, originalMainHand);
                }
            }

        } catch (Exception e) {
            // Silently ignore errors - mod may not be available
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        if (!Config.Baked.arsNouveauTabEnabled) return false;
        if (ModIntegrationManager.isModLoaded(ModIntegration.ARS_NOUVEAU) && hasSpellbook(player)) return true;
        return ModIntegrationManager.isModLoaded(ModIntegration.ARS_ZERO) && getArsZeroDeviceInHand(player) != null;
    }

    private static boolean isArsZeroDevice(ItemStack stack) {
        return !stack.isEmpty() && vodmordia.modtabs.utils.ClassCache.isInstance(
                vodmordia.modtabs.utils.ScreenClasses.ARS_ZERO_DEVICE_ITEM, stack.getItem());
    }

    /** Ars Zero screens are bound to an InteractionHand, so only devices held in a hand are usable. */
    private static ItemStack getArsZeroDeviceInHand(Player player) {
        if (isArsZeroDevice(player.getMainHandItem())) return player.getMainHandItem();
        if (isArsZeroDevice(player.getOffhandItem())) return player.getOffhandItem();
        return null;
    }

    private static void openArsZeroDevice(Player player) {
        ItemStack device = getArsZeroDeviceInHand(player);
        if (device == null) return;
        try {
            Method open = device.getItem().getClass().getMethod("onOpenBookMenuKeyPressed", ItemStack.class, Player.class);
            open.invoke(device.getItem(), device, player);
        } catch (Exception e) {
            // Silently ignore errors - Ars Zero API may have changed
        }
    }

    private boolean hasSpellbook(Player player) {
        try {
            // Check main inventory and hotbar
            for (ItemStack stack : player.getInventory().items) {
                if (!stack.isEmpty() && isSpellbook(stack)) {
                    return true;
                }
            }

            // Check offhand
            ItemStack offhandStack = player.getOffhandItem();
            if (!offhandStack.isEmpty() && isSpellbook(offhandStack)) {
                return true;
            }

            // Check Curios if available
            if (ModIntegrationManager.isModLoaded(ModIntegration.CURIOS) && !findSpellbookInCurios(player).isEmpty()) {
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private ItemStack findSpellbookInInventory(Player player) {
        // Check hands first
        if (isSpellbook(player.getMainHandItem())) {
            return player.getMainHandItem();
        }
        if (isSpellbook(player.getOffhandItem())) {
            return player.getOffhandItem();
        }

        // Check inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && isSpellbook(stack)) {
                return stack;
            }
        }

        // Check Curios if available
        if (ModIntegrationManager.isModLoaded(ModIntegration.CURIOS)) {
            return findSpellbookInCurios(player);
        }

        return ItemStack.EMPTY;
    }

    /**
     * Returns the first spellbook in any Curios slot, or EMPTY.
     * Curios 1.18.2 (5.0.x): {@code CuriosApi.getCuriosHelper().findCurios(LivingEntity, Predicate)}
     * returns {@code List<SlotResult>} (record with {@code stack()}).
     */
    private ItemStack findSpellbookInCurios(Player player) {
        try {
            Class<?> curiosAPIClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Object helper = curiosAPIClass.getMethod("getCuriosHelper").invoke(null);
            if (helper == null) return ItemStack.EMPTY;
            Class<?> helperClass = Class.forName("top.theillusivec4.curios.api.type.util.ICuriosHelper");
            Method findCurios = helperClass.getMethod("findCurios",
                    net.minecraft.world.entity.LivingEntity.class, java.util.function.Predicate.class);
            java.util.function.Predicate<ItemStack> spellbookPredicate = stack ->
                    !stack.isEmpty() && isSpellbook(stack);

            @SuppressWarnings("unchecked")
            java.util.List<Object> results = (java.util.List<Object>) findCurios.invoke(helper, player, spellbookPredicate);
            if (results == null || results.isEmpty()) return ItemStack.EMPTY;
            Object first = results.get(0);
            return (ItemStack) first.getClass().getDeclaredMethod("stack").invoke(first);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    private boolean isSpellbook(ItemStack stack) {
        return vodmordia.modtabs.utils.ClassCache.isInstance(
                vodmordia.modtabs.utils.ScreenClasses.ARS_NOUVEAU_SPELLBOOK_ITEM, stack.getItem());
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return vodmordia.modtabs.utils.ClassCache.isInstance(
                vodmordia.modtabs.utils.ScreenClasses.ARS_NOUVEAU_SPELLBOOK_GUI, currentScreen)
            || vodmordia.modtabs.utils.ClassCache.isInstance(
                vodmordia.modtabs.utils.ScreenClasses.ARS_ZERO_STAFF_GUI, currentScreen)
            || vodmordia.modtabs.utils.ClassCache.isInstance(
                vodmordia.modtabs.utils.ScreenClasses.ARS_ZERO_STATIC_STAFF_SCREEN, currentScreen)
            || vodmordia.modtabs.utils.ClassCache.isInstance(
                vodmordia.modtabs.utils.ScreenClasses.ARS_ZERO_CIRCLET_GUI, currentScreen);
    }

    @Override
    public Component getTooltip() {
        return new TranslatableComponent("tooltip." + ModTabs.MOD_ID + ".tab.ars_nouveau.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder()
            .withStandardDimensions()
            .inverted()
            .registerAllTabs(
                vodmordia.modtabs.utils.ScreenClasses.ARS_NOUVEAU_SPELLBOOK_GUI,
                vodmordia.modtabs.utils.ScreenClasses.ARS_ZERO_STAFF_GUI,
                vodmordia.modtabs.utils.ScreenClasses.ARS_ZERO_STATIC_STAFF_SCREEN,
                vodmordia.modtabs.utils.ScreenClasses.ARS_ZERO_CIRCLET_GUI);
    }
}
