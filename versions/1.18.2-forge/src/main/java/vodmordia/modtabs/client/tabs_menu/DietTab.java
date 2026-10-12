package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
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
 * Tab for Diet (illusivesoulworks, modid {@code diet}), 1.18.2 Forge build
 * ({@code top.theillusivec4.diet.client.DietScreen(boolean fromInventory)}).
 * Bulking has no 1.18.2 release, so there is no BulkingScreen swap here.
 */
@TabConfig(configKey = "dietTab", defaultEnabled = true, defaultOrder = 0)
public class DietTab extends IntegrationIconTab {

    // Vanilla apple texture as the default icon — fits the food/diet theme and avoids
    // shipping a bespoke PNG. Override via the dietTabCustomIcon config if you want.
    private static final ResourceLocation DIET_ICON =
            new ResourceLocation("minecraft", "textures/item/apple.png");

    private static final TabSpec SPEC = new TabSpec(
            "dietTab",
            ModIntegration.DIET,
            () -> Config.Baked.dietTabEnabled,
            "diet",
            "diet",
            new TabSpec.Layout(false, TabSpec.Layout.Dimensions.DIET),
            new String[] { ScreenClasses.DIET_SCREEN },
            new String[] { ScreenClasses.DIET_SCREEN }
    );

    public DietTab() {
        super(SPEC, DIET_ICON, Config.Baked.dietTabCustomIcon);
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.dietTabEnabled || !player.level.isClientSide) return;

        boolean fromInventory = Minecraft.getInstance().screen instanceof InventoryScreen;
        String screenFqn = ScreenClasses.DIET_SCREEN;

        try {
            Class<?> screenClass = ClassCache.resolve(screenFqn);
            if (screenClass == null) return;
            Screen screen = (Screen) screenClass
                    .getDeclaredConstructor(boolean.class)
                    .newInstance(fromInventory);
            Minecraft.getInstance().setScreen(screen);
        } catch (Exception e) {
            ModTabs.LOGGER.error("Error opening Diet/Bulking screen", e);
        }
    }
}
