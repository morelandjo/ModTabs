package vodmordia.modtabs.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Client → server request to open the player's own Reliable Backpack (Forge 1.20.1).
 *
 * Plain message class for the SimpleChannel (mirrors {@link OpenQuarkBackpackPayload}).
 * No fields — the server uses the sender to locate the backpack.
 */
public class OpenReliableBackpackPayload {

    public OpenReliableBackpackPayload() {
    }

    public static void encode(OpenReliableBackpackPayload msg, FriendlyByteBuf buf) {
        // No payload fields.
    }

    public static OpenReliableBackpackPayload decode(FriendlyByteBuf buf) {
        return new OpenReliableBackpackPayload();
    }
}
