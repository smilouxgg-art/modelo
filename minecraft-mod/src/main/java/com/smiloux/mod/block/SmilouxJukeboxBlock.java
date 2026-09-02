package com.smiloux.mod.block;

import com.smiloux.mod.client.SmilouxClientAccess;
import com.smiloux.mod.entity.SmilouxJukeboxBlockEntity;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SmilouxJukeboxBlock extends BlockWithEntity {
    public SmilouxJukeboxBlock(Settings settings) { super(settings); }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SmilouxJukeboxBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            SmilouxClientAccess.openMusicScreen();
            return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME;
    }
}
