package vodmordia.modtabs.compat;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 1.18.2 shim for the subset of 1.20's {@code net.minecraft.client.gui.GuiGraphics} that ModTabs uses.
 *
 * <p>1.18.2 has no GuiGraphics: rendering goes through a {@link PoseStack} plus the static
 * {@link GuiComponent} helpers. Wrapping the PoseStack keeps the ported render code identical to the
 * 1.20.1 tree — only the place where the instance is created differs ({@code new GuiGraphics(poseStack)}).
 */
public class GuiGraphics {
    /** Active scissor rectangles in GUI coordinates (x1, y1, x2, y2), innermost last. */
    private static final Deque<int[]> SCISSOR_STACK = new ArrayDeque<>();

    private final PoseStack pose;

    public GuiGraphics(PoseStack pose) {
        this.pose = pose;
    }

    public PoseStack pose() {
        return pose;
    }

    public void fill(int x1, int y1, int x2, int y2, int color) {
        GuiComponent.fill(pose, x1, y1, x2, y2, color);
    }

    // 1.20 GuiGraphics.drawString defaults to a drop shadow.
    public int drawString(Font font, String text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, String text, int x, int y, int color, boolean shadow) {
        if (text == null) return 0;
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }

    public int drawString(Font font, Component text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, Component text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color) {
        return drawString(font, text, x, y, color, true);
    }

    public int drawString(Font font, FormattedCharSequence text, int x, int y, int color, boolean shadow) {
        return shadow ? font.drawShadow(pose, text, x, y, color) : font.draw(pose, text, x, y, color);
    }

    /** blit from a 256x256 texture sheet. */
    public void blit(ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        blit(texture, x, y, u, v, width, height, 256, 256);
    }

    public void blit(ResourceLocation texture, int x, int y, float u, float v, int width, int height, int texWidth, int texHeight) {
        // Same state handling as 1.20's GuiGraphics.innerBlit: bind texture + shader, no blend change.
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(pose, x, y, u, v, width, height, texWidth, texHeight);
    }

    /**
     * Render a GUI item honouring this pose (1.18.2's ItemRenderer only reads the global model-view
     * stack, so the current pose is pushed onto it for the call).
     */
    public void renderItem(ItemStack stack, int x, int y) {
        if (stack == null || stack.isEmpty()) return;
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.mulPoseMatrix(pose.last().pose());
        RenderSystem.applyModelViewMatrix();
        try {
            // Like 1.20's renderItem(stack, x, y): uses the local player for model overrides, no decorations.
            itemRenderer.renderAndDecorateItem(stack, x, y);
        } finally {
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    /** Scissor in GUI coordinates; nested calls intersect like 1.20's GuiGraphics scissor stack. */
    public void enableScissor(int x1, int y1, int x2, int y2) {
        int[] rect = {x1, y1, x2, y2};
        int[] parent = SCISSOR_STACK.peekLast();
        if (parent != null) {
            rect = new int[]{Math.max(x1, parent[0]), Math.max(y1, parent[1]),
                    Math.min(x2, parent[2]), Math.min(y2, parent[3])};
            if (rect[2] < rect[0]) rect[2] = rect[0];
            if (rect[3] < rect[1]) rect[3] = rect[1];
        }
        SCISSOR_STACK.addLast(rect);
        applyScissor(rect);
    }

    public void disableScissor() {
        SCISSOR_STACK.pollLast();
        int[] parent = SCISSOR_STACK.peekLast();
        if (parent != null) {
            applyScissor(parent);
        } else {
            RenderSystem.disableScissor();
        }
    }

    private static void applyScissor(int[] r) {
        Window window = Minecraft.getInstance().getWindow();
        double scale = window.getGuiScale();
        int x = (int) (r[0] * scale);
        int y = (int) (window.getHeight() - r[3] * scale);
        int w = Math.max(0, (int) ((r[2] - r[0]) * scale));
        int h = Math.max(0, (int) ((r[3] - r[1]) * scale));
        RenderSystem.enableScissor(x, y, w, h);
    }

    /** 1.18.2 GUI rendering is immediate; just flush any pending batched buffers. */
    public void flush() {
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
    }
}
