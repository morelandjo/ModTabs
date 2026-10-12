package vodmordia.modtabs.client.tabs_menu;

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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * SDM Shop. Mirrors the mod's keybind path: {@code SDMShopClient.openGui(Identifier)} with the
 * {@code server:auto_shop_open} sentinel, which round-trips through the server before opening the
 * FTB Library-wrapped shop screen.
 */
@TabConfig(configKey = "sdmShopTab", defaultEnabled = true, defaultOrder = 0)
public final class SdmShopTab extends SimpleItemTab {
    private static final String CLIENT = "net.sixik.sdmshop.client.SDMShopClient";
    private static final String WRAPPER = "dev.ftb.mods.ftblibrary.ui.ScreenWrapper";

    private static Field wrappedGuiField;
    private static boolean reflectFailed;

    public SdmShopTab() {
        super(() -> new ItemStack(Items.EMERALD));
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (!player.getWorld().isClient) return;
        try {
            Class<?> client = Class.forName(CLIENT);
            Method openGui = client.getMethod("openGui", Identifier.class);
            openGui.invoke(null, new Identifier("server", "auto_shop_open"));
        } catch (ReflectiveOperationException e) {
            ModTabs.LOGGER.warn("Failed to open SDM Shop: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.sdmShopTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.SDM_SHOP);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen == null || !WRAPPER.equals(currentScreen.getClass().getName()) || reflectFailed) return false;
        try {
            if (wrappedGuiField == null) {
                wrappedGuiField = currentScreen.getClass().getDeclaredField("wrappedGui");
                wrappedGuiField.setAccessible(true);
            }
            Object wrapped = wrappedGuiField.get(currentScreen);
            return wrapped != null && wrapped.getClass().getName().startsWith("net.sixik.sdmshop.");
        } catch (ReflectiveOperationException e) {
            reflectFailed = true;
            return false;
        }
    }

    @Override
    public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.sdm_shop.description");
    }

    @Override
    public void initTabOnScreens() {
        if (!ModIntegrationManager.isModLoaded(ModIntegration.SDM_SHOP)) return;
        // FTB Quests / FTB Teams own the shared FTB Library wrapper when installed; only claim it as a fallback.
        if (ModIntegrationManager.isModLoaded(ModIntegration.FTB_QUESTS)
                || ModIntegrationManager.isModLoaded(ModIntegration.FTB_TEAMS)) return;
        ScreenRegistry.builder().withStandardDimensions().inverted().registerAllTabs(WRAPPER);
    }
}
