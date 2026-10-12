package vodmordia.modtabs.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * 1.18.2 base for ModTabs buttons that keeps the 1.20 widget API the ported code is written against:
 * {@code renderWidget(GuiGraphics, ...)}, {@code getX/getY/setX/setY} and {@code setTooltip(Tooltip)}.
 */
public class CompatButton extends Button {
    private Tooltip tooltip;

    public CompatButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress);
    }

    public int getX() { return this.x; }
    public int getY() { return this.y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public void setTooltip(Tooltip tooltip) { this.tooltip = tooltip; }
    public Tooltip getTooltip() { return this.tooltip; }

    public boolean isHovered() { return this.isHovered; }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        super.render(pose, mouseX, mouseY, partialTick);
        // 1.20 AbstractWidget.updateTooltip: runs after renderWidget, reads isHovered.
        if (this.visible && this.tooltip != null && this.isHovered) {
            Tooltip.setForNextRenderPass(this.tooltip);
        }
    }

    @Override
    public final void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderWidget(new GuiGraphics(pose), mouseX, mouseY, partialTick);
    }

    /** 1.20-style render hook; the default draws the vanilla button. */
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.renderButton(gui.pose(), mouseX, mouseY, partialTick);
    }
}
