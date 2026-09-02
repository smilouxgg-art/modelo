package com.smiloux.mod.client;

import com.smiloux.mod.client.gui.MusicScreen;
import net.minecraft.client.MinecraftClient;

public final class SmilouxClientAccess {
    private SmilouxClientAccess() {}

    public static void openMusicScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new MusicScreen(client.currentScreen, SmilouxClient.ENGINE));
    }
}
