package com.smiloux.mod.client.audio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AudioProcessManager {
    public record TrackInfo(String title, String artist, String url, int durationSeconds) {}

    private final Path ytdlp;
    private final Path ffmpeg;
    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "Smiloux-Audio");
        t.setDaemon(true);
        return t;
    });
    private volatile Process currentYtDlp;
    private volatile Process currentFfmpeg;

    public AudioProcessManager() {
        Path bin = FabricLoader.getInstance().getGameDir().resolve("mods").resolve("Smiloux").resolve("bin");
        ytdlp = bin.resolve(isWindows() ? "yt-dlp.exe" : "yt-dlp");
        ffmpeg = bin.resolve(isWindows() ? "ffmpeg.exe" : "ffmpeg");
    }

    public CompletableFuture<List<TrackInfo>> search(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String json = run(new ProcessBuilder(
                    ytdlp.toString(), "--flat-playlist", "--dump-single-json", "--no-warnings",
                    "ytsearch8:" + query
                ), false);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                List<TrackInfo> tracks = new ArrayList<>();
                JsonArray entries = root.has("entries") ? root.getAsJsonArray("entries") : new JsonArray();
                for (JsonElement element : entries) {
                    if (!element.isJsonObject()) continue;
                    JsonObject obj = element.getAsJsonObject();
                    String url = obj.has("webpage_url") ? obj.get("webpage_url").getAsString() : obj.has("url") ? obj.get("url").getAsString() : "";
                    if (!url.isBlank() && !url.startsWith("http")) url = "https://www.youtube.com/watch?v=" + url;
                    tracks.add(new TrackInfo(
                        obj.has("title") ? obj.get("title").getAsString() : "Sin título",
                        obj.has("uploader") ? obj.get("uploader").getAsString() : obj.has("channel") ? obj.get("channel").getAsString() : "Desconocido",
                        url,
                        obj.has("duration") && !obj.get("duration").isJsonNull() ? obj.get("duration").getAsInt() : 0
                    ));
                }
                return tracks;
            } catch (Exception e) {
                throw new RuntimeException("No se pudo buscar música: " + e.getMessage(), e);
            }
        }, executor);
    }

    public CompletableFuture<Path> prepareAudio(TrackInfo track, AudioFilter filter) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path output = Files.createTempFile("smiloux-", ".pcm");
                Process yt = new ProcessBuilder(
                    ytdlp.toString(), "-f", "bestaudio/best", "-o", "-", "--no-playlist", "--quiet", track.url()
                ).redirectError(ProcessBuilder.Redirect.DISCARD).start();
                Process ff = new ProcessBuilder(
                    ffmpeg.toString(), "-hide_banner", "-loglevel", "error", "-i", "pipe:0",
                    "-af", filter.ffmpegFilter(), "-f", "s16le", "-ar", "44100", "-ac", "2", "-y", output.toString()
                ).redirectError(ProcessBuilder.Redirect.DISCARD).start();
                currentYtDlp = yt;
                currentFfmpeg = ff;

                CompletableFuture<Void> pipe = CompletableFuture.runAsync(() -> {
                    try (var in = yt.getInputStream(); var out = ff.getOutputStream()) {
                        in.transferTo(out);
                    } catch (IOException ignored) { }
                }, executor);

                int ffCode = ff.waitFor();
                int ytCode = yt.waitFor();
                pipe.join();
                currentYtDlp = null;
                currentFfmpeg = null;

                if (ffCode != 0 || ytCode != 0 || Files.size(output) == 0) {
                    Files.deleteIfExists(output);
                    throw new IOException("yt-dlp/FFmpeg no pudo procesar la pista");
                }
                return output;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }, executor);
    }

    public void stopProcesses() {
        Process yt = currentYtDlp;
        Process ff = currentFfmpeg;
        if (yt != null) yt.destroyForcibly();
        if (ff != null) ff.destroyForcibly();
    }

    public void shutdown() {
        stopProcesses();
        executor.shutdownNow();
    }

    private static String run(ProcessBuilder builder, boolean captureStderr) throws Exception {
        Process process = builder.redirectError(captureStderr ? ProcessBuilder.Redirect.PIPE : ProcessBuilder.Redirect.DISCARD).start();
        String out = new String(process.getInputStream().readAllBytes());
        int code = process.waitFor();
        if (code != 0) throw new IOException("Proceso terminó con código " + code);
        return out;
    }

    private static boolean isWindows() { return System.getProperty("os.name").toLowerCase().contains("win"); }
}
