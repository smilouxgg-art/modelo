package com.smiloux.mod.client;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.smiloux.mod.client.audio.AudioFilter;
import com.smiloux.mod.network.SmilouxClientNetwork;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class SmilouxCommands {
    private SmilouxCommands() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal("music")
                .then(ClientCommandManager.literal("gui").executes(ctx -> { SmilouxClientAccess.openMusicScreen(); return 1; }))
                .then(ClientCommandManager.literal("play").then(ClientCommandManager.argument("url", StringArgumentType.greedyString())
                    .executes(ctx -> { SmilouxClient.ENGINE.play(new com.smiloux.mod.client.audio.AudioProcessManager.TrackInfo("URL", "Internet", StringArgumentType.getString(ctx, "url"), 0)); return 1; })))
                .then(ClientCommandManager.literal("pause").executes(ctx -> { SmilouxClient.ENGINE.pause(); return 1; }))
                .then(ClientCommandManager.literal("resume").executes(ctx -> { SmilouxClient.ENGINE.resume(); return 1; }))
                .then(ClientCommandManager.literal("skip").executes(ctx -> { SmilouxClient.ENGINE.next(); return 1; }))
                .then(ClientCommandManager.literal("previous").executes(ctx -> { SmilouxClient.ENGINE.previous(); return 1; }))
                .then(ClientCommandManager.literal("stop").executes(ctx -> { SmilouxClient.ENGINE.stop(); return 1; }))
                .then(ClientCommandManager.literal("clear").executes(ctx -> { SmilouxClient.ENGINE.clearQueue(); return 1; }))
                .then(ClientCommandManager.literal("status").executes(ctx -> {
                    var t = SmilouxClient.ENGINE.getCurrent();
                    send(t == null ? "No hay música" : "♫ " + t.title() + " — " + SmilouxClient.ENGINE.getFilter().displayName());
                    return 1;
                }))
                .then(ClientCommandManager.literal("volume").then(ClientCommandManager.argument("value", FloatArgumentType.floatArg(0, 100))
                    .executes(ctx -> { SmilouxClient.ENGINE.setVolume(FloatArgumentType.getFloat(ctx, "value") / 100f); return 1; })))
                .then(ClientCommandManager.literal("filter").then(ClientCommandManager.argument("name", StringArgumentType.word())
                    .executes(ctx -> {
                        String value = StringArgumentType.getString(ctx, "name");
                        for (AudioFilter f : AudioFilter.values()) if (f.name().equalsIgnoreCase(value) || f.displayName().replace(" ", "_").equalsIgnoreCase(value)) { SmilouxClient.ENGINE.setFilter(f); send("Filtro: " + f.displayName()); return 1; }
                        send("Filtros: NONE, BASS_BOOST, NIGHTCORE, EIGHT_D, VAPORWAVE"); return 0;
                    })))
                .then(ClientCommandManager.literal("broadcast").then(ClientCommandManager.argument("url", StringArgumentType.greedyString())
                    .executes(ctx -> {
                        var player = MinecraftClient.getInstance().player;
                        if (player == null) return 0;
                        SmilouxClientNetwork.requestFromBlock(player.getBlockPos(), StringArgumentType.getString(ctx, "url"));
                        send("Solicitud enviada al servidor");
                        return 1;
                    })))
        ));
    }

    private static void send(String msg) {
        var player = MinecraftClient.getInstance().player;
        if (player != null) player.sendMessage(Text.literal("§b[Smiloux] §f" + msg), false);
    }
}
