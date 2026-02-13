package com.lumii.lumenium.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class BeamRenderer {

    private static final Identifier PACKET_ID = new Identifier("lumenium", "cyl_beam");
    private static final List<Beam> beams = new ArrayList<>();

    static {
        init();
        ClientTickEvents.START_CLIENT_TICK.register(client -> tick());
    }

    // server schedule
    public static void scheduleCommon(
            ServerWorld world,
            Vec3d pos,
            float radiusX,
            float radiusZ,
            float height,
            Identifier texture,
            int duration,
            float alpha,
            boolean fade,
            int fadeStart,
            float initialScale,
            float targetScale,
            float rotationSpeed) {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);

        buf.writeFloat(radiusX);
        buf.writeFloat(radiusZ);
        buf.writeFloat(height);

        buf.writeString(texture.toString());

        buf.writeInt(duration);
        buf.writeFloat(alpha);
        buf.writeBoolean(fade);
        buf.writeInt(fadeStart);
        buf.writeFloat(initialScale);
        buf.writeFloat(targetScale);
        buf.writeFloat(rotationSpeed);

        for (ServerPlayerEntity p : world.getPlayers()) {
            ClientPlayNetworking.send(PACKET_ID, buf);
        }
    }

    // client schedule
    public static void scheduleClient(
            Vec3d pos,
            float radiusX,
            float radiusZ,
            float height,
            Identifier texture,
            int duration,
            float alpha,
            boolean fade,
            int fadeStart,
            float initialScale,
            float targetScale,
            float rotationSpeed

    ) {
        beams.add(new Beam(pos, radiusX, radiusZ, height, texture, duration, alpha, fade, fadeStart, initialScale, targetScale, rotationSpeed));
    }

    // packet init stuff
    private static void init() {
        ClientPlayNetworking.registerGlobalReceiver(PACKET_ID, (client, handler, buf, sender) -> {
            Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());

            float rx = buf.readFloat();
            float rz = buf.readFloat();
            float h  = buf.readFloat();

            Identifier texture = new Identifier(buf.readString());

            int duration = buf.readInt();
            float alpha = buf.readFloat();
            boolean fade = buf.readBoolean();
            int fadeStart = buf.readInt();
            float scale = buf.readFloat();
            float targetScale = buf.readFloat();
            float rotSpeed = buf.readFloat();

            client.execute(() ->
                    scheduleClient(pos, rx, rz, h, texture, duration, alpha, fade, fadeStart, scale, targetScale, rotSpeed)
            );
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(BeamRenderer::render);
    }

    // remove dead beams
    private static void tick() {
        beams.removeIf(b -> {
            b.prevDuration = b.duration;
            b.duration--;
            return b.duration <= 0;
        });
    }

    // render
    private static void render(WorldRenderContext ctx) {
        if (beams.isEmpty()) return;

        MatrixStack matrices = ctx.matrixStack();
        Vec3d cam = ctx.camera().getPos();
        float tickDelta = ctx.tickDelta();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        for (Beam b : beams) {
            float interp = b.prevDuration + (b.duration - b.prevDuration) * tickDelta;

            float alpha = b.alpha;
            if (b.fade && interp <= b.fadeStart) {
                float t = interp / b.fadeStart;
                if (t < 0) t = 0;
                if (t > 1) t = 1;
                alpha *= t;
            }

            Vec3d renderPos = b.pos.subtract(cam);

            matrices.push();

            matrices.translate(renderPos.x, renderPos.y, renderPos.z);

            b.rotation += b.rotSpeed * tickDelta;
            matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(b.rotation));

            float lifeProgress = 1f - ((float)b.duration / (float)b.maxDuration);
            float scale = b.initialScale;

            if (b.scaleUp) {
            
                int ticksLived = b.maxDuration - b.duration;
                if (ticksLived < b.scaleStart) {
                    scale = b.initialScale; 
                } else {
                    float t = (ticksLived - b.scaleStart) / (float)(b.maxDuration - b.scaleStart);
                    t = MathHelper.clamp(t, 0f, 1f);
                    scale = b.initialScale + (b.targetScale *= b.initialScale) * t;
                }
            } else {
            
                scale = b.initialScale + (b.initialScale + b.targetScale) * lifeProgress;
            }

            matrices.scale(scale, scale, scale);


            drawCylinder(
                    matrices.peek().getPositionMatrix(),
                    b.radiusX,
                    b.radiusZ,
                    b.height,
                    b.texture,
                    alpha
            );

            matrices.pop();
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    // cylinder
    private static void drawCylinder(Matrix4f m, float rx, float rz, float height, Identifier texture, float alpha) {
        final int slices = 32;

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        BufferBuilder buf = Tessellator.getInstance().getBuffer();
        buf.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        int a = (int)(alpha * 255);

        for (int i = 0; i < slices; i++) {
            float a0 = (float)(2 * Math.PI * i / slices);
            float a1 = (float)(2 * Math.PI * (i + 1) / slices);

            float x0 = (float)Math.cos(a0) * rx;
            float z0 = (float)Math.sin(a0) * rz;

            float x1 = (float)Math.cos(a1) * rx;
            float z1 = (float)Math.sin(a1) * rz;

            buf.vertex(m, x0, 0,  z0).texture((float)i/slices, 1).color(255,255,255,a).next();
            buf.vertex(m, x1, 0,  z1).texture((float)(i+1)/slices,1).color(255,255,255,a).next();
            buf.vertex(m, x1, height, z1).texture((float)(i+1)/slices,0).color(255,255,255,a).next();
            buf.vertex(m, x0, height, z0).texture((float)i/slices,0).color(255,255,255,a).next();
        }

        Tessellator.getInstance().draw();
    }

    // data
    private static class Beam {
        Vec3d pos;

        float radiusX;
        float radiusZ;
        float height;

        Identifier texture;

        int duration;
        int prevDuration;
        int maxDuration;

        float alpha;
        boolean fade;
        int fadeStart;

        float scale;
        float rotation;
        float rotSpeed;
        float targetScale;
        float initialScale;
        boolean scaleUp = false;
        int scaleStart = 0;

        Beam(Vec3d pos, float rx, float rz, float height,
             Identifier tex, int duration,
             float alpha, boolean fade, int fadeStart,
             float initialScale, float rotationSpeed, float targetScale) {

            this.pos = pos;
            this.radiusX = rx;
            this.radiusZ = rz;
            this.height = height;

            this.texture = tex;

            this.duration = duration;
            this.prevDuration = duration;
            this.maxDuration = duration;

            this.alpha = alpha;
            this.fade = fade;
            this.fadeStart = fadeStart;

            this.initialScale = initialScale;
            this.scale = initialScale;
            this.rotSpeed = rotationSpeed;
            this.rotation = 0;
            this.targetScale = targetScale;

        }
    }
}
