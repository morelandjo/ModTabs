package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.SimpleTextureTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

/**
 * Truly Best Friends Forever. Opens the main pet management screen (not the radial team
 * wheel) the same way the mod's open-pet-screen keybind does: the client-only static
 * {@code ClientEvents.openPetTab()}, which does {@code new TrulyScreen(...)}.
 *
 * <p>The mod can also register an L2Tabs "Pet Companions" tab on TrulyScreen; FabricScreenEvents
 * already strips L2 tab widgets from every screen when L2 is present, so no duplicate bar appears.
 */
@TabConfig(configKey = "trulyBestFriendsTab", defaultEnabled = true, defaultOrder = 0)
public final class TrulyBestFriendsTab extends SimpleTextureTab {
    private static final String SCREEN = "com.whidte.trulybestfriends.tab.TrulyScreen";
    private static final String OPENER = "com.whidte.trulybestfriends.client.ClientEvents";

    public TrulyBestFriendsTab() {
        // Bottle icon shipped by the mod (asset namespace is truly_best_friends, not the mod id).
        super(new Identifier("truly_best_friends", "textures/gui/world_in_a_bottle.png"));
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (!player.getWorld().isClient) return;
        try {
            Class.forName(OPENER).getMethod("openPetTab").invoke(null);
        } catch (ReflectiveOperationException e) {
            ModTabs.LOGGER.warn("Failed to open Truly Best Friends screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.trulyBestFriendsTabEnabled
                && ModIntegrationManager.isModLoaded(ModIntegration.TRULY_BEST_FRIENDS);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return currentScreen != null && SCREEN.equals(currentScreen.getClass().getName());
    }

    @Override
    public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.truly_best_friends.description");
    }

    @Override
    public void initTabOnScreens() {
        // TrulyScreen is a full-screen Screen (not a handled container), so anchor to the top.
        ScreenRegistry.builder().withStandardDimensions().inverted().atTop().registerAllTabs(SCREEN);
    }
}
