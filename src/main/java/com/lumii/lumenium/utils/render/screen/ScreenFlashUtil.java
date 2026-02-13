package com.lumii.lumenium.utils.render.screen;

import com.lumii.lumenium.utils.math.Easings;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public class ScreenFlashUtil implements HudRenderCallback {
    private static final List<Flash> flashes = new ArrayList<>();
    private static final List<Flash> toRemove = new ArrayList<>();
    private static float partialTicks = 0f;


    static {
        // ticking
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            tick();
        });

        // register hud rendering
        HudRenderCallback.EVENT.register(new ScreenFlashUtil());
    }

    // simple flash, fades immediately
    public static void flash(int r, int g, int b, float alpha, int duration, Easings easing) {
        scheduleFlash(r, g, b, alpha, duration, 0, easing);
    }

    // flash that holds max alpha for specified tick
    public static void heldFlash(int r, int g, int b, float alpha, int holdDuration, int fadeDuration, Easings easing) {
        scheduleFlash(r, g, b, alpha, fadeDuration, holdDuration, easing);
    }

    private static void scheduleFlash(int r, int g, int b, float alpha, int fadeDuration, int holdDuration, Easings easing) {
        if (fadeDuration <= 0 && holdDuration <= 0) return;
        synchronized (flashes) {
            flashes.add(new Flash(r, g, b, alpha, fadeDuration, holdDuration, easing));
        }
    }

    // ticking

    private static void tick() {
        synchronized (flashes) {
            if (flashes.isEmpty()) return;

            toRemove.clear();
            for (Flash flash : flashes) {
                flash.prevTicks = flash.ticks;
                flash.ticks++;

                int total = flash.holdDuration + flash.fadeDuration;
                if (flash.ticks >= total) {
                    toRemove.add(flash);
                }
            }
            flashes.removeAll(toRemove);
        }
    }

    // rendering

    public static void renderFlash(MatrixStack matrices, float tickDelta) {
        partialTicks = tickDelta;

        synchronized (flashes) {
            if (flashes.isEmpty()) return;

            MinecraftClient client = MinecraftClient.getInstance();
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();

            for (Flash flash : flashes) {
                float t = flash.prevTicks + (flash.ticks - flash.prevTicks) * partialTicks;

                float alpha;
                if (t < flash.holdDuration) alpha = flash.baseAlpha;
                else {
                    float dt = t - flash.holdDuration;
                    alpha = flash.baseAlpha * (1f - dt / flash.fadeDuration);
                    if (alpha <= 0f) continue;
                }

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();

                DrawableHelper.fill(
                        matrices,
                        0, 0, width, height,
                        ((int)(alpha * 255) << 24) | (flash.r << 16) | (flash.g << 8) | flash.b
                );

                RenderSystem.disableBlend();
            }
        }
    }

    @Override
    public void onHudRender(MatrixStack matrices, float tickDelta) {
        partialTicks = tickDelta;

        synchronized (flashes) {
            if (flashes.isEmpty()) return;

            int width = MinecraftClient.getInstance().getWindow().getScaledWidth();
            int height = MinecraftClient.getInstance().getWindow().getScaledHeight();

            for (Flash flash : flashes) {
                float t = flash.prevTicks + (flash.ticks - flash.prevTicks) * partialTicks;

                float alpha = flash.baseAlpha;

                // holding
                if (t < flash.holdDuration) {
                    alpha = flash.baseAlpha;
                }
                // fading
                else {
                    float dt = t - flash.holdDuration;
                    float fadeProgress = dt / flash.fadeDuration;
                    if (fadeProgress > 1f) fadeProgress = 1f;

                    alpha = flash.baseAlpha * (1f - fadeProgress);
                }

                if (alpha <= 0f) continue;

                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();


                DrawableHelper.fill(
                        matrices,
                        0, 0, width, height,
                        (int)(alpha * 255) << 24 | (flash.r << 16) | (flash.g << 8) | flash.b
                );

                RenderSystem.disableBlend();
            }
        }
    }

    // data stuff

    private static class Flash {

        Easings easing = Easings.LINEAR; // default easing
        int r, g, b;
        float baseAlpha;

        int fadeDuration;
        int holdDuration;

        int ticks = 0;
        int prevTicks = 0;

        Flash(int r, int g, int b, float baseAlpha, int fadeDuration, int holdDuration, Easings easing) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.baseAlpha = baseAlpha;
            this.fadeDuration = Math.max(1, fadeDuration);
            this.holdDuration = Math.max(0, holdDuration);
            this.easing = easing == null ? Easings.LINEAR : easing;
        }
    }
}