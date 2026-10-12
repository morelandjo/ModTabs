package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.SimpleItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.lang.reflect.Method;

/**
 * Modonomicon. Books are item-driven, so (as on Forge) the tab opens the first ModonomiconItem found in
 * the player's inventory via {@code BookGuiManager.get().openBook(book.getId())} and is only enabled
 * while such an item is present.
 */
@TabConfig(configKey = "modonomiconTab", defaultEnabled = true, defaultOrder = 0)
public final class ModonomiconTab extends SimpleItemTab {
    private static final String ITEM = "com.klikli_dev.modonomicon.item.ModonomiconItem";
    private static final String GUI_MANAGER = "com.klikli_dev.modonomicon.client.gui.BookGuiManager";
    private static final String OVERVIEW_SCREEN = "com.klikli_dev.modonomicon.client.gui.book.BookOverviewScreen";
    private static final String ERROR_SCREEN = "com.klikli_dev.modonomicon.client.gui.book.BookErrorScreen";

    public ModonomiconTab() {
        super(ModonomiconTab::getIcon);
    }

    private static ItemStack getIcon() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            ItemStack book = findBookInInventory(player);
            if (!book.isEmpty()) return book;
        }
        return new ItemStack(Items.KNOWLEDGE_BOOK);
    }

    private static ItemStack findBookInInventory(PlayerEntity player) {
        Class<?> itemClass;
        try {
            itemClass = Class.forName(ITEM);
        } catch (ClassNotFoundException e) {
            return ItemStack.EMPTY;
        }
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (!player.getWorld().isClient) return;
        ItemStack stack = findBookInInventory(player);
        if (stack.isEmpty()) return;
        try {
            Object book = Class.forName(ITEM).getMethod("getBook", ItemStack.class).invoke(null, stack);
            if (book == null) return;
            Object id = book.getClass().getMethod("getId").invoke(book);
            if (!(id instanceof Identifier bookId)) return;
            Class<?> mgrClass = Class.forName(GUI_MANAGER);
            Object mgr = mgrClass.getMethod("get").invoke(null);
            if (mgr == null) return;
            Method openBook = mgrClass.getMethod("openBook", Identifier.class);
            openBook.invoke(mgr, bookId);
        } catch (ReflectiveOperationException e) {
            ModTabs.LOGGER.warn("Failed to open Modonomicon book: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.modonomiconTabEnabled
                && ModIntegrationManager.isModLoaded(ModIntegration.MODONOMICON)
                && !findBookInInventory(player).isEmpty();
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;
        String name = currentScreen.getClass().getName();
        return OVERVIEW_SCREEN.equals(name) || ERROR_SCREEN.equals(name);
    }

    @Override
    public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.modonomicon.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder().withStandardDimensions().registerAllTabs(OVERVIEW_SCREEN, ERROR_SCREEN);
    }
}
