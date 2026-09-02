package com.smiloux.mod.client.gui;

import com.smiloux.mod.client.audio.AudioEngine;
import com.smiloux.mod.client.config.HudConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class SmilouxHudOverlay {
    private static long start = System.nanoTime();
    private SmilouxHudOverlay() {}

    public static void render(DrawContext context, AudioEngine engine) {
        HudConfig cfg = HudConfig.get();
        if (!cfg.enabled || engine.getCurrent() == null) return;
        var client = MinecraftClient.getInstance();
        int x = cfg.x;
        int y = cfg.y;
        int width = 260;
        int height = 62;
        context.fill(x, y, x + width, y + height, 0xCC0D1117);
        context.drawTextWithShadow(client.textRenderer, Text.literal("♫ SMILOUX"), x + 8, y + 6, 0xFF65D7FF);
        context.drawTextWithShadow(client.textRenderer, Text.literal(trim(engine.getCurrent().title(), 34)), x + 8, y + 20, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer, Text.literal(trim(engine.getCurrent().artist(), 34)), x + 8, y + 34, 0xFFAAAAAA);
        AudioSpectrumWidget.render(context, x + 130, y + 8, 120, 38, (System.nanoTime() - start) / 1_000_000_000f);
        int duration = engine.getCurrent().durationSeconds();
        float progress = duration > 0 ? Math.min(1f, engine.progressSeconds() / duration) : 0f;
        context.fill(x + 8, y + height - 7, x + width - 8, y + height - 5, 0xFF3A424C);
        context.fill(x + 8, y + height - 7, x + 8 + (int) ((width - 16) * progress), y + height - 5, 0xFF65D7FF);
    }

    private static String trim(String s, int n) { return s.length() <= n ? s : s.substring(0, n - 1) + "…"; }
}
