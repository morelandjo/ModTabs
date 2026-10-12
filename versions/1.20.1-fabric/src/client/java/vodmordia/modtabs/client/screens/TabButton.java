package vodmordia.modtabs.client.screens;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import vodmordia.modtabs.api.tabs_menu.TabBase;
import vodmordia.modtabs.api.tabs_menu.TabDisplayMode;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;

import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_HEIGHT;
import static vodmordia.modtabs.api.tabs_menu.TabBase.TAB_WIDTH;

public class TabButton extends ButtonWidget {
    public int tabPositionIndex;
    public TabBase tabBase;
    public PlayerEntity player;
    public Screen screen;
    public boolean isDisabled;
    public TabDisplayMode displayMode;
    /** Featured tabs draw (and hit-test) at featuredTabScale% of the normal size. Refreshed in setTabBase. */
    private boolean featured;

    public TabButton(TabBase tabBase, PlayerEntity player, Screen screen, int tabPositionIndex, int leftScreenPos, int topScreenPos, TabDisplayMode displayMode) {
        super(leftScreenPos + TabsMenu.slotOffset(tabPositionIndex),
              calculateYPosition(topScreenPos, displayMode, TAB_HEIGHT),
              TAB_WIDTH, TAB_HEIGHT, Text.empty(), button -> {}, DEFAULT_NARRATION_SUPPLIER);

        this.tabPositionIndex = tabPositionIndex;
        this.player = player;
        this.screen = screen;
        this.displayMode = displayMode;
        this.setTabBase(tabBase);
        if (this.featured) {
            // Featured height is only known once the tab is assigned.
            setY(calculateYPosition(topScreenPos, displayMode, this.height));
        }
    }

    private static int calculateYPosition(int topScreenPos, TabDisplayMode displayMode, int tabHeight) {
        // Adjust Y position for inverted tabs - they hang down instead of up
        return displayMode == TabDisplayMode.INVERTED ?
            topScreenPos :
            topScreenPos - tabHeight;
    }

    @Override
    public void onPress() {
        super.onPress();
        if (!this.isDisabled) {
            TabsMenu.markScreenOpenedViaTab(this.screen);
            tabBase.openTargetScreen(this.player);
        }
    }

    public void setTabBase(TabBase tabBase) {
        this.tabBase = tabBase;
        this.featured = tabBase.isFeatured();
        this.width = TabsMenu.tabWidth(this.featured);
        this.height = TabsMenu.tabHeight(this.featured);
        TabsMenu.setSlotFeatured(this.tabPositionIndex, this.featured);
        this.isDisabled = this.tabBase.isCurrentlyUsed(this.screen);
        setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(this.tabBase.getTooltip()));
    }

    @Override
    public void renderButton(DrawContext gui, int mouseX, int mouseY, float partial) {
        // Apply animation offset for tuck mode
        int animatedY = this.getY() + TabsMenu.getAnimatedYOffset();

        // Check if mouse is over the animated position
        boolean isMouseOverAnimated = mouseX >= this.getX() && mouseX < this.getX() + this.width &&
                                     mouseY >= animatedY && mouseY < animatedY + this.height;
        boolean hover = this.isDisabled || isMouseOverAnimated;

        // Render the tab icon/graphics (featured tabs are scaled about their top-left corner)
        if (this.featured) {
            float scale = TabsMenu.featuredScaleFactor();
            gui.getMatrices().push();
            gui.getMatrices().translate(this.getX(), animatedY, 0);
            gui.getMatrices().scale(scale, scale, 1.0f);
            this.tabBase.render(gui, 0, 0, hover, this.displayMode);
            gui.getMatrices().pop();
        } else {
            this.tabBase.render(gui, this.getX(), animatedY, hover, this.displayMode);
        }
    }

    // Provide renderWidget for compatibility with FabricScreenEvents
    public void renderWidget(DrawContext gui, int mouseX, int mouseY, float partial) {
        renderButton(gui, mouseX, mouseY, partial);
    }

    public void updatePosition(int leftScreenPos, int topScreenPos) {
        int finalX = leftScreenPos + TabsMenu.slotOffset(tabPositionIndex);
        setX(finalX);
        setY(calculateYPosition(topScreenPos, displayMode, TabsMenu.tabHeight(featured)));
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        // Override to account for animation offset in tuck mode
        int animatedY = this.getY() + TabsMenu.getAnimatedYOffset();
        return mouseX >= this.getX() && mouseX < this.getX() + this.width &&
               mouseY >= animatedY && mouseY < animatedY + this.height;
    }
}