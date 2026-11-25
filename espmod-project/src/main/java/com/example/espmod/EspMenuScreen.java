package com.example.espmod;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class EspMenuScreen extends Screen {
    private static final int W = 200, H = 20, GAP = 24;
    private int row = 0;

    public EspMenuScreen() {
        super(Text.literal("ESP + Freecam"));
    }

    @Override
    protected void init() {
        row = 0;
        toggle("ESP", () -> Config.espEnabled, v -> Config.espEnabled = v);
        toggle("Spawners", () -> Config.spawners, v -> Config.spawners = v);
        toggle("Chests", () -> Config.chests, v -> Config.chests = v);
        toggle("Barrels", () -> Config.barrels, v -> Config.barrels = v);
        toggle("Ender Chests", () -> Config.enderChests, v -> Config.enderChests = v);
        toggle("Freecam", () -> Freecam.active, v -> {
            if (v != Freecam.active) EspMod.toggleFreecam(client);
        });

        addDrawableChild(new SpeedSlider(width / 2 - W / 2, y(), W, H));
        row++;

        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(width / 2 - W / 2, y() + 6, W, H).build());
    }

    private int y() {
        return height / 2 - 100 + row * GAP;
    }

    private void toggle(String name, BooleanSupplier get, Consumer<Boolean> set) {
        ButtonWidget btn = ButtonWidget.builder(label(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(label(name, get.getAsBoolean()));
        }).dimensions(width / 2 - W / 2, y(), W, H).build();
        addDrawableChild(btn);
        row++;
    }

    private static Text label(String name, boolean on) {
        return Text.literal(name + ": " + (on ? "ON" : "OFF"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 120, 0xFFFFFF);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) { // Right Shift again closes the menu
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static class SpeedSlider extends SliderWidget {
        private static final double MIN = 0.1, MAX = 3.0;

        SpeedSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Text.empty(), (Freecam.speed - MIN) / (MAX - MIN));
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Text.literal(String.format("Freecam speed: %.1f", MIN + value * (MAX - MIN))));
        }

        @Override
        protected void applyValue() {
            Freecam.speed = MIN + value * (MAX - MIN);
        }
    }
}
