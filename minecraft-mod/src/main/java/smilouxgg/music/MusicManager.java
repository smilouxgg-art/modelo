package smilouxgg.music;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import org.lwjgl.openal.AL10;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

public final class MusicManager {
    private int source = 0;
    private int buffer = 0;
    private volatile String current = "Nada";
    private volatile boolean paused = false;

    public void play(MinecraftServer server, String url) {
        CompletableFuture.runAsync(() -> {
            try {
                BinaryManager.ensureInstalled();
                Path pcm = BinaryManager.downloadAndConvert(url);
                MinecraftClient.getInstance().execute(() -> startPcm(pcm, url));
            } catch (Exception e) {
                MinecraftClient.getInstance().execute(() ->
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(
                        net.minecraft.text.Text.literal("§cMusic Mod: " + e.getMessage())));
            }
        });
    }

    private void startPcm(Path pcm, String url) {
        stop();
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(pcm))) {
            byte[] bytes = in.readAllBytes();
            ByteBuffer data = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
            data.put(bytes).flip();
            IntBuffer buffers = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
            AL10.alGenBuffers(buffers);
            buffer = buffers.get(0);
            AL10.alBufferData(buffer, AL10.AL_FORMAT_STEREO16, data, 48000);

            IntBuffer sources = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
            AL10.alGenSources(sources);
            source = sources.get(0);
            AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
            AL10.alSourcePlay(source);
            current = url;
            paused = false;
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer el audio: " + e.getMessage(), e);
        }
    }

    public void stop() {
        if (source != 0) {
            AL10.alSourceStop(source);
            AL10.alDeleteSources(source);
            source = 0;
        }
        if (buffer != 0) {
            AL10.alDeleteBuffers(buffer);
            buffer = 0;
        }
        current = "Nada";
        paused = false;
    }

    public void pause() {
        if (source != 0) {
            AL10.alSourcePause(source);
            paused = true;
        }
    }

    public void resume() {
        if (source != 0) {
            AL10.alSourcePlay(source);
            paused = false;
        }
    }

    public String status() {
        return paused ? "Pausado: " + current : "Reproduciendo: " + current;
    }
}
