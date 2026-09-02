package smilouxgg.music;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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

        if (!Files.exists(ffmpeg)) {
            throw new IllegalStateException("FFmpeg aún no está incluido en el instalador automático de esta versión.");
        }
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
