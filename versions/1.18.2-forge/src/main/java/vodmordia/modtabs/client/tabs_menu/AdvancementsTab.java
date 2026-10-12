package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ConfigurableIconTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.config.Config;

@TabConfig(configKey = "advancementsTab", defaultEnabled = true, defaultOrder = 0)
public class AdvancementsTab extends ConfigurableIconTab {
    // 1.21+ moved advancement frame icons to /sprites/advancements/<frame>_obtained.png; on 1.20.1
    // those individual files don't exist (everything lived in textures/gui/advancements/widgets.png
    // as a sprite sheet). Use the vanilla knowledge book item icon instead — same theme, exists
    // as a single 16×16 file.
    private static final ResourceLocation ADVANCEMENTS_ICON = new ResourceLocation("minecraft", "textures/item/knowledge_book.png");

    public AdvancementsTab() {
        super(ADVANCEMENTS_ICON, Config.Baked.advancementsTabCustomIcon, "advancements");
    }

    @Override
    public void openTargetScreen(Player player) {
        // TODO(1.18.2 integrations): open BetterAdvancementsScreen when Better Advancements is ported.
        // Use vanilla advancements screen (fallback or default)
        AdvancementsScreen newGui = new AdvancementsScreen(Minecraft.getInstance().getConnection().getAdvancements());
        Minecraft.getInstance().setScreen(newGui);
    }

    @Override
    public boolean isEnabled(Player player) {
        return Config.Baked.advancementsTabEnabled;
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        if (currentScreen instanceof AdvancementsScreen) {
            return true;
        }
        // TODO(1.18.2 integrations): Better Advancements screen check.
        return false;
    }

    @Override
    public Component getTooltip() {
        return new net.minecraft.network.chat.TranslatableComponent("tooltip." + ModTabs.MOD_ID + ".tab.advancements.description");
    }

    @Override
    public void initTabOnScreens() {
        // Register the advancements screen with tabs displayed inverted at the top
        ScreenRegistry.registerInvertedScreens(AdvancementsScreen.class);

        // TODO(1.18.2 integrations): register the Better Advancements screen when ported.
    }
}