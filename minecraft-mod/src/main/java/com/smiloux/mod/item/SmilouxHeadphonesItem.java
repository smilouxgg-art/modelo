package com.smiloux.mod.item;

import com.smiloux.mod.client.SmilouxClientAccess;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.List;

public class SmilouxHeadphonesItem extends Item {
    public SmilouxHeadphonesItem(Settings settings) { super(settings); }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient && user.isSneaking()) SmilouxClientAccess.openMusicScreen();
        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip) {
        tooltip.add(Text.literal("Shift + clic derecho: abrir Smiloux"));
        tooltip.add(Text.literal("Equípalo en la ranura de cabeza para audio privado."));
    }
}
