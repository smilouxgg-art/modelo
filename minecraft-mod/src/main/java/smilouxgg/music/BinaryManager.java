package smilouxgg.music;

import net.minecraft.client.MinecraftClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class BinaryManager {
    private static final String YTDLP_URL = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";
    private static final String FFMPEG_URL = "https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip";
    private static volatile boolean ready;
    private static volatile boolean installing;
    private static volatile String status = "Esperando instalación...";

    private BinaryManager() {}

    public static void startAutoInstall() {
        if (installing || ready) return;
        installing = true;
        Thread thread = new Thread(() -> {
            try {
                ensureInstalled();
            } catch (Exception e) {
                status = "Error: " + e.getMessage();
            } finally {
                installing = false;
            }
        }, "MusicMod-DependencyInstaller");
        thread.setDaemon(true);
        thread.start();
    }

    public static boolean isReady() {
        if (ready) return true;
        try {
            ready = Files.exists(ytDlpPath()) && Files.exists(ffmpegPath());
        } catch (Exception ignored) {}
        return ready;
    }

    public static String getStatus() { return status; }

    public static Path ytDlpPath() { return binDirectory().resolve(isWindows() ? "yt-dlp.exe" : "yt-dlp"); }
    public static Path ffmpegPath() { return binDirectory().resolve(isWindows() ? "ffmpeg.exe" : "ffmpeg"); }

    public static void ensureInstalled() throws Exception {
        Path bin = binDirectory();
        Files.createDirectories(bin);

        if (!isWindows() || !isX64()) {
            if (isCommandAvailable("yt-dlp") && isCommandAvailable("ffmpeg")) {
                status = "✓ Dependencias encontradas en PATH.";
                ready = true;
                return;
            }
            throw new IllegalStateException("La instalación automática está preparada para Windows x64. En Linux/macOS usa yt-dlp y FFmpeg en PATH.");
        }

        Path yt = ytDlpPath();
        Path ff = ffmpegPath();
        if (!Files.exists(yt)) {
            status = "Descargando yt-dlp...";
            download(YTDLP_URL, yt);
            yt.toFile().setExecutable(true);
        }
        if (!Files.exists(ff)) {
            status = "Descargando FFmpeg (~160 MB)...";
            Path archive = bin.resolve("ffmpeg.zip");
            download(FFMPEG_URL, archive);
            extractFfmpeg(archive, ff);
            Files.deleteIfExists(archive);
        }
        ready = Files.exists(yt) && Files.exists(ff);
        status = ready ? "✓ yt-dlp y FFmpeg listos." : "Faltan dependencias.";
    }

    private static void extractFfmpeg(Path zip, Path target) throws Exception {
        try (InputStream in = Files.newInputStream(zip); ZipInputStream zipIn = new ZipInputStream(in)) {
            ZipEntry entry;
            byte[] buffer = new byte[1024 * 1024];
            while ((entry = zipIn.getNextEntry()) != null) {
                String name = entry.getName().replace('\\', '/').toLowerCase(Locale.ROOT);
                if (!entry.isDirectory() && name.endsWith("/bin/ffmpeg.exe")) {
                    try (OutputStream out = Files.newOutputStream(target)) {
                        int read;
                        while ((read = zipIn.read(buffer)) != -1) out.write(buffer, 0, read);
                    }
                    target.toFile().setExecutable(true);
                    return;
                }
            }
        }
        throw new IllegalStateException("No se encontró ffmpeg.exe en el archivo descargado.");
    }

    private static void download(String url, Path target) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestProperty("User-Agent", "MusicMod/1.0");
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(300000);
        connection.setInstanceFollowRedirects(true);
        try (InputStream in = connection.getInputStream(); OutputStream out = Files.newOutputStream(target)) {
            byte[] buffer = new byte[1024 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
        }
    }

    private static Path binDirectory() {
        MinecraftClient client = MinecraftClient.getInstance();
        Path root = client != null ? client.runDirectory.toPath() : Path.of(".");
        return root.resolve("musicmod").resolve("bin");
    }

    private static boolean isCommandAvailable(String command) {
        try {
            Process p = new ProcessBuilder(command, "--version").redirectError(ProcessBuilder.Redirect.DISCARD).start();
            return p.waitFor() == 0;
        } catch (Exception ignored) { return false; }
    }

    private static boolean isWindows() { return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win"); }
    private static boolean isX64() {
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);
        return arch.contains("amd64") || arch.contains("x86_64");
    }
}
