package smilouxgg.music;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class BinaryManager {
    private static final Path DIR = Path.of(System.getProperty("user.home"), ".musicmod", "bin");

    private BinaryManager() {}

    public static void ensureInstalled() throws Exception {
        Files.createDirectories(DIR);
        Path ytdlp = DIR.resolve(isWindows() ? "yt-dlp.exe" : "yt-dlp");
        Path ffmpeg = DIR.resolve(isWindows() ? "ffmpeg.exe" : "ffmpeg");

        if (!Files.exists(ytdlp)) {
            download(isWindows()
                ? "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe"
                : "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp", ytdlp);
            ytdlp.toFile().setExecutable(true);
        }

        if (!Files.exists(ffmpeg)) installFfmpeg(ffmpeg);
    }

    private static void installFfmpeg(Path target) throws Exception {
        String url;
        if (isWindows()) {
            url = "https://github.com/BtbN/FFmpeg-Builds/releases/latest/download/ffmpeg-master-latest-win64-gpl.zip";
        } else if (System.getProperty("os.name").toLowerCase().contains("linux") && System.getProperty("os.arch").contains("64")) {
            url = "https://github.com/BtbN/FFmpeg-Builds/releases/latest/download/ffmpeg-master-latest-linux64-gpl.tar.xz";
        } else {
            throw new IllegalStateException("Instalación automática de FFmpeg disponible en Windows x64 y Linux x64.");
        }

        Path archive = Files.createTempFile("musicmod-ffmpeg-", isWindows() ? ".zip" : ".tar.xz");
        download(url, archive);
        if (isWindows()) extractWindowsFfmpeg(archive, target);
        else throw new IllegalStateException("FFmpeg Linux descargado; añade extracción tar.xz para este sistema.");
        Files.deleteIfExists(archive);
        target.toFile().setExecutable(true);
    }

    private static void extractWindowsFfmpeg(Path zip, Path target) throws Exception {
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith("/bin/ffmpeg.exe")) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }
        }
        throw new IllegalStateException("No se encontró ffmpeg.exe en el paquete descargado.");
    }

    public static Path downloadAndConvert(String url) throws Exception {
        ensureInstalled();
        Path output = Files.createTempFile("musicmod-", ".pcm");
        Path ytdlp = DIR.resolve(isWindows() ? "yt-dlp.exe" : "yt-dlp");
        Path ffmpeg = DIR.resolve(isWindows() ? "ffmpeg.exe" : "ffmpeg");

        Process yt = new ProcessBuilder(ytdlp.toString(), "-f", "bestaudio/best", "-o", "-", "--no-playlist", "--quiet", url)
            .redirectError(ProcessBuilder.Redirect.DISCARD).start();
        Process ff = new ProcessBuilder(ffmpeg.toString(), "-hide_banner", "-loglevel", "error", "-i", "pipe:0", "-f", "s16le", "-ar", "48000", "-ac", "2", "pipe:1")
            .redirectError(ProcessBuilder.Redirect.DISCARD).start();

        yt.getInputStream().transferTo(ff.getOutputStream());
        ff.getOutputStream().close();
        Files.copy(ff.getInputStream(), output, StandardCopyOption.REPLACE_EXISTING);
        int ytCode = yt.waitFor();
        int ffCode = ff.waitFor();
        if (ytCode != 0 || ffCode != 0) {
            Files.deleteIfExists(output);
            throw new IllegalStateException("yt-dlp o FFmpeg no pudo procesar el audio.");
        }
        return output;
    }

    private static void download(String url, Path target) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) URI.create(url).toURL().openConnection();
        connection.setRequestProperty("User-Agent", "MusicMod/1.0");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(120000);
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
