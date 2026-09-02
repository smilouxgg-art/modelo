package smilouxgg.music;

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
    private final MusicEngine engine;
    private TextFieldWidget searchBox;
    private final List<ButtonWidget> resultButtons = new ArrayList<>();
    private List<MusicTrack> results = List.of();
    private String status = "Escribe una canción o artista y pulsa Buscar";
    private boolean searching;

    public MusicScreen(Screen parent, MusicEngine engine) {
        super(Text.literal("Music Mod"));
        this.parent = parent;
        this.engine = engine;
    }

    @Override
    protected void init() {
        int center = this.width / 2;
        searchBox = new TextFieldWidget(this.textRenderer, center - 170, 38, 250, 20, Text.literal("Buscar música"));
        searchBox.setMaxLength(120);
        searchBox.setPlaceholder(Text.literal("Canción, artista o URL..."));
        addDrawableChild(searchBox);

        addDrawableChild(ButtonWidget.builder(Text.literal("Buscar"), button -> doSearch())
            .dimensions(center + 86, 38, 84, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("▶ Reproducir"), b -> {
            MusicTrack current = engine.getCurrent();
            if (current != null) engine.resume();
        }).dimensions(center - 170, 64, 84, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("⏸ Pausa"), b -> engine.pause())
            .dimensions(center - 80, 64, 84, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("⏭ Siguiente"), b -> engine.next())
            .dimensions(center + 10, 64, 84, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("⏹ Detener"), b -> engine.stop())
            .dimensions(center + 100, 64, 84, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("🔉 -"), b -> engine.setVolume(engine.getVolume() - 0.1f))
            .dimensions(center - 105, 90, 45, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("🔊 +"), b -> engine.setVolume(engine.getVolume() + 0.1f))
            .dimensions(center + 60, 90, 45, 20).build());

        refreshResults();
        searchBox.setFocused(true);
    }

    private void doSearch() {
        String query = searchBox.getText().trim();
        if (query.isEmpty() || searching) return;
        searching = true;
        status = "Buscando...";
        refreshResults();
        SearchService.search(query).whenComplete((found, error) -> MinecraftClient.getInstance().execute(() -> {
            searching = false;
            if (error != null) {
                results = List.of();
                status = "Error: " + rootMessage(error);
            } else {
                results = found;
                status = found.isEmpty() ? "No se encontraron resultados" : "Resultados encontrados: " + found.size();
            }
            refreshResults();
        }));
    }

    private void refreshResults() {
        for (ButtonWidget button : resultButtons) remove(button);
        resultButtons.clear();

        int y = 116;
        int center = this.width / 2;
        int max = Math.min(results.size(), 8);
        for (int i = 0; i < max; i++) {
            MusicTrack track = results.get(i);
            final MusicTrack chosen = track;
            String title = (i + 1) + ". " + truncate(track.title(), 56);
            ButtonWidget button = ButtonWidget.builder(Text.literal(title), b -> {
                engine.play(chosen);
                status = "Reproduciendo: " + chosen.title();
            }).dimensions(center - 220, y, 440, 20).build();
            resultButtons.add(button);
            addDrawableChild(button);
            y += 25;
        }
    }

    private static String truncate(String value, int length) {
        return value.length() <= length ? value : value.substring(0, length - 1) + "…";
    }

    private static String rootMessage(Throwable throwable) {
        Throwable t = throwable;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? "Error desconocido" : t.getMessage();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int center = width / 2;
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("♫ MUSIC MOD"), center, 12, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), center, 104, 0xB5B5B5);
        MusicTrack current = engine.getCurrent();
        if (current != null) {
            context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Ahora: " + truncate(current.title(), 70)), center, height - 28, 0x55FF55);
            context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(String.format("Volumen: %d%%", Math.round(engine.getVolume() * 100))), center, 94, 0xAAAAAA);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 && searchBox.isFocused()) {
            doSearch();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }
}
