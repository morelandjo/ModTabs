package vodmordia.modtabs.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * 1.18.2 base for ModTabs edit boxes exposing the 1.20 widget API the ported code uses:
 * {@code renderWidget(GuiGraphics, ...)}, {@code getX/getY/setX/setY}, {@code setTooltip(Tooltip)}
 * and a public {@code setFocused(boolean)}.
 */
public class CompatEditBox extends EditBox {
    private Tooltip tooltip;

    public CompatEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }

    public int getX() { return this.x; }
    public int getY() { return this.y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    public void setTooltip(Tooltip tooltip) { this.tooltip = tooltip; }
    public Tooltip getTooltip() { return this.tooltip; }

    public boolean isHovered() { return this.isHovered; }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        super.render(pose, mouseX, mouseY, partialTick);
        if (this.visible && this.tooltip != null && this.isHovered) {
            Tooltip.setForNextRenderPass(this.tooltip);
        }
    }

    @Override
    public final void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        renderWidget(new GuiGraphics(pose), mouseX, mouseY, partialTick);
    }

    /** 1.20-style render hook; the default draws the vanilla edit box. */
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        super.renderButton(gui.pose(), mouseX, mouseY, partialTick);
    }
}
