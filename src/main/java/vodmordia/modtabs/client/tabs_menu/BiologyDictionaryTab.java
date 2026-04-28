package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Method;

/**
 * Tab for the Biology Dictionary mod. Shows only when the player is carrying the
 * dictionary book (a vanilla {@code minecraft:writable_book} tagged with the
 * {@code biologydictionary} CUSTOM_DATA key — the mod doesn't register a custom item).
 *
 * <p>Detection and screen-opening both go through the mod's own static helpers
 * ({@code BiologyDictionaryItem.isBook}, {@code BiologyDictionaryEvent.openBookScreen})
 * so we honor any permission / config gating the mod itself applies.
 */
@TabConfig(configKey = "biologyDictionaryTab", defaultEnabled = true, defaultOrder = 0)
public class BiologyDictionaryTab extends IntegrationItemTab {

    private static final TabSpec SPEC = new TabSpec(
            "biologyDictionaryTab",
            ModIntegration.BIOLOGY_DICTIONARY,
            () -> Config.Baked.biologyDictionaryTabEnabled,
            "biologyDictionary",
            "biology_dictionary",
            TabSpec.Layout.invertedTop(),
            new String[] {
                    ScreenClasses.BIOLOGY_DICTIONARY_HOME_SCREEN,
                    ScreenClasses.BIOLOGY_DICTIONARY_ABOUT_SCREEN,
                    ScreenClasses.BIOLOGY_DICTIONARY_CONFIG_SCREEN,
                    ScreenClasses.BIOLOGY_DICTIONARY_ENTITY_OVERVIEW_SCREEN,
                    ScreenClasses.BIOLOGY_DICTIONARY_ENTITY_DETAIL_SCREEN
            },
            new String[] {
                    ScreenClasses.BIOLOGY_DICTIONARY_HOME_SCREEN,
                    ScreenClasses.BIOLOGY_DICTIONARY_ABOUT_SCREEN,
                    ScreenClasses.BIOLOGY_DICTIONARY_CONFIG_SCREEN,
                    ScreenClasses.BIOLOGY_DICTIONARY_ENTITY_OVERVIEW_SCREEN,
                    ScreenClasses.BIOLOGY_DICTIONARY_ENTITY_DETAIL_SCREEN
            }
    );

    public BiologyDictionaryTab() {
        super(SPEC, BiologyDictionaryTab::getBookIconStack, Config.Baked.biologyDictionaryTabCustomIcon);
    }

    /**
     * Tab icon: the dictionary book itself, produced via {@code BiologyDictionaryItem.createBook()}
     * so it carries the same NBT a real player-held copy would. Falls back to a plain writable
     * book if the mod isn't on the classpath.
     */
    private static ItemStack getBookIconStack() {
        try {
            Class<?> itemClass = ClassCache.resolve(ScreenClasses.BIOLOGY_DICTIONARY_ITEM);
            if (itemClass != null) {
                Method createBook = itemClass.getMethod("createBook");
                Object stack = createBook.invoke(null);
                if (stack instanceof ItemStack itemStack) return itemStack;
            }
        } catch (Exception ignored) {}
        return new ItemStack(Items.WRITABLE_BOOK);
    }

    @Override
    public boolean isEnabled(Player player) {
        // Tab only shows when the dictionary book is actually in the player's inventory.
        return super.isEnabled(player) && hasBook(player);
    }

    private static boolean hasBook(Player player) {
        Class<?> itemClass = ClassCache.resolve(ScreenClasses.BIOLOGY_DICTIONARY_ITEM);
        if (itemClass == null) return false;
        Method isBook;
        try {
            isBook = itemClass.getMethod("isBook", ItemStack.class);
        } catch (NoSuchMethodException e) {
            return false;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && isBookStack(isBook, stack)) return true;
        }
        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty() && isBookStack(isBook, offhand)) return true;
        return false;
    }

    private static boolean isBookStack(Method isBook, ItemStack stack) {
        try {
            return Boolean.TRUE.equals(isBook.invoke(null, stack));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.biologyDictionaryTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> eventClass = ClassCache.resolve(ScreenClasses.BIOLOGY_DICTIONARY_EVENT);
            if (eventClass == null) return;
            Method openBookScreen = eventClass.getMethod("openBookScreen", Minecraft.class);
            openBookScreen.invoke(null, Minecraft.getInstance());
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Biology Dictionary screen: " + e.getMessage());
        }
    }
}
