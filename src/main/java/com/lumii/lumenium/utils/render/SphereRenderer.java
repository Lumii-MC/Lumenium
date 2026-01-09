package com.lumii.lumenium.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class SphereRenderer {

    private static final List<Sphere> spheres = new ArrayList<>();
    private static final Identifier PACKET_ID = new Identifier("lumenium", "sphere_render");

    static {
        init();
        ClientTickEvents.START_CLIENT_TICK.register(client -> tick());
    }

    // SERVER → CLIENT
    public static void scheduleCommon(ServerWorld world, Vec3d pos, float radius, Identifier texture,
                                      int duration, float alpha, boolean fade, int fadeStart) {

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
        buf.writeFloat(radius);
        buf.writeString(texture.toString());
        buf.writeInt(duration);
        buf.writeFloat(alpha);
        buf.writeBoolean(fade);
        buf.writeInt(fadeStart);

        for (ServerPlayerEntity p : world.getPlayers()) {
            ServerPlayNetworking.send(p, PACKET_ID, buf);
        }
    }

    // CLIENT ONLY
    public static void scheduleClient(Vec3d pos, float radius, Identifier texture,
                                      int duration, float alpha, boolean fade, int fadeStart) {

        spheres.add(new Sphere(pos, radius, texture, duration, alpha, fade, fadeStart));
    }

    private static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isPaused()) return;

        spheres.removeIf(s -> {
            s.prevDuration = s.duration;
            s.duration--;
            return s.duration <= 0;
        });
    }

    private static void init() {
        // Receive from server
        ClientPlayNetworking.registerGlobalReceiver(PACKET_ID, (client, handler, buf, sender) -> {
            Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            float radius = buf.readFloat();
            Identifier tex = new Identifier(buf.readString());
            int duration = buf.readInt();
            float alpha = buf.readFloat();
            boolean fade = buf.readBoolean();
            int fadeStart = buf.readInt();

            client.execute(() -> scheduleClient(pos, radius, tex, duration, alpha, fade, fadeStart));
        });

        // Render spheres
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            if (spheres.isEmpty()) return;

            MatrixStack matrices = context.matrixStack();
            Vec3d camPos = context.camera().getPos();
            float tickDelta = context.tickDelta();

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

            for (Sphere s : spheres) {
                float interpDur = s.prevDuration + (s.duration - s.prevDuration) * tickDelta;
                float lived = s.maxDuration - interpDur;

                float alpha = s.baseAlpha;
                if (s.fade && lived >= s.fadeStart) {
                    float fadeTicks = (s.maxDuration - s.fadeStart);
                    float remaining = Math.max(0, interpDur - s.fadeStart);
                    alpha *= (remaining / fadeTicks);
                }

                RenderSystem.setShaderTexture(0, s.texture);
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

                matrices.push();
                matrices.translate(s.pos.x - camPos.x, s.pos.y - camPos.y, s.pos.z - camPos.z);
                matrices.scale(s.radius, s.radius, s.radius);

                drawSphere(matrices.peek().getPositionMatrix());

                matrices.pop();
            }

            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        });
    }

    // ------------------------------------------------------------
    // SPHERE DRAWING (simple UV sphere)
    // ------------------------------------------------------------
    private static void drawSphere(Matrix4f matrix) {
        final int stacks = 16;
        final int slices = 16;

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_TEXTURE_COLOR);

        int r = 255, g = 255, b = 255, a = 255;

        for (int i = 0; i < stacks; i++) {
            float lat0 = (float) Math.PI * (-0.5f + (float) i / stacks);
            float lat1 = (float) Math.PI * (-0.5f + (float) (i + 1) / stacks);

            float z0 = (float) Math.sin(lat0);
            float zr0 = (float) Math.cos(lat0);

            float z1 = (float) Math.sin(lat1);
            float zr1 = (float) Math.cos(lat1);

            for (int j = 0; j <= slices; j++) {
                float lng = (float) (2 * Math.PI * (j % slices) / slices);
                float x = (float) Math.cos(lng);
                float y = (float) Math.sin(lng);

                buffer.vertex(matrix, x * zr0, y * zr0, z0).texture((float) j / slices, (float) i / stacks).color(r, g, b, a).next();
                buffer.vertex(matrix, x * zr1, y * zr1, z1).texture((float) j / slices, (float) (i + 1) / stacks).color(r, g, b, a).next();
            }
        }

        Tessellator.getInstance().draw();
    }

    // ------------------------------------------------------------
    // DATA CLASS
    // ------------------------------------------------------------
    private static class Sphere {
        Vec3d pos;
        float radius;
        Identifier texture;

        int duration;
        int maxDuration;
        int prevDuration;

        boolean fade;
        int fadeStart;
        float baseAlpha;

        Sphere(Vec3d pos, float radius, Identifier texture,
               int duration, float alpha, boolean fade, int fadeStart) {

            this.pos = pos;
            this.radius = radius;
            this.texture = texture;

            this.duration = duration;
            this.maxDuration = duration;
            this.prevDuration = duration;

            this.fade = fade;
            this.fadeStart = fadeStart;
            this.baseAlpha = alpha;
        }
    }
}
