package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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

/**
 * Tab for Skatric's Item Collector (modid {@code itemcollector}, NOT Serilum's "Item Collectors").
 * Mirrors the mod's own open path (keybind B / {@code /collection}):
 * {@code Minecraft.setScreen(new CollectionBookScreen())}. The screen has a public no-arg
 * constructor and is a plain {@link Screen} subclass.
 */
@TabConfig(configKey = "collectionBookTab", defaultEnabled = true, defaultOrder = 0)
public class CollectionBookTab extends IntegrationItemTab {

    private static final TabSpec SPEC = new TabSpec(
            "collectionBookTab",
            ModIntegration.ITEM_COLLECTOR,
            () -> Config.Baked.collectionBookTabEnabled,
            "collectionBook",
            "collection_book",
            TabSpec.Layout.invertedTop(),
            new String[] { ScreenClasses.COLLECTION_BOOK_SCREEN },
            new String[] { ScreenClasses.COLLECTION_BOOK_SCREEN }
    );

    public CollectionBookTab() {
        super(SPEC, CollectionBookTab::getIcon, Config.Baked.collectionBookTabCustomIcon);
    }

    private static ItemStack getIcon() {
        return new ItemStack(Items.WRITTEN_BOOK);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.collectionBookTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> screenClass = ClassCache.resolve(ScreenClasses.COLLECTION_BOOK_SCREEN);
            if (screenClass == null) return;
            Minecraft.getInstance().setScreen((Screen) screenClass.getConstructor().newInstance());
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Item Collector's Collection Book: " + e.getMessage());
        }
    }
}
