package vodmordia.modtabs.client.tabs_menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import vodmordia.modtabs.ModTabs;
import vodmordia.modtabs.api.tabs_menu.IntegrationItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabSpec;
import vodmordia.modtabs.config.Config;
import vodmordia.modtabs.integration.ModIntegration;
import vodmordia.modtabs.network.OpenReliableBackpackPayload;
import vodmordia.modtabs.utils.ClassCache;
import vodmordia.modtabs.utils.ScreenClasses;

/**
 * Tab for the Reliable Backpacks mod (Forge 1.20.1). Visible whenever the player carries a
 * backpack. Clicking sends an {@link OpenReliableBackpackPayload}; the server opens the
 * backpack with the mod's own container and menu classes.
 *
 * The mod ships no open-from-self path (only "interact with another player from behind"),
 * so this tab replicates its server-side open logic in our own packet handler.
 */
@TabConfig(configKey = "reliableBackpacksTab", defaultEnabled = true, defaultOrder = 0)
public class ReliableBackpackTab extends IntegrationItemTab {
    private static final ResourceLocation BACKPACK_ITEM_ID =
            new ResourceLocation("reliable_backpacks", "backpack");

    private static final TabSpec SPEC = TabSpec.withoutCurrentScreen(
            "reliableBackpacksTab",
            ModIntegration.RELIABLE_BACKPACKS,
            () -> Config.Baked.reliableBackpacksTabEnabled,
            "reliableBackpacks",
            "reliable_backpacks",
            TabSpec.Layout.guiRelative()
    );

    public ReliableBackpackTab() {
        super(SPEC, ReliableBackpackTab::getBackpackIcon, Config.Baked.reliableBackpacksTabCustomIcon);
    }

    private static ItemStack getBackpackIcon() {
        Item item = BuiltInRegistries.ITEM.get(BACKPACK_ITEM_ID);
        if (item != null && item != Items.AIR) {
            return new ItemStack(item);
        }
        return new ItemStack(Items.LEATHER);
    }

    @Override
    public boolean isEnabled(Player player) {
        return super.isEnabled(player) && hasBackpack(player);
    }

    /** Visible when a backpack is anywhere on the player (inventory, armor or offhand). */
    private static boolean hasBackpack(Player player) {
        Class<?> itemClass = ClassCache.resolve(ScreenClasses.RELIABLE_BACKPACKS_ITEM);
        if (itemClass == null) return false;

        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) return true;
        }
        ItemStack offhand = player.getOffhandItem();
        return !offhand.isEmpty() && itemClass.isInstance(offhand.getItem());
    }

    @Override
    public void openTargetScreen(Player player) {
        if (!Config.Baked.reliableBackpacksTabEnabled || !player.level().isClientSide) return;
        ModTabs.CHANNEL.sendToServer(new OpenReliableBackpackPayload());
    }
}
