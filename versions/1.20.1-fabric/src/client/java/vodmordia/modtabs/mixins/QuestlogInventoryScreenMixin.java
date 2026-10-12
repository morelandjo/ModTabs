package vodmordia.modtabs.mixins;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Removes Questlog's inventory open button (the tab replaces it). Mixin cannot target Questlog's own
 * mixin, so we also inject at the tail of {@code InventoryScreen.init()} and remove the button by class name.
 */
// Priority above the default (1000) so this TAIL callback runs after Questlog's own TAIL injection adds the button.
@Mixin(value = InventoryScreen.class, priority = 2000)
public abstract class QuestlogInventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    private static final String QUESTLOG_BUTTON_FQN =
            "org.infernalstudios.questlog.client.gui.components.QuestlogOpenButton";

    public QuestlogInventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        throw new UnsupportedOperationException("Mixin constructor");
    }

    @Inject(method = "init()V", at = @At("TAIL"))
    private void modtabs$removeQuestlogButton(CallbackInfo ci) {
        List<Element> toRemove = null;
        for (Element child : this.children()) {
            if (child != null && QUESTLOG_BUTTON_FQN.equals(child.getClass().getName())) {
                if (toRemove == null) toRemove = new ArrayList<>(1);
                toRemove.add(child);
            }
        }
        if (toRemove != null) {
            for (Element w : toRemove) this.remove(w);
        }
    }
}
