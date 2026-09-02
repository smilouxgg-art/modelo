package com.smiloux.mod.client;

import com.smiloux.mod.client.audio.AudioEngine;
import com.smiloux.mod.client.discord.SmilouxDiscordRPC;
import com.smiloux.mod.client.gui.MusicScreen;
import com.smiloux.mod.client.gui.SmilouxHudEditScreen;
import com.smiloux.mod.client.gui.SmilouxHudOverlay;
import com.smiloux.mod.client.playlist.PlaylistManager;
import com.smiloux.mod.client.util.DependencyManager;
import com.smiloux.mod.network.SmilouxClientNetwork;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class SmilouxClient implements ClientModInitializer {
    public static final AudioEngine ENGINE = new AudioEngine();
    public static KeyBinding OPEN_GUI;
    public static KeyBinding EDIT_HUD;

    @Override
    public void onInitializeClient() {
        OPEN_GUI = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.smiloux.open_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_M, "category.smiloux"));
        EDIT_HUD = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.smiloux.edit_hud", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_O, "category.smiloux"));
        SmilouxClientNetwork.register();
        SmilouxCommands.register();
        PlaylistManager.ensureDirectory();
        DependencyManager.ensureInstalled();
        SmilouxDiscordRPC.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_GUI.wasPressed()) client.setScreen(new MusicScreen(client.currentScreen, ENGINE));
            while (EDIT_HUD.wasPressed()) client.setScreen(new SmilouxHudEditScreen(client.currentScreen));
            ENGINE.tick(client);
            SmilouxDiscordRPC.tick(ENGINE.getCurrent());
        });
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> SmilouxHudOverlay.render(drawContext, ENGINE));
    }
}
