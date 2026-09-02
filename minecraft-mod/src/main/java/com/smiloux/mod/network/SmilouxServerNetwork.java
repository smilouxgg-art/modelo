package com.smiloux.mod.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class SmilouxServerNetwork {
    private SmilouxServerNetwork() {}

    public static void register() {
        PayloadTypeRegistry.playC2S().register(PlaySongPayload.ID, PlaySongPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(PlaySongPayload.ID, PlaySongPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(PlaySongPayload.ID, (payload, context) -> {
            ServerPlayerEntity sender = context.player();
            context.server().execute(() -> {
                double sx = payload.pos().getX() + .5;
                double sy = payload.pos().getY() + .5;
                double sz = payload.pos().getZ() + .5;
                if (sender.squaredDistanceTo(sx, sy, sz) > 16 * 16) return;
                for (ServerPlayerEntity player : context.server().getPlayerManager().getPlayerList()) {
                    if (player.getWorld().getRegistryKey() != sender.getWorld().getRegistryKey()) continue;
                    if (player.squaredDistanceTo(sx, sy, sz) <= 32 * 32) ServerPlayNetworking.send(player, payload);
                }
            });
        });
    }
}
