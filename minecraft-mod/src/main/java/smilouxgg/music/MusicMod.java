package smilouxgg.music;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class MusicMod implements ClientModInitializer {
    public static final String MOD_ID = "musicmod";
    public static final MusicEngine ENGINE = new MusicEngine();
    private static KeyBinding openGuiKey;

    @Override
    public void onInitializeClient() {
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.musicmod.open_gui", GLFW.GLFW_KEY_M, "category.musicmod"));

        ClientTickEvents.END_CLIENT_TICK.register(MusicMod::tick);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal("music")
                .then(ClientCommandManager.literal("gui").executes(ctx -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new MusicScreen(client.currentScreen, ENGINE));
                    return 1;
                }))
                .then(ClientCommandManager.literal("play")
                    .then(ClientCommandManager.argument("url", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String url = StringArgumentType.getString(ctx, "url");
                            ENGINE.play(new MusicTrack(url, url));
                            send("§a♫ Reproduciendo: " + url);
                            return 1;
                        })))
                .then(ClientCommandManager.literal("pause").executes(ctx -> { ENGINE.pause(); send("§e♫ Pausado."); return 1; }))
                .then(ClientCommandManager.literal("resume").executes(ctx -> { ENGINE.resume(); send("§a♫ Continuando."); return 1; }))
                .then(ClientCommandManager.literal("skip").executes(ctx -> { ENGINE.next(); send("§b♫ Siguiente."); return 1; }))
                .then(ClientCommandManager.literal("stop").executes(ctx -> { ENGINE.stop(); send("§e♫ Detenido."); return 1; }))
                .then(ClientCommandManager.literal("status").executes(ctx -> { send("§b♫ " + ENGINE.getStatus()); return 1; }))
                .then(ClientCommandManager.literal("volume")
                    .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(0, 100))
                        .executes(ctx -> {
                            int value = IntegerArgumentType.getInteger(ctx, "value");
                            ENGINE.setVolume(value / 100f);
                            send("§d♫ Volumen: " + value + "%");
                            return 1;
                        })))
                .then(ClientCommandManager.literal("clear").executes(ctx -> { ENGINE.clearQueue(); send("§e♫ Cola limpiada."); return 1; }))
        ));

        BinaryManager.startAutoInstall();
    }

    private static void tick(MinecraftClient client) {
        while (openGuiKey.wasPressed()) {
            client.setScreen(new MusicScreen(client.currentScreen, ENGINE));
        }
    }

    private static void send(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) client.player.sendMessage(Text.literal(message), false);
    }
}
