package com.smiloux.mod.client.audio;

import com.smiloux.mod.SmilouxMod;
import com.smiloux.mod.client.util.DependencyManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import com.smiloux.mod.init.SmilouxItems;
import org.lwjgl.openal.AL10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;

public final class AudioEngine {
    private final AudioProcessManager processes = new AudioProcessManager();
    private final Deque<AudioProcessManager.TrackInfo> queue = new ArrayDeque<>();
    private final Deque<AudioProcessManager.TrackInfo> history = new ArrayDeque<>();
    private volatile AudioProcessManager.TrackInfo current;
    private volatile AudioFilter filter = AudioFilter.NONE;
    private float volume = 1f;
    private int source;
    private int buffer;
    private Path currentPcm;
    private Vec3d position = Vec3d.ZERO;
    private boolean positional;
    private boolean paused;

    public CompletableFuture<Void> play(AudioProcessManager.TrackInfo track) {
        queue.clear();
        return start(track, false, Vec3d.ZERO);
    }

    public CompletableFuture<Void> playPositional(AudioProcessManager.TrackInfo track, Vec3d pos) {
        queue.clear();
        return start(track, true, pos);
    }

    public void enqueue(AudioProcessManager.TrackInfo track) { queue.addLast(track); }

    public void next() {
        stopSource();
        AudioProcessManager.TrackInfo next = queue.pollFirst();
        if (next != null) start(next, positional, position);
    }

    public void previous() {
        AudioProcessManager.TrackInfo previous = history.pollLast();
        if (previous != null) start(previous, positional, position);
    }

    public void pause() { if (source != 0) { AL10.alSourcePause(source); paused = true; } }
    public void resume() { if (source != 0) { AL10.alSourcePlay(source); paused = false; } }

    public void stop() {
        stopSource();
        current = null;
        queue.clear();
        history.clear();
    }

    public void clearQueue() { queue.clear(); }

    public void setFilter(AudioFilter filter) { this.filter = filter; }
    public AudioFilter getFilter() { return filter; }
    public void setVolume(float value) { volume = Math.max(0f, Math.min(1f, value)); if (source != 0) AL10.alSourcef(source, AL10.AL_GAIN, volume); }
    public float getVolume() { return volume; }
    public AudioProcessManager.TrackInfo getCurrent() { return current; }
    public boolean isPlaying() { return source != 0 && AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING; }
    public boolean isPaused() { return paused; }
    public Vec3d getPosition() { return position; }
    public boolean isPositional() { return positional; }
    public Deque<AudioProcessManager.TrackInfo> getQueue() { return queue; }

    public float progressSeconds() {
        if (source == 0) return 0f;
        return AL10.alGetSourcef(source, AL10.AL_SEC_OFFSET);
    }

    public void tick(MinecraftClient client) {
        if (source == 0) return;
        boolean headphones = client.player != null && isHeadphonesEquipped(client.player.getInventory().getArmorStack(3));
        if (headphones || !positional) {
            AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
            AL10.alSource3f(source, AL10.AL_POSITION, 0f, 0f, 0f);
        } else {
            AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
            AL10.alSource3f(source, AL10.AL_POSITION, (float) position.x, (float) position.y, (float) position.z);
        }
        if (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_STOPPED) next();
    }

    private CompletableFuture<Void> start(AudioProcessManager.TrackInfo track, boolean positional, Vec3d pos) {
        this.positional = positional;
        this.position = pos;
        if (current != null) history.addLast(current);
        current = track;
        paused = false;
        return DependencyManager.ensureInstalled().thenCompose(ok -> {
            if (!ok) return CompletableFuture.failedFuture(new IllegalStateException("No se pudieron instalar yt-dlp/FFmpeg"));
            return processes.prepareAudio(track, filter);
        }).thenAccept(pcm -> MinecraftClient.getInstance().execute(() -> loadPcm(pcm)));
    }

    private void loadPcm(Path pcm) {
        try {
            stopSource();
            byte[] bytes = Files.readAllBytes(pcm);
            ByteBuffer data = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
            data.put(bytes).flip();
            buffer = AL10.alGenBuffers();
            AL10.alBufferData(buffer, AL10.AL_FORMAT_STEREO16, data, 44100);
            source = AL10.alGenSources();
            AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
            AL10.alSourcef(source, AL10.AL_GAIN, volume);
            AL10.alSourcef(source, AL10.AL_PITCH, 1f);
            AL10.alSourcePlay(source);
            currentPcm = pcm;
            SmilouxMod.LOGGER.info("Playing {}", current.title());
        } catch (Exception e) {
            SmilouxMod.LOGGER.error("Could not start audio", e);
        }
    }

    private void stopSource() {
        if (source != 0) {
            AL10.alSourceStop(source);
            AL10.alDeleteSources(source);
            source = 0;
        }
        if (buffer != 0) {
            AL10.alDeleteBuffers(buffer);
            buffer = 0;
        }
        if (currentPcm != null) {
            try { Files.deleteIfExists(currentPcm); } catch (Exception ignored) {}
            currentPcm = null;
        }
    }

    private boolean isHeadphonesEquipped(ItemStack stack) { return stack.isOf(SmilouxItems.HEADPHONES); }

    public void shutdown() { stop(); processes.shutdown(); }
}
