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
 * Jobs+ (DAQEM). JobsScreen needs server data, so mirror the mod's keybind: send an empty
 * {@code PacketOpenMenuC2S} (architectury message) and let the server reply with the screen.
 */
@TabConfig(configKey = "jobsPlusTab", defaultEnabled = true, defaultOrder = 0)
public final class JobsPlusTab extends SimpleTextureTab {
    private static final String SCREEN = "com.daqem.jobsplus.client.screen.JobsScreen";
    private static final String OPEN_PACKET = "com.daqem.jobsplus.networking.c2s.PacketOpenMenuC2S";

    public JobsPlusTab() {
        super(new Identifier(ModTabs.MOD_ID, "textures/gui/jobsplus.png"));
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (!player.getWorld().isClient) return;
        try {
            Object packet = Class.forName(OPEN_PACKET).getDeclaredConstructor().newInstance();
            packet.getClass().getMethod("sendToServer").invoke(packet);
        } catch (ReflectiveOperationException e) {
            ModTabs.LOGGER.warn("Failed to open Jobs+ screen: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.jobsPlusTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.JOBS_PLUS);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return currentScreen != null && SCREEN.equals(currentScreen.getClass().getName());
    }

    @Override
    public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.jobs_plus.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder().withStandardDimensions().registerAllTabs(SCREEN);
    }
}
