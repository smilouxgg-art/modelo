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
                if (sender.getWorld().getRegistryKey() != context.server().getOverworld().getRegistryKey()
                    && sender.getServerWorld() == null) return;
                if (sender.squaredDistanceTo(payload.pos().getX() + 0.5, payload.pos().getY() + 0.5, payload.pos().getZ() + 0.5) > 16 * 16) return;
                for (ServerPlayerEntity player : context.server().getPlayerManager().getPlayerList()) {
                    if (player.getServerWorld() != sender.getServerWorld()) continue;
                    if (player.squaredDistanceTo(payload.pos().getX() + 0.5, payload.pos().getY() + 0.5, payload.pos().getZ() + 0.5) <= 32 * 32) {
                        ServerPlayNetworking.send(player, payload);
                    }
                }
            });
        });
    }
}
