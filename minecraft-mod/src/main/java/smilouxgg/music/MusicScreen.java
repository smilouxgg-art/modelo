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
    private String status = "Busca una canción, artista o pega una URL";
    private boolean searching;

    public MusicScreen(Screen parent, MusicEngine engine) {
        super(Text.literal("Music Mod"));
        this.parent = parent;
        this.engine = engine;
    }

    @Override
    protected void init() {
        int center = width / 2;
        searchBox = new TextFieldWidget(textRenderer, center - 220, 36, 355, 20, Text.literal("Buscar música"));
        searchBox.setMaxLength(120);
        searchBox.setPlaceholder(Text.literal("Canción, artista o URL..."));
        addDrawableChild(searchBox);

        addDrawableChild(ButtonWidget.builder(Text.literal("Buscar"), b -> doSearch())
            .dimensions(center + 140, 36, 80, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("▶ Play"), b -> engine.resume())
            .dimensions(center - 220, 64, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("⏸ Pause"), b -> engine.pause())
            .dimensions(center - 145, 64, 75, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("⏮ Prev"), b -> engine.previous())
            .dimensions(center - 65, 64, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("⏭ Next"), b -> engine.next())
            .dimensions(center + 10, 64, 75, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("⏹ Stop"), b -> engine.stop())
            .dimensions(center + 90, 64, 70, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("🗑 Vaciar"), b -> engine.clearQueue())
            .dimensions(center + 165, 64, 75, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("🔉 -10%"), b -> engine.setVolume(engine.getVolume() - 0.1f))
            .dimensions(center - 80, 91, 75, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("🔊 +10%"), b -> engine.setVolume(engine.getVolume() + 0.1f))
            .dimensions(center + 5, 91, 75, 20).build());

        refreshResults();
        searchBox.setFocused(true);
    }

    private void doSearch() {
        String query = searchBox.getText().trim();
        if (query.isEmpty() || searching) return;
        searching = true;
        status = "Buscando con yt-dlp...";
        refreshResults();
        SearchService.search(query).whenComplete((found, error) -> MinecraftClient.getInstance().execute(() -> {
            searching = false;
            if (error != null) {
                results = List.of();
                status = "Error: " + rootMessage(error);
            } else {
                results = found;
                status = found.isEmpty() ? "No se encontraron resultados" : "Resultados: " + found.size();
            }
            refreshResults();
        }));
    }

    private void refreshResults() {
        for (ButtonWidget button : resultButtons) remove(button);
        resultButtons.clear();

        int y = 120;
        int center = width / 2;
        int max = Math.min(results.size(), 8);
        for (int i = 0; i < max; i++) {
            MusicTrack track = results.get(i);
            final MusicTrack chosen = track;
            ButtonWidget play = ButtonWidget.builder(
                Text.literal((i + 1) + ". " + truncate(track.title(), 49)),
                b -> { engine.play(chosen); status = "▶ " + chosen.title(); })
                .dimensions(center - 220, y, 365, 20).build();
            ButtonWidget add = ButtonWidget.builder(Text.literal("+ Cola"),
                b -> { engine.enqueue(chosen); status = "✓ Añadida: " + chosen.title(); })
                .dimensions(center + 150, y, 70, 20).build();
            resultButtons.add(play);
            resultButtons.add(add);
            addDrawableChild(play);
            addDrawableChild(add);
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
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(status), center, 105, 0xB5B5B5);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Dependencias: " + BinaryManager.getStatus()), center, height - 44, 0xAAAAAA);

        MusicTrack current = engine.getCurrent();
        if (current != null) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal("Ahora: " + truncate(current.title(), 65)), center, height - 28, 0x55FF55);
            context.drawCenteredTextWithShadow(textRenderer,
                Text.literal(String.format("Volumen: %d%% | Cola: %d", Math.round(engine.getVolume() * 100), engine.getQueueSnapshot().size())),
                center, 94, 0xAAAAAA);
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
