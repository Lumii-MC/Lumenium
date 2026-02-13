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
import net.minecraft.util.math.Vec3f;

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

    // common scheduler (rotationSpeed added)
    public static void scheduleCommon(ServerWorld world, Vec3d pos,
                                      float startRadius, float endRadius,
                                      Identifier texture,
                                      int duration, float alpha,
                                      boolean fade, int fadeStart,
                                      float rotationSpeed) {

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);

        buf.writeFloat(startRadius);
        buf.writeFloat(endRadius);

        buf.writeString(texture.toString());
        buf.writeInt(duration);
        buf.writeFloat(alpha);
        buf.writeBoolean(fade);
        buf.writeInt(fadeStart);

        buf.writeFloat(rotationSpeed);

        for (ServerPlayerEntity p : world.getPlayers()) {
            ServerPlayNetworking.send(p, PACKET_ID, buf);
        }
    }

    // client scheduler (rotationSpeed added)
    public static void scheduleClient(Vec3d pos, float startRadius, float endRadius,
                                      Identifier texture, int duration,
                                      float alpha, boolean fade, int fadeStart,
                                      float rotationSpeed) {

        spheres.add(new Sphere(pos, startRadius, endRadius, texture, duration, alpha, fade, fadeStart, rotationSpeed));
    }

    private static void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isPaused()) return;

        spheres.removeIf(s -> {
            s.prevDuration = s.duration;
            s.duration--;

            // rotate sphere
            s.rotation += s.rotationSpeed;
            s.prevDuration = s.duration;
            s.prevRotation = s.rotation;

            return s.duration <= 0;
        });
    }

    private static void init() {

        // packet receiving
        ClientPlayNetworking.registerGlobalReceiver(PACKET_ID, (client, handler, buf, sender) -> {

            Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());

            float startR = buf.readFloat();
            float endR   = buf.readFloat();

            Identifier tex = new Identifier(buf.readString());
            int duration = buf.readInt();
            float alpha = buf.readFloat();
            boolean fade = buf.readBoolean();
            int fadeStart = buf.readInt();

            float rotationSpeed = buf.readFloat();

            client.execute(() -> scheduleClient(pos, startR, endR, tex, duration, alpha, fade, fadeStart, rotationSpeed));
        });

        // rendering
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
                float progress = 1f - (interpDur / s.maxDuration);

                float alpha = s.baseAlpha;
                if (s.fade) {
                    float remaining = interpDur;
                    if (remaining <= s.fadeStart) {
                        float t = 1f - (remaining / s.fadeStart);
                        t = Math.max(0f, Math.min(1f, t));
                        alpha *= (1f - t);
                    }
                }

                float scale = s.startRadius + (s.endRadius - s.startRadius) * progress;

                RenderSystem.setShaderTexture(0, s.texture);
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

                matrices.push();
                matrices.translate(s.pos.x - camPos.x, s.pos.y - camPos.y, s.pos.z - camPos.z);
                matrices.scale(scale, scale, scale);

                // sphere rotation
                float rot = s.prevRotation + (s.rotation - s.prevRotation) * tickDelta;
                matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(rot));


                drawSphere(matrices.peek().getPositionMatrix());
                matrices.pop();
            }

            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        });
    }

    // renders inside & outside faces
    private static void drawSphere(Matrix4f matrix) {
        RenderSystem.disableCull();
        buildSphere(matrix, false); // outside
        buildSphere(matrix, true);  // inside
        RenderSystem.enableCull();
    }

    private static void buildSphere(Matrix4f matrix, boolean inverted) {
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

                if (!inverted) {
                    buffer.vertex(matrix, x * zr0, z0, y * zr0)
                            .texture((float) j / slices, 1f - ((float) i / stacks))
                            .color(r, g, b, a).next();

                    buffer.vertex(matrix, x * zr1, z1, y * zr1)
                            .texture((float) j / slices, 1f - ((float) (i + 1) / stacks))
                            .color(r, g, b, a).next();
                } else {
                    buffer.vertex(matrix, x * zr1, z1, y * zr1)
                            .texture((float) j / slices, 1f - ((float) (i + 1) / stacks))
                            .color(r, g, b, a).next();

                    buffer.vertex(matrix, x * zr0, z0, y * zr0)
                            .texture((float) j / slices, 1f - ((float) i / stacks))
                            .color(r, g, b, a).next();
                }
            }
        }

        Tessellator.getInstance().draw();
    }

    // data class
    private static class Sphere {
        Vec3d pos;

        float startRadius;
        float endRadius;

        Identifier texture;

        int duration;
        int maxDuration;
        int prevDuration;

        boolean fade;
        int fadeStart;

        float baseAlpha;

        // rotation handling
        float rotation = 0f;
        float rotationSpeed;
        float prevRotation;


        Sphere(Vec3d pos, float startRadius, float endRadius,
               Identifier texture, int duration,
               float alpha, boolean fade, int fadeStart,
               float rotationSpeed) {

            this.pos = pos;
            this.startRadius = startRadius;
            this.endRadius = endRadius;
            this.texture = texture;

            this.duration = duration;
            this.maxDuration = duration;
            this.prevDuration = duration;

            this.fade = fade;
            this.fadeStart = fadeStart;
            this.baseAlpha = alpha;

            this.rotationSpeed = rotationSpeed;
        }
    }
}