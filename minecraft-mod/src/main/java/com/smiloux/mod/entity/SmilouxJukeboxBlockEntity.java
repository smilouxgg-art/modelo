package com.smiloux.mod.entity;

import com.smiloux.mod.init.SmilouxBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public class SmilouxJukeboxBlockEntity extends BlockEntity {
    private String songUrl = "";
    private boolean playing;

    public SmilouxJukeboxBlockEntity(BlockPos pos, BlockState state) {
        super(SmilouxBlockEntities.SMILOUX_JUKEBOX, pos, state);
    }

    public String getSongUrl() { return songUrl; }
    public boolean isPlaying() { return playing; }

    public void setSong(String url) {
        songUrl = url == null ? "" : url;
        playing = !songUrl.isBlank();
        markDirty();
    }

    public void setPlaying(boolean value) {
        playing = value;
        markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        nbt.putString("SongUrl", songUrl);
        nbt.putBoolean("Playing", playing);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        songUrl = nbt.getString("SongUrl");
        playing = nbt.getBoolean("Playing");
    }
}
