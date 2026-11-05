package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

/**
 * Tab for Truly Best Friends Forever. Opens the main pet management screen (not the radial
 * team wheel) exactly like the mod's own "open pet tab" keybind: a purely client-side
 * {@code new TrulyScreen(Component.translatable("tab.trulybestfriends.pets"))}, which is
 * what the static {@code openPetTab()} entrypoint does, so we call that.
 *
 * <p>The mod can also register an L2 Library (L2Tabs) "Pet Companions" tab; ModTabs already
 * strips L2 tab widgets from every screen when L2 is present, so no duplicate tab bar appears.
 */
@TabConfig(configKey = "trulyBestFriendsTab", defaultEnabled = true, defaultOrder = 0)
public class TrulyBestFriendsTab extends IntegrationIconTab {
    // 16x16 bottle icon shipped by the mod (note the asset namespace differs from the mod id).
    private static final ResourceLocation ICON =
            new ResourceLocation("truly_best_friends", "textures/gui/world_in_a_bottle.png");

    // TrulyScreen extends Screen (full-screen, not a container), so anchor the bar to the top.
    private static final TabSpec SPEC = new TabSpec(
            "trulyBestFriendsTab",
            ModIntegration.TRULY_BEST_FRIENDS,
            () -> Config.Baked.trulyBestFriendsTabEnabled,
            "trulyBestFriends",
            "truly_best_friends",
            TabSpec.Layout.invertedTop(),
            new String[] { ScreenClasses.TRULY_BEST_FRIENDS_SCREEN },
            new String[] { ScreenClasses.TRULY_BEST_FRIENDS_SCREEN }
    );

    public TrulyBestFriendsTab() {
        super(SPEC, ICON, Config.Baked.trulyBestFriendsTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.trulyBestFriendsTabEnabled || !player.level().isClientSide) {
            return;
        }
        try {
            Class<?> opener = ClassCache.resolve(ScreenClasses.TRULY_BEST_FRIENDS_OPENER);
            opener.getMethod("openPetTab").invoke(null);
        } catch (Exception e) {
            ModTabs.LOGGER.error("Error opening Truly Best Friends pet screen", e);
        }
    }
}
