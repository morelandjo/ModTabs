package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementsScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.SimpleTextureTab;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

public class AdvancementsTab extends SimpleTextureTab {
    // Use the advancements widgets texture sprite sheet
    private static final Identifier ADVANCEMENTS_ICON = new Identifier("minecraft", "textures/gui/advancements/widgets.png");

    private static final String BETTER_ADVANCEMENTS_SCREEN = "betteradvancements.common.gui.BetterAdvancementsScreen";

    public AdvancementsTab() {
        super(ADVANCEMENTS_ICON);
    }

    @Override
    public void render(DrawContext gui, int x, int y, boolean hover) {
        // Render using custom UV coordinates: icon at 27,129 with size 26x26 from 256x256 sprite sheet
        // Display as 16x16 (scaled down from 26x26 source)
        vodmordia.modtabs.api.tabs_menu.TabRenderer.builder()
            .withBackground()
            .withTextureIcon(getIconTexture(), 5, 4, 16, 16, 27, 129, 26, 26, 256, 256)
            .render(gui, x, y, hover, false);
    }

    @Override
    protected void renderInverted(DrawContext gui, int x, int y, boolean hover) {
        // Render using custom UV coordinates for inverted tab
        vodmordia.modtabs.api.tabs_menu.TabRenderer.builder()
            .withBackground()
            .withTextureIcon(getIconTexture(), 5, 4, 16, 16, 27, 129, 26, 26, 256, 256)
            .render(gui, x, y, hover, true);
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (ModIntegrationManager.isModLoaded(ModIntegration.BETTER_ADVANCEMENTS)) {
            // Use the Better Advancements screen if the mod is loaded
            try {
                Class<?> betterScreenClass = Class.forName(BETTER_ADVANCEMENTS_SCREEN);
                Screen betterScreen = (Screen) betterScreenClass
                        .getConstructor(net.minecraft.client.network.ClientAdvancementManager.class)
                        .newInstance(minecraft.getNetworkHandler().getAdvancementHandler());
                minecraft.setScreen(betterScreen);
                return;
            } catch (Exception e) {
                // Fall back to vanilla if Better Advancements fails to load
            }
        }
        AdvancementsScreen newGui = new AdvancementsScreen(minecraft.getNetworkHandler().getAdvancementHandler());
        minecraft.setScreen(newGui);
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return Config.Baked.advancementsTabEnabled;
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen instanceof AdvancementsScreen) return true;
        return currentScreen != null && ModIntegrationManager.isModLoaded(ModIntegration.BETTER_ADVANCEMENTS)
                && currentScreen.getClass().getName().equals(BETTER_ADVANCEMENTS_SCREEN);
    }

    @Override
    public Text getTooltip() {
        return Text.translatable("tooltip." + ModTabs.MOD_ID + ".tab.advancements.description");
    }

    @Override
    public void initTabOnScreens() {
        // Register the advancements screen with tabs displayed inverted at the top
        ScreenRegistry.registerInvertedScreens(AdvancementsScreen.class);

        // Also register Better Advancements' screen (string-based to avoid class loading)
        if (ModIntegrationManager.isModLoaded(ModIntegration.BETTER_ADVANCEMENTS)) {
            ScreenRegistry.registerInvertedScreens(BETTER_ADVANCEMENTS_SCREEN);
        }
    }
}
