package com.smiloux.mod.client.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class DependencyManager {
    private static final Path BIN = FabricLoader.getInstance().getGameDir().resolve("mods/Smiloux/bin");

    private DependencyManager() {}

    public static Path binDirectory() { return BIN; }
    public static Path ytdlpPath() { return BIN.resolve(exe("yt-dlp")); }
    public static Path ffmpegPath() { return BIN.resolve(exe("ffmpeg")); }

    public static CompletableFuture<Boolean> ensureInstalled() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Files.createDirectories(BIN);
                installYtDlp();
                installFfmpeg();
                return Files.isExecutable(ytdlpPath()) && Files.isExecutable(ffmpegPath());
            } catch (Exception e) {
                System.err.println("[Smiloux] Dependencias: " + e.getMessage());
                return false;
            }
        });
    }

    private static void installYtDlp() throws Exception {
        if (Files.exists(ytdlpPath())) return;
        String asset = isWindows() ? "yt-dlp.exe" : isMac() ? "yt-dlp_macos" : "yt-dlp";
        download("https://github.com/yt-dlp/yt-dlp/releases/latest/download/" + asset, ytdlpPath());
        ytdlpPath().toFile().setExecutable(true);
    }

    private static void installFfmpeg() throws Exception {
        if (Files.exists(ffmpegPath())) return;
        if (isWindows()) {
            Path zip = BIN.resolve("ffmpeg.zip");
            download("https://github.com/BtbN/FFmpeg-Builds/releases/latest/download/ffmpeg-master-latest-win64-gpl.zip", zip);
            unzipFfmpeg(zip, ffmpegPath());
            Files.deleteIfExists(zip);
        } else if (isLinux()) {
            Path archive = BIN.resolve("ffmpeg.tar.xz");
            download("https://github.com/BtbN/FFmpeg-Builds/releases/latest/download/ffmpeg-master-latest-linux64-gpl.tar.xz", archive);
            extractTar(archive);
            Files.deleteIfExists(archive);
        } else if (isMac()) {
            Path zip = BIN.resolve("ffmpeg.zip");
            download("https://evermeet.cx/ffmpeg/getrelease/ffmpeg/zip", zip);
            unzipFfmpeg(zip, ffmpegPath());
            Files.deleteIfExists(zip);
        } else {
            throw new IllegalStateException("Sistema operativo no soportado");
        }
        ffmpegPath().toFile().setExecutable(true);
    }

    private static void download(String url, Path target) throws Exception {
        HttpURLConnection c = (HttpURLConnection) URI.create(url).toURL().openConnection();
        c.setInstanceFollowRedirects(true);
        c.setConnectTimeout(20_000);
        c.setReadTimeout(180_000);
        c.setRequestProperty("User-Agent", "Smiloux/1.0");
        if (c.getResponseCode() / 100 != 2) throw new IllegalStateException("HTTP " + c.getResponseCode());
        try (InputStream in = c.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void unzipFfmpeg(Path zip, Path target) throws Exception {
        try (var in = new java.util.zip.ZipInputStream(Files.newInputStream(zip))) {
            java.util.zip.ZipEntry e;
            while ((e = in.getNextEntry()) != null) {
                if (!e.isDirectory() && e.getName().toLowerCase(Locale.ROOT).endsWith("/bin/ffmpeg.exe")) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
                if (!e.isDirectory() && isMac() && e.getName().equals("ffmpeg")) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }
        }
        throw new IllegalStateException("No se encontró ffmpeg en el archivo descargado");
    }

    private static void extractTar(Path archive) throws Exception {
        Process p = new ProcessBuilder("tar", "-xJf", archive.toString(), "-C", BIN.toString()).inheritIO().start();
        if (p.waitFor() != 0) throw new IllegalStateException("tar no pudo extraer FFmpeg");
        try (var stream = Files.walk(BIN)) {
            Path found = stream.filter(Files::isRegularFile).filter(pth -> pth.getFileName().toString().equals("ffmpeg")).findFirst().orElseThrow();
            Files.move(found, ffmpegPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String exe(String name) { return isWindows() ? name + ".exe" : name; }
    private static boolean isWindows() { return os("win"); }
    private static boolean isLinux() { return os("linux"); }
    private static boolean isMac() { return os("mac"); }
    private static boolean os(String value) { return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains(value); }
}
