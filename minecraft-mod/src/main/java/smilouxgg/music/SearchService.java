package smilouxgg.music;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class SearchService {
    private SearchService() {}

    public static CompletableFuture<List<MusicTrack>> search(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!BinaryManager.isReady()) BinaryManager.ensureInstalled();
                Process process = new ProcessBuilder(
                    BinaryManager.ytDlpPath().toString(),
                    "--flat-playlist", "--dump-single-json", "--skip-download", "--no-warnings",
                    "ytsearch8:" + query
                ).redirectError(ProcessBuilder.Redirect.DISCARD).start();

                String json = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                if (!process.waitFor(60, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    throw new IllegalStateException("La búsqueda tardó demasiado.");
                }
                if (process.exitValue() != 0) throw new IllegalStateException("yt-dlp no pudo realizar la búsqueda.");

                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                JsonArray entries = root.has("entries") ? root.getAsJsonArray("entries") : new JsonArray();
                List<MusicTrack> result = new ArrayList<>();
                for (JsonElement element : entries) {
                    if (!element.isJsonObject()) continue;
                    JsonObject item = element.getAsJsonObject();
                    String title = item.has("title") ? item.get("title").getAsString() : "Sin título";
                    String url = item.has("webpage_url") ? item.get("webpage_url").getAsString() : "";
                    if (url.isBlank() && item.has("id")) url = "https://www.youtube.com/watch?v=" + item.get("id").getAsString();
                    if (!url.isBlank()) result.add(new MusicTrack(title, url));
                }
                return result;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }
}
