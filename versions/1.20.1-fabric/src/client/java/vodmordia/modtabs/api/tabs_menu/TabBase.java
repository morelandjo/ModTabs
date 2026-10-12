package vodmordia.modtabs.api.tabs_menu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerEntity;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.config.Config;
import net.minecraft.client.gui.screen.Screen;


public abstract class TabBase {
    public static final int TAB_HEIGHT = 22;
    public static final int TAB_WIDTH = 26;

    public TabBase() {
    }

    public abstract void openTargetScreen(PlayerEntity player);

    public abstract boolean isEnabled(PlayerEntity player);

    public abstract void initTabOnScreens();

    public abstract void render(DrawContext gui, int x, int y, boolean hover);

    public void render(DrawContext gui, int x, int y, boolean hover, TabDisplayMode displayMode) {
        if (displayMode == TabDisplayMode.INVERTED) {
            renderInverted(gui, x, y, hover);
        } else {
            render(gui, x, y, hover);
        }
    }

    protected void renderInverted(DrawContext gui, int x, int y, boolean hover) {
        // Default: call regular render() which subclasses override
        // Subclasses can override this method to handle inverted rendering differently
        // or they can use the invertedMode flag in their render logic
        this.invertedMode = true;
        render(gui, x, y, hover);
        this.invertedMode = false;
    }

    // Track if currently rendering in inverted mode
    private boolean invertedMode = false;

    protected boolean isInvertedMode() {
        return invertedMode;
    }

    /**
     * Get the tab renderer for this tab. Subclasses should override this.
     */
    protected TabRenderer getTabRenderer() {
        // Default renderer with just background
        return TabRenderer.builder()
                .withBackground();
    }

    /**
     * Helper method to render tab with icon
     */
    protected void renderWithIcon(DrawContext gui, int x, int y, boolean hover, net.minecraft.util.Identifier iconTexture) {
        TabRenderer.builder()
                .withBackground()
                .withTextureIcon(iconTexture, 5, 3, 16, 16)
                .render(gui, x, y, hover, isInvertedMode());
    }

    /**
     * Helper method to render tab with item stack
     */
    protected void renderWithItem(DrawContext gui, int x, int y, boolean hover, net.minecraft.item.ItemStack itemStack) {
        TabRenderer.builder()
                .withBackground()
                .withItemIcon(itemStack, 5, 3)
                .render(gui, x, y, hover, isInvertedMode());
    }


    public abstract boolean isCurrentlyUsed(Screen currentScreen);

    public abstract Text getTooltip();

    public int getOverrideOrder() {
        return TabRegistry.getTabOrder(this);
    }

    /**
     * Config-key prefix used for the {@code <key>Featured} flag on ModTabsConfig. Uses the
     * {@code @TabConfig} key when present, otherwise the class name with a lower-case first
     * letter (InventoryTab → inventoryTab). Override when the config name differs.
     */
    protected String featuredConfigKey() {
        TabConfig annotation = getClass().getAnnotation(TabConfig.class);
        if (annotation != null) return annotation.configKey();
        String name = getClass().getSimpleName();
        return name.isEmpty() ? name : Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * True when this tab is featured: it sorts ahead of non-featured tabs and renders at
     * {@code featuredTabScale}% of the normal tab size. Backed by {@code <key>Featured}.
     */
    public boolean isFeatured() {
        try {
            var field = vodmordia.modtabs.config.ModTabsConfig.class.getField(featuredConfigKey() + "Featured");
            return field.getType() == boolean.class && field.getBoolean(null);
        } catch (Exception e) {
            return false;
        }
    }
}