package com.smiloux.mod.init;

import com.smiloux.mod.SmilouxMod;
import com.smiloux.mod.item.SmilouxHeadphonesItem;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class SmilouxItems {
    public static final Item SMILOUX_JUKEBOX = Registry.register(
        Registries.ITEM,
        Identifier.of(SmilouxMod.MOD_ID, "smiloux_jukebox"),
        new BlockItem(SmilouxBlocks.SMILOUX_JUKEBOX, new Item.Settings())
    );
    public static final Item HEADPHONES = Registry.register(
        Registries.ITEM,
        Identifier.of(SmilouxMod.MOD_ID, "smiloux_headphones"),
        new SmilouxHeadphonesItem(new Item.Settings().maxCount(1))
    );

    private SmilouxItems() {}
    public static void register() {}
}
