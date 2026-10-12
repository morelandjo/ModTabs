package vodmordia.modtabs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import vodmordia.modtabs.compat.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;

/**
 * Two unrelated tweaks for container screens:
 *
 * 1. While the layout editor is active, force {@code isHovering(Slot, ...)} to return
 *    false so vanilla doesn't draw slot-hover highlights or set {@code hoveredSlot} (which
 *    would render item tooltips on top of our overlay).
 * 2. Inject right before the GUI panel image draws in {@code render} so our tabs land
 *    under it. Edit mode skips this pass and renders tabs on top via the normal
 *    widget loop, since handles need to be reachable.
 */
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void modtabs$suppressSlotHoverWhileEditing(Slot slot, double mouseX, double mouseY,
                                                       CallbackInfoReturnable<Boolean> cir) {
        if (TabsMenu.isEditing(Minecraft.getInstance().screen)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderBg(Lcom/mojang/blaze3d/vertex/PoseStack;FII)V"))
    private void modtabs$drawTabsBehindPanel(PoseStack pose, int mouseX, int mouseY, float partialTick,
                                             CallbackInfo ci) {
        TabsMenu.renderTabsBehindPanel((AbstractContainerScreen<?>) (Object) this, new GuiGraphics(pose), mouseX, mouseY, partialTick);
    }
}
