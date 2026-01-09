package com.lumii.lumenium.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
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
public final class CubeRenderer {

    private static final List<Cube> queuedCubes = new ArrayList<>();
    private static final List<Cube> cubesToRemove = new ArrayList<>();
    private static int tickCounter = 0;
    private static float partialTicks = 0f;

    private static final Identifier CUBE_PACKET_ID = new Identifier("tritium", "cube_render");

    static {
        init();
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            clientTick();
        });
    }

    /**
     * Schedule a cube to be rendered on the server and sent to all clients.
     * Call this from server-side code.
     */
    public static void scheduleCommon(ServerWorld world, Vec3d pos, float width, float height, float depth,
                                      Vec3d rotation, float scale, Identifier texture,
                                      int duration, boolean fade, int fadeStart,
                                      boolean scaleUp, int scaleStart, float scaleFactor,
                                      float alpha) {

        PacketByteBuf buf = PacketByteBufs.create();

        // Write position
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);

        // Write dimensions
        buf.writeFloat(width);
        buf.writeFloat(height);
        buf.writeFloat(depth);

        // Write rotation
        buf.writeDouble(rotation.x);
        buf.writeDouble(rotation.y);
        buf.writeDouble(rotation.z);

        // Write scale and texture
        buf.writeFloat(scale);
        buf.writeString(texture.toString());

        // Write duration and fade settings
        buf.writeInt(duration);
        buf.writeBoolean(fade);
        buf.writeInt(fadeStart);

        // Write scale settings
        buf.writeBoolean(scaleUp);
        buf.writeInt(scaleStart);
        buf.writeFloat(scaleFactor);

        // Write alpha
        buf.writeFloat(alpha);

        // Send to all players in the world
        for (ServerPlayerEntity player : world.getPlayers()) {
            ServerPlayNetworking.send(player, CUBE_PACKET_ID, buf);
        }
    }

    /**
     * Schedule a cube to be rendered only on the client.
     * Call this from client-side code.
     */
    public static void scheduleClient(Vec3d pos, float width, float height, float depth,
                                      Vec3d rotation, float scale, Identifier texture,
                                      int duration, boolean fade, int fadeStart,
                                      boolean scaleUp, int scaleStart, float scaleFactor,
                                      float alpha) {
        if (duration <= 0) return;
        synchronized (queuedCubes) {
            queuedCubes.add(new Cube(pos, width, height, depth, rotation, scale, texture, duration,
                    fade, fadeStart, scaleUp, scaleStart, scaleFactor, alpha));
        }
    }

    private static void clientTick() {
        synchronized (queuedCubes) {
            if (queuedCubes.isEmpty()) return;

            tickCounter++;
            cubesToRemove.clear();
            for (Cube cube : queuedCubes) {
                if (!MinecraftClient.getInstance().isPaused() || !MinecraftClient.getInstance().isInSingleplayer()) {
                    cube.prevDuration = cube.duration;
                    cube.duration--;
                }
                if (cube.duration <= 0) {
                    cubesToRemove.add(cube);
                }
            }
            queuedCubes.removeAll(cubesToRemove);
        }
    }

    private static void init() {
        ClientPlayNetworking.registerGlobalReceiver(CUBE_PACKET_ID,
                (client, handler, buf, responseSender) -> {

                    double x = buf.readDouble();
                    double y = buf.readDouble();
                    double z = buf.readDouble();
                    Vec3d pos = new Vec3d(x, y, z);

                    float width = buf.readFloat();
                    float height = buf.readFloat();
                    float depth = buf.readFloat();

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
                        scheduleClient(pos, width, height, depth, rotation, scale, texture,
                                duration, fade, fadeStart, scaleUp, scaleStart, scaleFactor, alpha);
                    });
                }
        );

        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            synchronized (queuedCubes) {
                if (queuedCubes.isEmpty()) return;

                partialTicks = context.tickDelta();

                MatrixStack matrices = context.matrixStack();
                Vec3d camPos = context.camera().getPos();

                List<Cube> sortedCubes = new ArrayList<>(queuedCubes);
                sortedCubes.sort((a, b) -> {
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

                for (Cube cube : sortedCubes) {
                    float interpolatedDuration = cube.prevDuration + (cube.duration - cube.prevDuration) * partialTicks;
                    float interpolatedTicksLived = cube.maxDuration - interpolatedDuration;

                    float alpha = cube.baseAlpha;
                    if (cube.fade) {
                        if (interpolatedTicksLived >= cube.fadeStart) {
                            int fadeTicks = cube.maxDuration - cube.fadeStart;
                            float remainingDuration = Math.max(0, interpolatedDuration - cube.fadeStart);
                            alpha = cube.baseAlpha * (remainingDuration / fadeTicks);
                            if (alpha < 0f) alpha = 0f;
                        }
                    }

                    float scale = cube.scale;
                    if (cube.scaleUp) {
                        if (interpolatedTicksLived >= cube.scaleStart) {
                            float t = (interpolatedTicksLived - cube.scaleStart) / (cube.maxDuration - cube.scaleStart);
                            if (t > 1f) t = 1f;
                            scale = cube.scale * (1f + (cube.scaleFactor - 1f) * t * t);
                        }
                    }

                    RenderSystem.setShaderTexture(0, cube.texture);
                    RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

                    matrices.push();
                    matrices.translate(cube.position.x - camPos.x, cube.position.y - camPos.y, cube.position.z - camPos.z);
                    matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float) cube.rotation.y));
                    matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion((float) cube.rotation.x));
                    matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion((float) cube.rotation.z));

                    matrices.scale(scale, scale, scale);

                    Matrix4f matrix = matrices.peek().getPositionMatrix();
                    float hw = cube.width / 2f;
                    float hh = cube.height / 2f;
                    float hd = cube.depth / 2f;
                    int r = 255, g = 255, b = 255;
                    int a = (int) (alpha * 255);

                    BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                    buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

                    // Front face
                    buffer.vertex(matrix, -hw, -hh, -hd).texture(0f, 1f).color(r, g, b, a).next();
                    buffer.vertex(matrix, hw, -hh, -hd).texture(1f, 1f).color(r, g, b, a).next();
                    buffer.vertex(matrix, hw, hh, -hd).texture(1f, 0f).color(r, g, b, a).next();
                    buffer.vertex(matrix, -hw, hh, -hd).texture(0f, 0f).color(r, g, b, a).next();

                    // Back face
                    buffer.vertex(matrix, -hw, -hh, hd).texture(1f, 1f).color(r, g, b, a).next();
                    buffer.vertex(matrix, -hw, hh, hd).texture(1f, 0f).color(r, g, b, a).next();
                    buffer.vertex(matrix, hw, hh, hd).texture(0f, 0f).color(r, g, b, a).next();
                    buffer.vertex(matrix, hw, -hh, hd).texture(0f, 1f).color(r, g, b, a).next();

                    // Top face
                    buffer.vertex(matrix, -hw, hh, -hd).texture(0f, 1f).color(r, g, b, a).next();
                    buffer.vertex(matrix, hw, hh, -hd).texture(1f, 1f).color(r, g, b, a).next();
                    buffer.vertex(matrix, hw, hh, hd).texture(1f, 0f).color(r, g, b, a).next();
                    buffer.vertex(matrix, -hw, hh, hd).texture(0f, 0f).color(r, g, b, a).next();

                    // Bottom face
                    buffer.vertex(matrix, -hw, -hh, -hd).texture(0f, 0f).color(r, g, b, a).next();
                    buffer.vertex(matrix, -hw, -hh, hd).texture(0f, 1f).color(r, g, b, a).next();
                    buffer.vertex(matrix, hw, -hh, hd).texture(1f, 1f).color(r, g, b, a).next();
                    buffer.vertex(matrix, hw, -hh, -hd).texture(1f, 0f).color(r, g, b, a).next();

                    // Right face
                    buffer.vertex(matrix, hw, -hh, -hd).texture(0f, 1f).color(r, g, b, a).next();
                    buffer.vertex(matrix, hw, -hh, hd).texture(1f, 1f).color(r, g, b, a).next();
                    buffer.vertex(matrix, hw, hh, hd).texture(1f, 0f).color(r, g, b, a).next();
                    buffer.vertex(matrix, hw, hh, -hd).texture(0f, 0f).color(r, g, b, a).next();

                    // Left face
                    buffer.vertex(matrix, -hw, -hh, -hd).texture(1f, 1f).color(r, g, b, a).next();
                    buffer.vertex(matrix, -hw, hh, -hd).texture(1f, 0f).color(r, g, b, a).next();
                    buffer.vertex(matrix, -hw, hh, hd).texture(0f, 0f).color(r, g, b, a).next();
                    buffer.vertex(matrix, -hw, -hh, hd).texture(0f, 1f).color(r, g, b, a).next();

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

    private static class Cube {
        Vec3d position;
        float width, height, depth;
        Vec3d rotation;
        float scale;
        Identifier texture;
        int duration, maxDuration;
        int prevDuration; // For interpolation
        boolean fade;
        int fadeStart;
        boolean scaleUp;
        int scaleStart;
        float scaleFactor;
        float baseAlpha;

        Cube(Vec3d pos, float w, float h, float d, Vec3d rot, float s, Identifier tex,
             int duration, boolean fade, int fadeStart,
             boolean scaleUp, int scaleStart, float scaleFactor, float baseAlpha) {
            this.position = pos;
            this.width = w;
            this.height = h;
            this.depth = d;
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