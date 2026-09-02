package smilouxgg.music;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

public class MusicMod implements ClientModInitializer {
    public static final String MOD_ID = "musicmod";
    private static final MusicManager MUSIC = new MusicManager();

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal("music")
                .then(ClientCommandManager.literal("play")
                    .then(ClientCommandManager.argument("url", StringArgumentType.greedyString())
                        .executes(ctx -> play(StringArgumentType.getString(ctx, "url")))))
                .then(ClientCommandManager.literal("stop").executes(ctx -> stop()))
                .then(ClientCommandManager.literal("pause").executes(ctx -> pause()))
                .then(ClientCommandManager.literal("resume").executes(ctx -> resume()))
                .then(ClientCommandManager.literal("status").executes(ctx -> status()))
        ));
    }

    private static int play(String url) {
        MUSIC.play(url);
        send("§a♫ Música iniciada: " + url);
        return 1;
    }

    private static int stop() { MUSIC.stop(); send("§e♫ Música detenida."); return 1; }
    private static int pause() { MUSIC.pause(); send("§e♫ Música pausada."); return 1; }
    private static int resume() { MUSIC.resume(); send("§a♫ Música reanudada."); return 1; }
    private static int status() { send("§b♫ " + MUSIC.status()); return 1; }

    private static void send(String text) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player != null) client.player.sendMessage(Text.literal(text), false);
    }
}
