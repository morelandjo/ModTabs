package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.SimpleTextureTab;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.config.ModTabsConfig;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.integration.ModIntegrationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Questlog (Infernal Studios). Opens {@code new QuestlogScreen(previousScreen)} like the mod's
 * keybind, and mirrors the native button's notification badge (recent notification quest, or any
 * completed-but-unrewarded quest). The native inventory button is removed by QuestlogInventoryScreenMixin.
 */
@TabConfig(configKey = "questLogTab", defaultEnabled = true, defaultOrder = 0)
public final class QuestLogTab extends SimpleTextureTab {
    private static final String SCREEN = "org.infernalstudios.questlog.client.gui.screen.QuestlogScreen";
    private static final String CLIENT_EVENTS = "org.infernalstudios.questlog.QuestlogClientEvents";
    private static final String CLIENT = "org.infernalstudios.questlog.QuestlogClient";

    private static final Identifier BADGE_TEXTURE = new Identifier("questlog", "textures/gui/quest_peripherals.png");

    public QuestLogTab() {
        super(new Identifier(ModTabs.MOD_ID, "textures/gui/questlog.png"));
    }

    @Override
    public void openTargetScreen(PlayerEntity player) {
        if (!player.getWorld().isClient) return;
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            Object screen = Class.forName(SCREEN).getConstructor(Screen.class).newInstance(mc.currentScreen);
            mc.setScreen((Screen) screen);
        } catch (ReflectiveOperationException e) {
            ModTabs.LOGGER.warn("Failed to open Questlog: " + e.getMessage());
        }
    }

    @Override
    public boolean isEnabled(PlayerEntity player) {
        return ModTabsConfig.questLogTabEnabled && ModIntegrationManager.isModLoaded(ModIntegration.QUEST_LOG);
    }

    @Override
    public boolean isCurrentlyUsed(Screen currentScreen) {
        return currentScreen != null && SCREEN.equals(currentScreen.getClass().getName());
    }

    @Override
    public Text getTooltip() {
        return Text.translatable("tooltip.modtabs.tab.quest_log.description");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.builder().withStandardDimensions().registerAllTabs(SCREEN);
    }

    @Override
    public void render(DrawContext gui, int x, int y, boolean hover) {
        super.render(gui, x, y, hover);
        if (shouldShowBadge()) drawBadge(gui, x, y);
    }

    @Override
    protected void renderInverted(DrawContext gui, int x, int y, boolean hover) {
        super.renderInverted(gui, x, y, hover);
        if (shouldShowBadge()) drawBadge(gui, x, y);
    }

    private static boolean shouldShowBadge() {
        try {
            Field f = Class.forName(CLIENT_EVENTS).getField("mostRecentNotificationQuest");
            if (f.get(null) != null) return true;
        } catch (ReflectiveOperationException ignored) {
        }
        return hasCompletedUnrewardedQuest();
    }

    private static boolean hasCompletedUnrewardedQuest() {
        try {
            if (MinecraftClient.getInstance().player == null) return false;
            Object manager = Class.forName(CLIENT).getMethod("getLocal").invoke(null);
            if (manager == null) return false;
            Object quests = manager.getClass().getMethod("getAllQuests").invoke(manager);
            if (!(quests instanceof Iterable<?> iter)) return false;
            for (Object quest : iter) {
                if (quest == null) continue;
                Method isCompleted = quest.getClass().getMethod("isCompleted");
                Method isRewarded = quest.getClass().getMethod("isRewarded");
                if (Boolean.TRUE.equals(isCompleted.invoke(quest)) && Boolean.FALSE.equals(isRewarded.invoke(quest))) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static void drawBadge(DrawContext gui, int tabX, int tabY) {
        int bx = tabX + TabBase.TAB_WIDTH - 16 + 4;
        int by = tabY - 6;
        float bob = (float) Math.sin((System.currentTimeMillis() / 50.0F) * 0.2F) * 1.5F;
        gui.getMatrices().push();
        gui.getMatrices().translate(0, bob, 0);
        gui.drawTexture(BADGE_TEXTURE, bx, by, 8, 12, 16, 16, 256, 256);
        gui.getMatrices().pop();
    }
}
