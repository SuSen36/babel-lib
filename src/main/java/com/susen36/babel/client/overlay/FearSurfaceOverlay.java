package com.susen36.babel.client.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Objects;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = BabelMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FearSurfaceOverlay {
    private static final int WIDTH = 320;
    private static final int HEIGHT = 180;
    private static final float FULL_ALPHA = 175.0F;
    private static final float BASE_MIN_FRACTION = 0.5F;
    private static final float BASE_MAX_FRACTION = 0.6F;
    private static final float GAMMA_FACTOR = 0.45F;
    private static final float PULSE_SPEED = (float) (Math.PI * 0.025);
    private static final float TAU = (float) (Math.PI * 2.0);
    private static final int SEED = 20090716;
    private static final int ANIM_INTERVAL = 2;
    private static final float TIME_SCALE = 0.35F;
    private static final float STROKE_SPACING = 13.0F;
    private static final float GOLDEN_ANGLE = 2.399963F;

    private static final int[] PALETTE = {0x7700D5, 0x8F00F3, 0xAA0EFF, 0xC94DFF, 0xE46FFF, 0xF887FF};
    private static final int WHITE = 0xFFFFFF;

    private static final int[] PERM = new int[512];

    static {
        RandomSource random = RandomSource.create(SEED);
        for (int i = 0; i < 256; i++) {
            PERM[i] = i;
        }
        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swap = PERM[i];
            PERM[i] = PERM[j];
            PERM[j] = swap;
        }
        for (int i = 0; i < 256; i++) {
            PERM[i + 256] = PERM[i];
        }
    }

    @Nullable
    private static ResourceLocation texture;
    @Nullable
    private static DynamicTexture fearTexture;
    private static int lastAnimTick;

    @SubscribeEvent
    public static void registerOverlay(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.AIR_LEVEL.id(),
                "fear_surface",
                (gui, guiGraphics, partialTick, width, height) -> renderFearSurface(guiGraphics, width, height, partialTick));
    }

    private static void renderFearSurface(GuiGraphics guiGraphics, int w, int h, float partialTick) {
        Player player = Minecraft.getInstance().player;
        MobEffectInstance fear = player == null ? null : player.getEffect(BabelMobEffects.FEAR.get());
        if (player != null && fear != null) {
            animateTexture(player);
            ResourceLocation surfaceTexture = texture;
            if (surfaceTexture != null) {
                float alpha = computeAlpha(fear, player, partialTick);
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
                guiGraphics.blit(surfaceTexture, 0, 0, w, h, 0.0F, 0.0F, WIDTH, HEIGHT, WIDTH, HEIGHT);
                RenderSystem.depthMask(true);
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                RenderSystem.disableBlend();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    private static float computeAlpha(MobEffectInstance fear, Player player, float partialTick) {
        float darknessScale = Minecraft.getInstance().options.darknessEffectScale().get().floatValue();
        float effectFactor = fear.getFactorData().map(data -> data.getFactor(player, partialTick)).orElse(0.0F);
        float gamma = notGamma(effectFactor) * darknessScale;
        float pulse = darknessPulse(player, partialTick) * gamma * darknessScale;
        float gammaOffset = Math.max(0.0F, Minecraft.getInstance().options.gamma().get().floatValue() - gamma);
        return Mth.lerp(Mth.clamp(pulse + gammaOffset, 0.0F, 1.0F), BASE_MIN_FRACTION, BASE_MAX_FRACTION);
    }

    private static float notGamma(float value) {
        float remaining = 1.0F - Mth.clamp(value, 0.0F, 1.0F);
        return 1.0F - remaining * remaining * remaining * remaining;
    }

    private static float darknessPulse(Player player, float partialTick) {
        return Math.max(0.0F, Mth.cos((player.tickCount - partialTick) * PULSE_SPEED));
    }

    private static void animateTexture(Player player) {
        if (fearTexture == null) {
            createTexture();
            lastAnimTick = player.tickCount;
        } else if (player.tickCount - lastAnimTick >= ANIM_INTERVAL) {
            lastAnimTick = player.tickCount;
            fillPixels(Objects.requireNonNull(fearTexture.getPixels(), "dynamicTexture pixels"), player.tickCount * TIME_SCALE);
            fearTexture.upload();
        }
    }

    private static void createTexture() {
        DynamicTexture dynamicTexture = new DynamicTexture(WIDTH, HEIGHT, true);
        dynamicTexture.setFilter(true, false);
        fillPixels(Objects.requireNonNull(dynamicTexture.getPixels(), "dynamicTexture pixels"), 0.0F);
        dynamicTexture.upload();
        ResourceLocation location = new ResourceLocation(BabelMod.MODID, "fear_surface_dynamic");
        Minecraft.getInstance().getTextureManager().register(location, dynamicTexture);
        fearTexture = dynamicTexture;
        texture = location;
    }

    private static void fillPixels(NativeImage image, float time) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                image.setPixelRGBA(x, y, computePixel(x, y, time));
            }
        }
    }

    private static int computePixel(int x, int y, float time) {
        float distLeft = x + 1.0F;
        float distRight = WIDTH - x;
        float distTop = y + 1.0F;
        float distBottom = HEIGHT - y;
        float edgeY = distLeft < distRight ? y : HEIGHT - 1 - y;
        float edgeX = distTop < distBottom ? x : WIDTH - 1 - x;
        float corner = Math.max(Math.max(cornerBoost(distLeft, distTop), cornerBoost(distRight, distTop)), Math.max(cornerBoost(distLeft, distBottom), cornerBoost(distRight, distBottom)));
        float turbulence = fbm(x * 0.02F + time * 0.05F, y * 0.02F - time * 0.04F) * 9.0F;
        boolean vertical;
        float dist;
        float alongEdge;
        if (distLeft * HEIGHT <= Math.min(distRight * HEIGHT, Math.min(distTop * WIDTH, distBottom * WIDTH))) {
            vertical = true;
            dist = distLeft;
            alongEdge = y;
        } else if (distRight * HEIGHT <= Math.min(distLeft * HEIGHT, Math.min(distTop * WIDTH, distBottom * WIDTH))) {
            vertical = true;
            dist = distRight;
            alongEdge = y;
        } else if (distTop * WIDTH <= distBottom * WIDTH) {
            vertical = false;
            dist = distTop;
            alongEdge = x;
        } else {
            vertical = false;
            dist = distBottom;
            alongEdge = x;
        }
        float tooth = 2.0F * Math.abs((alongEdge % 12.0F) / 12.0F - 0.5F);
        float depth = Math.max(Math.max(edgeWave(edgeY, HEIGHT), edgeWave(edgeX, WIDTH)), corner)
                + turbulence + 2.5F * (1.0F - tooth);
        int packed = 0;
        if (dist <= depth) {
            float along = vertical ? y : x;
            float cross = vertical ? x : y;
            float seed = vertical ? 0.0F : 3.7F;
            float span = vertical ? WIDTH : HEIGHT;
            float best = Float.MAX_VALUE;
            int bestStroke = 0;
            for (int k = -1; k <= (int) (span / STROKE_SPACING) + 2; k++) {
                float phase = k * GOLDEN_ANGLE + seed + time * 0.03F;
                float center = 6.5F + k * STROKE_SPACING
                        + 3.0F * Mth.sin(TAU * (along + phase * 7.0F - time * 4.0F) / 30.0F)
                        + 1.4F * Mth.sin(TAU * (along + phase * 11.0F - time * 6.0F) / 13.0F)
                        + fbm((k * 3.1F + seed) * 0.15F + time * 0.06F, along * 0.05F) * 5.0F;
                float d = Math.abs(cross - center);
                if (d < best) {
                    best = d;
                    bestStroke = k;
                }
            }
            float halfWidth = Math.max(1.4F, 2.0F + fbm(bestStroke * 5.7F + time * 0.1F, along * 0.08F + (vertical ? 0.0F : 9.3F) + time * 0.07F) * 1.2F);
            if (best <= halfWidth) {
                packed = strokeColor(bestStroke, best / halfWidth, (depth - dist) / 10.0F);
            }
        }
        return packed;
    }

    private static int strokeColor(int stroke, float core, float edgeFade) {
        int color = Math.floorMod(stroke, 7) == 1 ? WHITE : PALETTE[Math.floorMod(stroke, PALETTE.length)];
        int alpha = Math.round(FULL_ALPHA * (1.0F - core * 0.55F) * Math.min(1.0F, edgeFade));
        int packed = 0;
        if (alpha > 0) {
            packed = (alpha << 24) | color;
        }
        return packed;
    }

    private static float edgeWave(float edge, float span) {
        return 20.0F + Math.max(0.0F, 10.0F * Mth.sin(TAU * edge / span) + 8.0F * Mth.sin(TAU * edge / (span / 3.0F) + 1.2F));
    }

    private static float cornerBoost(float dEdge, float dSide) {
        return Math.max(0.0F, 60.0F - Mth.sqrt(Mth.square(dEdge - 30.0F) + Mth.square(dSide - 30.0F))) * 1.1F;
    }

    private static float fbm(float x, float y) {
        float total = 0.0F;
        float amp = 1.0F;
        float freq = 1.0F;
        for (int i = 0; i < 3; i++) {
            int xi = Mth.floor(x * freq);
            int yi = Mth.floor(y * freq);
            float xf = x * freq - xi;
            float yf = y * freq - yi;
            float u = xf * xf * xf * (xf * (xf * 6.0F - 15.0F) + 10.0F);
            float v = yf * yf * yf * (yf * (yf * 6.0F - 15.0F) + 10.0F);
            int cellX = xi & 255;
            int cellY = yi & 255;
            int aa = PERM[PERM[cellX] + cellY] & 7;
            int ab = PERM[PERM[cellX] + cellY + 1] & 7;
            int ba = PERM[PERM[cellX + 1] + cellY] & 7;
            int bb = PERM[PERM[cellX + 1] + cellY + 1] & 7;
            float l = grad(aa, xf, yf) * (1.0F - u) + grad(ba, xf - 1.0F, yf) * u;
            float m = grad(ab, xf, yf - 1.0F) * (1.0F - u) + grad(bb, xf - 1.0F, yf - 1.0F) * u;
            total += (l * (1.0F - v) + m * v) * amp;
            freq *= 2.0F;
            amp *= 0.5F;
        }
        return total;
    }

    private static float grad(int hash, float x, float y) {
        int h = hash & 7;
        float u = (h & 1) == 0 ? x : -x;
        float v = (h & 2) == 0 ? y : -y;
        return (h & 4) == 0 ? u + 2.0F * v : u - 2.0F * v;
    }
}
