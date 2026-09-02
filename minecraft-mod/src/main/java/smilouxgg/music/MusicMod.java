package smilouxgg.music;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class MusicMod implements ModInitializer {
    public static final String MOD_ID = "musicmod";
    private static final MusicManager MUSIC = new MusicManager();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("music")
                .then(CommandManager.literal("play")
                    .then(CommandManager.argument("url", StringArgumentType.greedyString())
                        .executes(ctx -> play(ctx.getSource(), StringArgumentType.getString(ctx, "url")))))
                .then(CommandManager.literal("stop")
                    .executes(ctx -> stop(ctx.getSource())))
                .then(CommandManager.literal("pause")
                    .executes(ctx -> pause(ctx.getSource())))
                .then(CommandManager.literal("resume")
                    .executes(ctx -> resume(ctx.getSource())))
                .then(CommandManager.literal("status")
                    .executes(ctx -> status(ctx.getSource())))
            );
        });
    }

    private static int play(ServerCommandSource source, String url) {
        MUSIC.play(source.getServer(), url);
        source.sendFeedback(() -> Text.literal("§a♫ Música iniciada: " + url), false);
        return 1;
    }

    private static int stop(ServerCommandSource source) {
        MUSIC.stop();
        source.sendFeedback(() -> Text.literal("§e♫ Música detenida."), false);
        return 1;
    }

    private static int pause(ServerCommandSource source) {
        MUSIC.pause();
        source.sendFeedback(() -> Text.literal("§e♫ Música pausada."), false);
        return 1;
    }

    private static int resume(ServerCommandSource source) {
        MUSIC.resume();
        source.sendFeedback(() -> Text.literal("§a♫ Música reanudada."), false);
        return 1;
    }

    private static int status(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal("§b♫ " + MUSIC.status()), false);
        return 1;
    }
}
