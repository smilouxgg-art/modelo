package com.smiloux.mod.init;

import com.smiloux.mod.SmilouxMod;
import com.smiloux.mod.entity.SmilouxJukeboxBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class SmilouxBlockEntities {
    public static BlockEntityType<SmilouxJukeboxBlockEntity> SMILOUX_JUKEBOX;

    private SmilouxBlockEntities() {}

    public static void register() {
        SMILOUX_JUKEBOX = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(SmilouxMod.MOD_ID, "smiloux_jukebox"),
            FabricBlockEntityTypeBuilder.create(SmilouxJukeboxBlockEntity::new, SmilouxBlocks.SMILOUX_JUKEBOX).build()
        );
    }
}
