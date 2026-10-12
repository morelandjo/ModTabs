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
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.utils.ClassCache;

/** Opens Kindred's companion roster screen, matching its open-roster keybind (client-side only). */
@TabConfig(configKey = "kindredTab", defaultEnabled = true, defaultOrder = 0)
public final class KindredTab extends IntegrationItemTab {
    private static final String ROSTER_SCREEN = "net.silvertide.kindred.client.screen.RosterScreen";

    private static final TabSpec SPEC = new TabSpec(
            "kindredTab", ModIntegration.KINDRED, () -> ModTabsConfig.kindredTabEnabled,
            "kindred", "kindred", TabSpec.Layout.guiRelative(),
            new String[]{ROSTER_SCREEN}, new String[]{ROSTER_SCREEN});

    public KindredTab() {
        super(SPEC, () -> new ItemStack(Items.LEAD), ModTabsConfig.kindredTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        try {
            Class<?> type = ClassCache.resolve(ROSTER_SCREEN);
            if (type != null) Minecraft.getInstance().setScreen((Screen) type.getConstructor().newInstance());
        } catch (ReflectiveOperationException | LinkageError e) {
            ModTabs.LOGGER.warn("Failed to open Kindred roster screen", e);
        }
    }
}
