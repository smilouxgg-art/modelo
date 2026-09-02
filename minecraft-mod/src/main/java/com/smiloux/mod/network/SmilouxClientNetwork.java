package com.smiloux.mod.network;

import com.smiloux.mod.client.SmilouxClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class SmilouxClientNetwork {
    private SmilouxClientNetwork() {}

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(PlaySongPayload.ID, (payload, context) ->
            context.client().execute(() -> SmilouxClient.ENGINE.playRemote(payload.songUrl(), payload.pos()))
        );
    }

    public static void requestFromBlock(net.minecraft.util.math.BlockPos pos, String url) {
        ClientPlayNetworking.send(new PlaySongPayload(pos, url));
    }
}
