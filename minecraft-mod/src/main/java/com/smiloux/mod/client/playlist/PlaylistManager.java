package com.smiloux.mod.client.playlist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smiloux.mod.client.model.Playlist;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PlaylistManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path DIR = FabricLoader.getInstance().getGameDir().resolve("mods/Smiloux/playlists");

    private PlaylistManager() {}

    public static void ensureDirectory() {
        try { Files.createDirectories(DIR); } catch (Exception ignored) {}
    }

    public static List<Playlist> loadAll() {
        ensureDirectory();
        List<Playlist> result = new ArrayList<>();
        try (var stream = Files.list(DIR)) {
            stream.filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                try (Reader r = Files.newBufferedReader(path)) {
                    Playlist playlist = GSON.fromJson(r, Playlist.class);
                    if (playlist != null) result.add(playlist);
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
        return result;
    }

    public static void save(Playlist playlist) {
        ensureDirectory();
        String safe = playlist.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
        try (Writer w = Files.newBufferedWriter(DIR.resolve(safe + ".json"))) {
            GSON.toJson(playlist, w);
        } catch (Exception ignored) {}
    }
}
