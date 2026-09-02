package com.smiloux.mod.init;

import com.smiloux.mod.SmilouxMod;
import com.smiloux.mod.block.SmilouxJukeboxBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class SmilouxBlocks {
    public static final Block SMILOUX_JUKEBOX = Registry.register(
        Registries.BLOCK,
        Identifier.of(SmilouxMod.MOD_ID, "smiloux_jukebox"),
        new SmilouxJukeboxBlock(Block.Settings.create().strength(2.0f).requiresTool())
    );

    private SmilouxBlocks() {}
    public static void register() {}
}
