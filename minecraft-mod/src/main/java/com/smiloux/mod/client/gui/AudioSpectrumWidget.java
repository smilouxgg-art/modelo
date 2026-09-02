package com.smiloux.mod.client.gui;

import net.minecraft.client.gui.DrawContext;

public final class AudioSpectrumWidget {
    private AudioSpectrumWidget() {}

    public static void render(DrawContext context, int x, int y, int width, int height, float time) {
        int bars = 18;
        int gap = 2;
        int barWidth = Math.max(2, (width - (bars - 1) * gap) / bars);
        for (int i = 0; i < bars; i++) {
            float wave = (float) ((Math.sin(time * 5.0 + i * 0.72) + 1.0) * 0.5);
            float wave2 = (float) ((Math.sin(time * 2.7 + i * 0.31) + 1.0) * 0.5);
            int barHeight = Math.max(2, (int) ((0.25f + wave * 0.45f + wave2 * 0.30f) * height));
            int left = x + i * (barWidth + gap);
            context.fill(left, y + height - barHeight, left + barWidth, y + height, 0xFF65D7FF);
        }
    }
}
