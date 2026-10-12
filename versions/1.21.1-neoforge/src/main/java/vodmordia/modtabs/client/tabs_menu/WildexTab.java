package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
 * Tab for the Wildex Bestiary mod. Shown only while the player has the
 * {@code wildex:wildex_book} item in their inventory; opening goes through
 * {@code WildexScreenOpener.open()} so we share the mod's sound/setScreen flow.
 */
@TabConfig(configKey = "wildexTab", defaultEnabled = true, defaultOrder = 0)
public class WildexTab extends IntegrationItemTab {
    private static final ResourceLocation WILDEX_BOOK_ID =
            ResourceLocation.fromNamespaceAndPath("wildex", "wildex_book");

    private static final TabSpec SPEC = new TabSpec(
            "wildexTab",
            ModIntegration.WILDEX,
            () -> Config.Baked.wildexTabEnabled,
            "wildex",
            "wildex",
            TabSpec.Layout.invertedTop(),
            new String[] { ScreenClasses.WILDEX_SCREEN },
            new String[] { ScreenClasses.WILDEX_SCREEN }
    );

    public WildexTab() {
        super(SPEC, WildexTab::getBookIcon, Config.Baked.wildexTabCustomIcon);
    }

    private static ItemStack getBookIcon() {
        Item item = BuiltInRegistries.ITEM.get(WILDEX_BOOK_ID);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        return new ItemStack(Items.WRITABLE_BOOK);
    }

    @Override
    public boolean isEnabled(Player player) {
        return super.isEnabled(player) && hasBook(player);
    }

    /**
     * The tab is shown only when the player actually carries a {@code wildex:wildex_book},
     * regardless of Wildex's {@code requireBookForKeybind} setting (a tab with no book
     * to open is just clutter). Matches on item type only so books with custom
     * components still count.
     */
    private static boolean hasBook(Player player) {
        Item book = BuiltInRegistries.ITEM.get(WILDEX_BOOK_ID);
        if (book == null || book == Items.AIR) return false;

        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(book)) return true;
        }
        return false;
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.wildexTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> openerClass = ClassCache.resolve(ScreenClasses.WILDEX_SCREEN_OPENER);
            if (openerClass == null) return;
            Method open = openerClass.getMethod("open");
            open.invoke(null);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Wildex screen: " + e.getMessage());
        }
    }
}
