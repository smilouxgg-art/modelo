package com.smiloux.mod;

import com.smiloux.mod.init.SmilouxBlockEntities;
import com.smiloux.mod.init.SmilouxBlocks;
import com.smiloux.mod.init.SmilouxItems;
import com.smiloux.mod.network.SmilouxServerNetwork;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SmilouxMod implements ModInitializer {
    public static final String MOD_ID = "smiloux";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        SmilouxBlocks.register();
        SmilouxItems.register();
        SmilouxBlockEntities.register();
        SmilouxServerNetwork.register();
        LOGGER.info("Smiloux initialized");
    }
}
