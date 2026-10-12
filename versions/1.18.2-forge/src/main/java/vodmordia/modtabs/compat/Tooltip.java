package vodmordia.modtabs.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * 1.18.2 shim for 1.20's {@code net.minecraft.client.gui.components.Tooltip} plus the
 * {@code Screen.setTooltipForNextRenderPass} deferral.
 *
 * <p>Widgets register their tooltip during their render pass ({@link #setForNextRenderPass}); the
 * pending tooltip is drawn once, on top of everything, from {@code ScreenEvent.DrawScreenEvent.Post}
 * (see ClientNeoForgeEvents), mirroring 1.20's {@code Screen.renderWithTooltip}.
 */
public final class Tooltip {
    /** Same wrap width as 1.20's Tooltip.splitTooltip. */
    private static final int MAX_WIDTH = 170;

    private static Tooltip pending;

    private final Component message;

    private Tooltip(Component message) {
        this.message = message;
    }

    public static Tooltip create(Component message) {
        return new Tooltip(message);
    }

    public Component getMessage() {
        return message;
    }

    public List<FormattedCharSequence> toCharSequence(Minecraft mc) {
        return mc.font.split(message, MAX_WIDTH);
    }

    public static void setForNextRenderPass(Tooltip tooltip) {
        pending = tooltip;
    }

    public static void clearPending() {
        pending = null;
    }

    /** Draw (and clear) the tooltip registered during this frame's render pass. */
    public static void renderPending(Screen screen, PoseStack pose, int mouseX, int mouseY) {
        Tooltip t = pending;
        pending = null;
        if (t == null || t.message == null) return;
        screen.renderTooltip(pose, t.toCharSequence(Minecraft.getInstance()), mouseX, mouseY);
    }
}
