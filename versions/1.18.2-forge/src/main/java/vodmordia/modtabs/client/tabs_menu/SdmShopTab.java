package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ConfigurableItemTab;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

/**
 * Tab for SDM Shop (1.18.2 Forge: "SDM ShopR" 5.x, mod id {@code sdmshop}). Mirrors the keybind
 * path in {@code SDMShopRClient.keyInput}: if {@code Shop.CLIENT != null} (shop synced from the
 * server), {@code new MainShopScreen().openGui()}. Done reflectively so there is no hard
 * dependency on SDM Shop / FTB Library.
 *
 * The shop screen is an FTB Library {@code BaseScreen} wrapped in
 * {@link ScreenClasses#FTB_LIBRARY_WRAPPER}. {@link #isCurrentlyUsed} inspects the wrapper's
 * {@code wrappedGui} field so cycling skips this tab only when the shop is actually open.
 *
 * SDM Shop 1.18.2 hard-requires FTB Library, FTB Teams and FTB Quests, so the FTB tabs would
 * normally claim the wrapper registration; this tab also registers it (registering identical
 * params for the same class is harmless - finalize keeps the first) so the bar shows on the
 * shop screen even before/without the FTB tabs being registered.
 */
@TabConfig(configKey = "sdmShopTab", defaultEnabled = true, defaultOrder = 0)
public class SdmShopTab extends ConfigurableItemTab {

    public SdmShopTab() {
        super(SdmShopTab::getIcon, Config.Baked.sdmShopTabCustomIcon, "sdmShop");
    }

    private static ItemStack getIcon() {
        return new ItemStack(Items.EMERALD);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.sdmShopTabEnabled || !player.level.isClientSide) return;
        try {
            Class<?> shopClass = ClassCache.resolve(ScreenClasses.SDM_SHOP_CLASS);
            Class<?> screenClass = ClassCache.resolve(ScreenClasses.SDM_SHOP_MAIN_SCREEN);
            if (shopClass == null || screenClass == null) return;
            // Same guard as the keybind: Shop.CLIENT is null until the server synced the shop.
            if (shopClass.getField("CLIENT").get(null) == null) return;
            Object screen = screenClass.getConstructor().newInstance();
            screenClass.getMethod("openGui").invoke(screen);
        } catch (Exception e) {
            ModTabs.LOGGER.debug("Error opening SDM Shop: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.sdmShopTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.SDM_SHOP);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null) return false;
        if (!ScreenClasses.FTB_LIBRARY_WRAPPER.equals(currentScreen.getClass().getName())) return false;
        String wrapped = FtbScreenWrapperUtil.getWrappedGuiClassName(currentScreen);
        return wrapped != null && wrapped.startsWith(ScreenClasses.SDM_SHOP_PACKAGE_PREFIX);
    }

    @Override
    public Component getTooltip() {
        return new TranslatableComponent("tooltip." + ModTabs.MOD_ID + ".tab.sdm_shop.description");
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.SDM_SHOP)) return;
        ScreenRegistry.builder()
                .withStandardDimensions()
                .inverted()
                .registerAllTabs(ScreenClasses.FTB_LIBRARY_WRAPPER);
    }
}
