package com.example.espmod;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class EspMod implements ClientModInitializer {
    record Target(BlockPos pos, float r, float g, float b) {}

    private static final double MAX_DIST = 128.0;
    private static volatile List<Target> targets = new ArrayList<>();

    private KeyBinding espKey, freecamKey, menuKey;
    private int scanTimer = 0;

    @Override
    public void onInitializeClient() {
        espKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.espmod.esp", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, "category.espmod"));
        freecamKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.espmod.freecam", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J, "category.espmod"));

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.espmod.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.espmod"));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        WorldRenderEvents.LAST.register(ctx -> render(ctx.matrixStack(), ctx.camera(), ctx.consumers()));
    }

    private void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) {
            Freecam.active = false;
            targets = new ArrayList<>();
            return;
        }

        while (menuKey.wasPressed()) {
            if (mc.currentScreen == null) mc.setScreen(new EspMenuScreen());
        }
        while (espKey.wasPressed()) {
            Config.espEnabled = !Config.espEnabled;
            mc.player.sendMessage(Text.literal("ESP: " + (Config.espEnabled ? "ON" : "OFF")), true);
        }
        while (freecamKey.wasPressed()) {
            toggleFreecam(mc);
        }

        // Safety for hardcore: your body stands still during freecam, so bail out if you get hurt.
        if (Freecam.active && mc.player.hurtTime > 0) {
            Freecam.active = false;
            mc.player.sendMessage(Text.literal("Freecam OFF (you took damage!)"), true);
        }

        if (Freecam.active) moveFreecam(mc);

        if (Config.espEnabled && ++scanTimer >= 20) { // rescan once per second
            scanTimer = 0;
            scan(mc);
        }
    }

    public static void toggleFreecam(MinecraftClient mc) {
        if (!Freecam.active) {
            Vec3d eye = mc.player.getEyePos();
            Freecam.x = eye.x; Freecam.y = eye.y; Freecam.z = eye.z;
            Freecam.yaw = mc.player.getYaw();
            Freecam.pitch = mc.player.getPitch();
            Freecam.active = true;
            mc.player.sendMessage(Text.literal("Freecam ON - your body stays put, watch for mobs!"), true);
        } else {
            Freecam.active = false;
            mc.player.sendMessage(Text.literal("Freecam OFF"), true);
        }
    }

    private void moveFreecam(MinecraftClient mc) {
        GameOptions o = mc.options;
        double f = (o.forwardKey.isPressed() ? 1 : 0) - (o.backKey.isPressed() ? 1 : 0);
        double s = (o.leftKey.isPressed() ? 1 : 0) - (o.rightKey.isPressed() ? 1 : 0);
        double v = (o.jumpKey.isPressed() ? 1 : 0) - (o.sneakKey.isPressed() ? 1 : 0);
        double yaw = Math.toRadians(Freecam.yaw), pitch = Math.toRadians(Freecam.pitch);
        double fx = -Math.sin(yaw) * Math.cos(pitch), fy = -Math.sin(pitch), fz = Math.cos(yaw) * Math.cos(pitch);
        double sx = Math.cos(yaw), sz = Math.sin(yaw);
        Freecam.x += (fx * f + sx * s) * Freecam.speed;
        Freecam.y += (fy * f + v) * Freecam.speed;
        Freecam.z += (fz * f + sz * s) * Freecam.speed;
    }

    private void scan(MinecraftClient mc) {
        ClientWorld w = mc.world;
        ChunkPos cp = mc.player.getChunkPos();
        int r = mc.options.getClampedViewDistance();
        Vec3d p = mc.player.getPos();
        List<Target> out = new ArrayList<>();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                WorldChunk ch = w.getChunkManager().getWorldChunk(cp.x + dx, cp.z + dz);
                if (ch == null) continue;
                for (BlockEntity be : ch.getBlockEntities().values()) {
                    BlockPos pos = be.getPos();
                    if (pos.getSquaredDistance(p) > MAX_DIST * MAX_DIST) continue;
                    if (be instanceof MobSpawnerBlockEntity)      { if (Config.spawners) out.add(new Target(pos, 1f, 0.1f, 0.1f)); }   // red
                    else if (be instanceof EnderChestBlockEntity) { if (Config.enderChests) out.add(new Target(pos, 0.7f, 0.2f, 1f)); }   // purple
                    else if (be instanceof ChestBlockEntity)      { if (Config.chests) out.add(new Target(pos, 1f, 0.65f, 0f)); }    // orange (incl. trapped)
                    else if (be instanceof BarrelBlockEntity)     { if (Config.barrels) out.add(new Target(pos, 0.2f, 0.9f, 0.3f)); } // green
                }
            }
        }
        targets = out;
    }

    private void render(MatrixStack m, Camera cam, VertexConsumerProvider consumers) {
        if (!Config.espEnabled || m == null || !(consumers instanceof VertexConsumerProvider.Immediate imm)) return;
        List<Target> list = targets;
        if (list.isEmpty()) return;

        Vec3d c = cam.getPos();
        RenderSystem.disableDepthTest(); // draw through walls
        VertexConsumer vc = imm.getBuffer(RenderLayer.getLines());
        m.push();
        m.translate(-c.x, -c.y, -c.z);
        for (Target t : list) {
            BlockPos p = t.pos();
            VertexRendering.drawBox(m, vc, p.getX(), p.getY(), p.getZ(),
                    p.getX() + 1, p.getY() + 1, p.getZ() + 1, t.r(), t.g(), t.b(), 1f);
        }
        m.pop();
        imm.draw(RenderLayer.getLines());
        RenderSystem.enableDepthTest();
    }
}
