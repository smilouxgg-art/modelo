package com.smiloux.mod.client.gui;

import com.smiloux.mod.client.SmilouxClient;
import com.smiloux.mod.client.audio.AudioEngine;
import com.smiloux.mod.client.audio.AudioFilter;
import com.smiloux.mod.client.audio.AudioProcessManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class MusicScreen extends Screen {
    private final Screen parent;
    private final AudioEngine engine;
    private final List<ButtonWidget> resultButtons = new ArrayList<>();
    private TextFieldWidget search;
    private List<AudioProcessManager.TrackInfo> results = List.of();
    private String message = "Busca una canción, artista o pega una URL";
    private int filterIndex;

    public MusicScreen(Screen parent, AudioEngine engine) {
        super(Text.literal("Smiloux Music"));
        this.parent = parent;
        this.engine = engine;
        this.filterIndex = engine.getFilter().ordinal();
    }

    @Override
    protected void init() {
        int cx = width / 2;
        search = new TextFieldWidget(textRenderer, cx - 190, 28, 300, 20, Text.literal("Buscar"));
        search.setMaxLength(200);
        search.setPlaceholder(Text.literal("Canción, artista o URL..."));
        addDrawableChild(search);
        addDrawableChild(ButtonWidget.builder(Text.literal("Buscar"), b -> doSearch()).dimensions(cx + 116, 28, 74, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("▶ Play"), b -> { if (engine.getCurrent() != null) engine.resume(); })
            .dimensions(cx - 190, 56, 68, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("⏸ Pausa"), b -> engine.pause())
            .dimensions(cx - 116, 56, 68, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("⏮ Prev"), b -> engine.previous())
            .dimensions(cx - 42, 56, 68, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("⏭ Next"), b -> engine.next())
            .dimensions(cx + 32, 56, 68, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("⏹ Stop"), b -> engine.stop())
            .dimensions(cx + 106, 56, 84, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Filtro: " + engine.getFilter().displayName()), b -> {
            filterIndex = (filterIndex + 1) % AudioFilter.values().length;
            engine.setFilter(AudioFilter.values()[filterIndex]);
            b.setMessage(Text.literal("Filtro: " + engine.getFilter().displayName()));
        }).dimensions(cx - 190, 82, 150, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Vol -"), b -> engine.setVolume(engine.getVolume() - .1f)).dimensions(cx - 32, 82, 55, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Vol +"), b -> engine.setVolume(engine.getVolume() + .1f)).dimensions(cx + 28, 82, 55, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Limpiar cola"), b -> engine.clearQueue()).dimensions(cx + 88, 82, 102, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Editar HUD"), b -> MinecraftClient.getInstance().setScreen(new SmilouxHudEditScreen(this))).dimensions(cx - 190, height - 32, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cerrar"), b -> close()).dimensions(cx + 90, height - 32, 100, 20).build());
        search.setFocused(true);
    }

    private void doSearch() {
        String q = search.getText().trim();
        if (q.isEmpty()) return;
        message = "Buscando...";
        results = List.of();
        refreshResults();
        engine.search(q).whenComplete((found, error) -> MinecraftClient.getInstance().execute(() -> {
            if (error != null) message = "Error: " + root(error);
            else { results = found; message = found.isEmpty() ? "Sin resultados" : "Resultados: " + found.size(); }
            refreshResults();
        }));
    }

    private void refreshResults() {
        for (ButtonWidget b : resultButtons) remove(b);
        resultButtons.clear();
        int cx = width / 2;
        int y = 112;
        for (int i = 0; i < Math.min(8, results.size()); i++) {
            AudioProcessManager.TrackInfo track = results.get(i);
            ButtonWidget b = ButtonWidget.builder(Text.literal((i + 1) + ". " + trim(track.title(), 58)), btn -> {
                engine.play(track);
                message = "Cargando: " + track.title();
            }).dimensions(cx - 190, y, 380, 20).build();
            resultButtons.add(b);
            addDrawableChild(b);
            y += 25;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int cx = width / 2;
        context.fill(cx - 205, 8, cx + 205, height - 8, 0xCC111318);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("♫ SMILOUX MUSIC"), cx, 12, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(message), cx, 104, 0xBBBBBB);
        var current = engine.getCurrent();
        if (current != null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("♪ " + trim(current.title(), 62)), cx, height - 55, 0x55FF55);
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Volumen " + Math.round(engine.getVolume() * 100) + "% · " + engine.getFilter().displayName()), cx, height - 42, 0xAAAAAA);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 && search.isFocused()) { doSearch(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() { MinecraftClient.getInstance().setScreen(parent); }
    private static String trim(String s, int n) { return s.length() <= n ? s : s.substring(0, n - 1) + "…"; }
    private static String root(Throwable t) { while (t.getCause() != null) t = t.getCause(); return t.getMessage() == null ? "Error desconocido" : t.getMessage(); }
}
