package com.smiloux.mod.client.gui;

import com.smiloux.mod.client.config.HudConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class SmilouxHudEditScreen extends Screen {
    private final Screen parent;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public SmilouxHudEditScreen(Screen parent) {
        super(Text.literal("Editar HUD Smiloux"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        HudConfig cfg = HudConfig.get();
        int x = cfg.x, y = cfg.y;
        context.fill(x, y, x + 260, y + 62, 0xAA111827);
        context.drawTextWithShadow(textRenderer, Text.literal("Arrastra este HUD"), x + 8, y + 8, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, Text.literal("X: " + x + "  Y: " + y), x + 8, y + 26, 0xFFAAAAAA);
        context.drawTextWithShadow(textRenderer, Text.literal("ESC para salir · clic para arrastrar"), 10, height - 20, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        HudConfig cfg = HudConfig.get();
        if (button == 0 && mouseX >= cfg.x && mouseX <= cfg.x + 260 && mouseY >= cfg.y && mouseY <= cfg.y + 62) {
            dragging = true;
            dragOffsetX = (int) mouseX - cfg.x;
            dragOffsetY = (int) mouseY - cfg.y;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            HudConfig cfg = HudConfig.get();
            cfg.x = Math.max(0, Math.min(width - 260, (int) mouseX - dragOffsetX));
            cfg.y = Math.max(0, Math.min(height - 62, (int) mouseY - dragOffsetY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) { dragging = false; HudConfig.get().save(); return true; }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() { HudConfig.get().save(); MinecraftClient.getInstance().setScreen(parent); }
}
