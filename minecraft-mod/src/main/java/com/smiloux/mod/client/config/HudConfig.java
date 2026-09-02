package com.smiloux.mod.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HudConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("smiloux_hud.json");
    private static final HudConfig INSTANCE = load();

    public int x = 12;
    public int y = 12;
    public boolean enabled = true;

    public static HudConfig get() { return INSTANCE; }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(FILE)) { GSON.toJson(this, writer); } catch (Exception ignored) {}
    }

    private static HudConfig load() {
        try {
            if (Files.exists(FILE)) {
                try (Reader reader = Files.newBufferedReader(FILE)) {
                    HudConfig config = GSON.fromJson(reader, HudConfig.class);
                    if (config != null) return config;
                }
            }
        } catch (Exception ignored) {}
        HudConfig config = new HudConfig();
        config.save();
        return config;
    }
}
