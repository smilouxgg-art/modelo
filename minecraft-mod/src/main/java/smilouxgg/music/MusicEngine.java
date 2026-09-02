package smilouxgg.music;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

public final class MusicEngine {
    private final Deque<MusicTrack> queue = new ArrayDeque<>();
    private Process ytdlpProcess;
    private Process ffmpegProcess;
    private SourceDataLine line;
    private Thread playbackThread;
    private volatile MusicTrack current;
    private volatile boolean paused;
    private volatile boolean stopping;
    private volatile float volume = 0.85f;

    public synchronized void play(MusicTrack track) {
        queue.clear();
        stopInternal();
        queue.add(track);
        playNext();
    }

    public synchronized void enqueue(MusicTrack track) {
        queue.add(track);
        if (current == null) playNext();
    }

    public synchronized void next() {
        stopInternal();
        playNext();
    }

    public synchronized void previous() {
        if (current != null) {
            queue.addFirst(current);
            stopInternal();
            playNext();
        }
    }

    public synchronized void pause() {
        if (line != null && line.isOpen() && !paused) {
            line.stop();
            paused = true;
        }
    }

    public synchronized void resume() {
        if (line != null && line.isOpen() && paused) {
            line.start();
            paused = false;
        }
    }

    public synchronized void stop() {
        stopInternal();
        queue.clear();
    }

    public synchronized void clearQueue() {
        queue.clear();
    }

    public synchronized void setVolume(float value) {
        volume = Math.max(0f, Math.min(1f, value));
        applyVolume();
    }

    public float getVolume() {
        return volume;
    }

    public MusicTrack getCurrent() {
        return current;
    }

    public Deque<MusicTrack> getQueueSnapshot() {
        synchronized (this) {
            return new ArrayDeque<>(queue);
        }
    }

    public String getStatus() {
        MusicTrack track = current;
        if (track == null) return "Nada reproduciéndose";
        if (paused) return "⏸ " + track.title();
        return "▶ " + track.title();
    }

    private synchronized void playNext() {
        if (queue.isEmpty()) {
            current = null;
            return;
        }
        current = queue.pollFirst();
        paused = false;
        stopping = false;
        MusicTrack track = current;
        playbackThread = new Thread(() -> stream(track), "MusicMod-Playback");
        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    private void stream(MusicTrack track) {
        try {
            if (!BinaryManager.isReady()) BinaryManager.ensureInstalled();
            Path yt = BinaryManager.ytDlpPath();
            Path ff = BinaryManager.ffmpegPath();

            ytdlpProcess = new ProcessBuilder(yt.toString(), "-f", "bestaudio/best", "-o", "-", "--no-playlist", "--quiet", "--no-warnings", track.url())
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();
            ffmpegProcess = new ProcessBuilder(ff.toString(), "-hide_banner", "-loglevel", "error", "-i", "pipe:0", "-f", "s16le", "-ar", "48000", "-ac", "2", "pipe:1")
                .redirectError(ProcessBuilder.Redirect.DISCARD).start();

            Thread pipe = new Thread(() -> {
                try {
                    ytdlpProcess.getInputStream().transferTo(ffmpegProcess.getOutputStream());
                } catch (Exception ignored) {
                } finally {
                    try { ffmpegProcess.getOutputStream().close(); } catch (Exception ignored) {}
                }
            }, "MusicMod-Pipe");
            pipe.setDaemon(true);
            pipe.start();

            AudioFormat format = new AudioFormat(48000f, 16, 2, true, false);
            line = AudioSystem.getSourceDataLine(format);
            line.open(format, 96000);
            applyVolume();
            line.start();

            byte[] buffer = new byte[16384];
            try (BufferedInputStream in = new BufferedInputStream(ffmpegProcess.getInputStream())) {
                int read;
                while (!stopping && (read = in.read(buffer)) != -1) {
                    while (paused && !stopping) Thread.sleep(50);
                    if (!stopping) line.write(buffer, 0, read);
                }
            }

            int code = ffmpegProcess.waitFor();
            if (!stopping && code != 0) notifyUser("§cMusic Mod: FFmpeg no pudo reproducir el audio.");
        } catch (Exception e) {
            if (!stopping) notifyUser("§cMusic Mod: " + e.getMessage());
        } finally {
            synchronized (this) {
                if (line != null) {
                    try { line.drain(); } catch (Exception ignored) {}
                    try { line.stop(); } catch (Exception ignored) {}
                    try { line.close(); } catch (Exception ignored) {}
                    line = null;
                }
                if (ytdlpProcess != null) ytdlpProcess.destroyForcibly();
                if (ffmpegProcess != null) ffmpegProcess.destroyForcibly();
                ytdlpProcess = null;
                ffmpegProcess = null;
                if (!stopping) {
                    current = null;
                    if (!queue.isEmpty()) playNext();
                }
            }
        }
    }

    private synchronized void stopInternal() {
        stopping = true;
        paused = false;
        current = null;
        if (line != null) {
            try { line.stop(); } catch (Exception ignored) {}
            try { line.close(); } catch (Exception ignored) {}
            line = null;
        }
        if (ytdlpProcess != null) ytdlpProcess.destroyForcibly();
        if (ffmpegProcess != null) ffmpegProcess.destroyForcibly();
        ytdlpProcess = null;
        ffmpegProcess = null;
    }

    private synchronized void applyVolume() {
        if (line == null || !line.isControlSupported(FloatControl.Type.MASTER_GAIN)) return;
        FloatControl control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        float db;
        if (volume <= 0.001f) db = control.getMinimum();
        else db = 20f * (float) Math.log10(volume);
        control.setValue(Math.max(control.getMinimum(), Math.min(control.getMaximum(), db)));
    }

    private void notifyUser(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.execute(() -> {
            if (client.inGameHud != null) client.inGameHud.getChatHud().addMessage(Text.literal(message));
        });
    }
}
