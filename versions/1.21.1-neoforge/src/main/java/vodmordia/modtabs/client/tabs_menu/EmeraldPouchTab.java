package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Tab for Emerald Pouch (mod id {@code emeraldpouch}). Visible while the player has a pouch
 * (inventory or Curios/Accessories belt slot). Clicking does what the mod's open-first-pouch
 * keybind does, via the mod's own client entry point, and the bar renders on {@code PouchScreen}.
 */
@TabConfig(configKey = "emeraldPouchTab", defaultEnabled = true, defaultOrder = 0)
public class EmeraldPouchTab extends IntegrationItemTab {
    private static final ResourceLocation POUCH_ITEM_ID = ResourceLocation.fromNamespaceAndPath("emeraldpouch", "pouch");

    private static final TabSpec SPEC = new TabSpec(
            "emeraldPouchTab",
            ModIntegration.EMERALD_POUCH,
            () -> Config.Baked.emeraldPouchTabEnabled,
            "emeraldPouch",
            "emerald_pouch",
            TabSpec.Layout.guiRelative(),
            new String[] { ScreenClasses.EMERALD_POUCH_SCREEN },
            new String[] { ScreenClasses.EMERALD_POUCH_SCREEN }
    );

    public EmeraldPouchTab() {
        super(SPEC, EmeraldPouchTab::getPouchIcon, Config.Baked.emeraldPouchTabCustomIcon);
    }

    private static ItemStack getPouchIcon() {
        Item item = BuiltInRegistries.ITEM.get(POUCH_ITEM_ID);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        return new ItemStack(Items.EMERALD);
    }

    @Override
    public boolean isEnabled(Player player) {
        return super.isEnabled(player) && hasPouch(player);
    }

    /**
     * Uses the mod's public API (inventory, then the Curios/Accessories belt slot) so the
     * tab is visible exactly when the N keybind would open a pouch.
     */
    private static boolean hasPouch(Player player) {
        try {
            Class<?> api = ClassCache.resolve(ScreenClasses.EMERALD_POUCH_API);
            if (api == null) return false;
            Method find = api.getMethod("findFirstPlayerPouch", net.minecraft.world.entity.player.Inventory.class);
            Object result = find.invoke(null, player.getInventory());
            return result instanceof Optional<?> opt && opt.isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    /** Same call the mod's own keybind makes; the server opens the first pouch found. */
    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.emeraldPouchTabEnabled || !player.level().isClientSide) return;
        try {
            Class<?> networking = ClassCache.resolve(ScreenClasses.EMERALD_POUCH_CLIENT_NETWORKING);
            if (networking == null) return;
            networking.getMethod("sendOpenFirstPouch").invoke(null);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening Emerald Pouch: " + e.getMessage());
        }
    }

    @Override
    public void initTabOnScreens() {
        // Pouch GUI height depends on the pouch size (rows), so track the real GUI box.
        ScreenRegistry.builder()
                .withStandardDimensions()
                .withActualGeometry()
                .registerAllTabs(ScreenClasses.EMERALD_POUCH_SCREEN);
    }
}
