package com.lumii.softlightlib.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
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
public final class QuadRenderer {
    private static final List<Quad> queuedQuads = new ArrayList<>();
    private static final List<Quad> quadsToRemove = new ArrayList<>();
    private static int tickCounter = 0;
    private static float partialTicks = 0f;

    private static final Identifier QUAD_PACKET_ID = new Identifier("tritium", "quad_render");

    static {
        init();
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            clientTick();
        });
    }

    /**
     * Schedule a quad to be rendered on the server and sent to all clients.
     * Call this from server-side code.
     */
    public static void scheduleCommon(ServerWorld world, Vec3d pos, float width, float height,
                                      Vec3d rotation, float scale, Identifier texture,
                                      int duration, boolean fade, int fadeStart,
                                      boolean scaleUp, int scaleStart, float scaleFactor,
                                      float alpha) {

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);

        buf.writeFloat(width);
        buf.writeFloat(height);

        buf.writeDouble(rotation.x);
        buf.writeDouble(rotation.y);
        buf.writeDouble(rotation.z);

        buf.writeFloat(scale);
        buf.writeString(texture.toString());

        buf.writeInt(duration);
        buf.writeBoolean(fade);
        buf.writeInt(fadeStart);

        buf.writeBoolean(scaleUp);
        buf.writeInt(scaleStart);
        buf.writeFloat(scaleFactor);

        buf.writeFloat(alpha);

        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, QUAD_PACKET_ID, buf);
        }
    }

    /**
     * Schedule a quad to be rendered only on the client.
     * Call this from client-side code.
     */
    public static void scheduleClient(Vec3d pos, float width, float height,
                                      Vec3d rotation, float scale, Identifier texture,
                                      int duration, boolean fade, int fadeStart,
                                      boolean scaleUp, int scaleStart, float scaleFactor,
                                      float alpha) {
        if (duration <= 0) return;
        synchronized (queuedQuads) {
            queuedQuads.add(new Quad(pos, width, height, rotation, scale, texture, duration,
                    fade, fadeStart, scaleUp, scaleStart, scaleFactor, alpha));
        }
    }

    private static void clientTick() {
        synchronized (queuedQuads) {
            if (queuedQuads.isEmpty()) return;

            tickCounter++;

            quadsToRemove.clear();
            for (Quad quad : queuedQuads) {
                if (!MinecraftClient.getInstance().isPaused() || !MinecraftClient.getInstance().isInSingleplayer()){
                    quad.prevDuration = quad.duration;
                    quad.duration--;

                    if (quad.duration <= 0) {
                        quadsToRemove.add(quad);
                    }
                }
            }
            queuedQuads.removeAll(quadsToRemove);
        }
    }

    private static void init() {
        ClientPlayNetworking.registerGlobalReceiver(QUAD_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    double x = buf.readDouble();
                    double y = buf.readDouble();
                    double z = buf.readDouble();
                    Vec3d pos = new Vec3d(x, y, z);
                    float width = buf.readFloat();
                    float height = buf.readFloat();
                    double rotX = buf.readDouble();
                    double rotY = buf.readDouble();
                    double rotZ = buf.readDouble();
                    Vec3d rotation = new Vec3d(rotX, rotY, rotZ);
                    float scale = buf.readFloat();
                    String textureStr = buf.readString();
                    Identifier texture = new Identifier(textureStr);
                    int duration = buf.readInt();
                    boolean fade = buf.readBoolean();
                    int fadeStart = buf.readInt();

                    boolean scaleUp = buf.readBoolean();
                    int scaleStart = buf.readInt();
                    float scaleFactor = buf.readFloat();

                    float alpha = buf.readFloat();

                    client.execute(() -> {
                        scheduleClient(pos, width, height, rotation, scale, texture,
                                duration, fade, fadeStart, scaleUp, scaleStart, scaleFactor, alpha);
                    });
                }
        );

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            synchronized (queuedQuads) {
                if (queuedQuads.isEmpty()) return;

                partialTicks = context.tickDelta();

                MatrixStack matrices = context.matrixStack();
                Vec3d camPos = context.camera().getPos();

                List<Quad> sortedQuads = new ArrayList<>(queuedQuads);
                sortedQuads.sort((a, b) -> {
                    double distA = a.position.squaredDistanceTo(camPos);
                    double distB = b.position.squaredDistanceTo(camPos);
                    return Double.compare(distB, distA);
                });

                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(false);

                RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

                for (Quad q : sortedQuads) {
                    float interpolatedDuration = q.prevDuration + (q.duration - q.prevDuration) * partialTicks;
                    float interpolatedTicksLived = q.maxDuration - interpolatedDuration;

                    float alpha = q.baseAlpha;
                    if (q.fade) {
                        if (interpolatedTicksLived >= q.fadeStart) {
                            int fadeTicks = q.maxDuration - q.fadeStart;
                            float remainingDuration = Math.max(0, interpolatedDuration - q.fadeStart);
                            alpha = q.baseAlpha * (remainingDuration / fadeTicks);
                            if (alpha < 0f) alpha = 0f;
                        }
                    }

                    float scale = q.scale;
                    if (q.scaleUp) {
                        if (interpolatedTicksLived >= q.scaleStart) {
                            float t = (interpolatedTicksLived - q.scaleStart) / (q.maxDuration - q.scaleStart);
                            if (t > 1f) t = 1f;
                            scale = q.scale * (1f + (q.scaleFactor - 1f) * t * t);
                        }
                    }

                    RenderSystem.setShaderTexture(0, q.texture);
                    RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

                    matrices.push();
                    matrices.translate(q.position.x - camPos.x, q.position.y - camPos.y, q.position.z - camPos.z);
                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float) q.rotation.y));
                    matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion((float) q.rotation.x));
                    matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((float) q.rotation.z));

                    matrices.scale(scale, scale, scale);
                    Matrix4f matrix = matrices.peek().getPositionMatrix();
                    float hw = q.width / 2f;
                    float hh = q.height / 2f;
                    int r = 255, g = 255, b = 255;
                    int a = (int) (alpha * 255);

                    net.minecraft.client.render.BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                    buffer.begin(net.minecraft.client.render.VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

                    buffer.vertex(matrix, -hw, -hh, 0).texture(0f, 1f).color(r, g, b, a).next();
                    buffer.vertex(matrix, hw, -hh, 0).texture(1f, 1f).color(r, g, b, a).next();
                    buffer.vertex(matrix, hw, hh, 0).texture(1f, 0f).color(r, g, b, a).next();
                    buffer.vertex(matrix, -hw, hh, 0).texture(0f, 0f).color(r, g, b, a).next();

                    Tessellator.getInstance().draw();
                    matrices.pop();
                }

                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                RenderSystem.depthMask(true);
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
            }
        });
    }

    private static class Quad {
        Vec3d position;
        float width, height;
        Vec3d rotation;
        float scale;
        Identifier texture;
        int duration, maxDuration;
        int prevDuration;
        boolean fade;
        int fadeStart;
        boolean scaleUp;
        int scaleStart;
        float scaleFactor;
        float baseAlpha;

        Quad(Vec3d pos, float w, float h, Vec3d rot, float s, Identifier tex,
             int duration, boolean fade, int fadeStart,
             boolean scaleUp, int scaleStart, float scaleFactor, float baseAlpha) {
            this.position = pos;
            this.width = w;
            this.height = h;
            this.rotation = rot;
            this.scale = s;
            this.texture = tex;
            this.duration = duration;
            this.prevDuration = duration;
            this.maxDuration = duration;
            this.fade = fade;
            this.fadeStart = fadeStart;
            this.scaleUp = scaleUp;
            this.scaleStart = scaleStart;
            this.scaleFactor = scaleFactor;
            this.baseAlpha = baseAlpha;
        }
    }
}