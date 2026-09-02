package com.smiloux.mod.network;

import com.smiloux.mod.SmilouxMod;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record PlaySongPayload(BlockPos pos, String songUrl) implements CustomPayload {
    public static final Id<PlaySongPayload> ID = new Id<>(Identifier.of(SmilouxMod.MOD_ID, "play_song"));
    public static final PacketCodec<PacketByteBuf, PlaySongPayload> CODEC = PacketCodec.tuple(
        BlockPos.PACKET_CODEC, PlaySongPayload::pos,
        PacketCodecs.STRING, PlaySongPayload::songUrl,
        PlaySongPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
