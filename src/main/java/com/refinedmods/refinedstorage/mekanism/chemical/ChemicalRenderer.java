package com.refinedmods.refinedstorage.mekanism.chemical;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import mekanism.api.chemical.Chemical;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public final class ChemicalRenderer {
    private ChemicalRenderer() {
    }

    public static void render(final PoseStack poseStack, final int x, final int y, final Chemical chemical) {
        final int packedRgb = chemical.getTint();
        final Minecraft minecraft = Minecraft.getInstance();
        final ResourceLocation icon = chemical.getIcon();
        final TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(icon);
        render(poseStack, x, y, packedRgb, sprite);
    }

    public static void render(final PoseStack poseStack,
                              final MultiBufferSource renderTypeBuffer,
                              final int light,
                              final Chemical chemical) {
        final int packedRgb = chemical.getTint();
        final Minecraft minecraft = Minecraft.getInstance();
        final ResourceLocation icon = chemical.getIcon();
        final TextureAtlasSprite sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(icon);
        render(poseStack, renderTypeBuffer, light, packedRgb, sprite);
    }

    private static void render(final PoseStack poseStack,
                               final int x,
                               final int y,
                               final int packedRgb,
                               final TextureAtlasSprite sprite) {
        RenderSystem.setShaderTexture(0, sprite.atlasLocation());
        final int r = packedRgb >> 16 & 255;
        final int g = packedRgb >> 8 & 255;
        final int b = packedRgb & 255;
        final int slotXEnd = x + 16;
        final int slotYEnd = y + 16;
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        final Tesselator tesselator = Tesselator.getInstance();
        final BufferBuilder bufferBuilder = tesselator.begin(
            VertexFormat.Mode.QUADS,
            DefaultVertexFormat.POSITION_TEX_COLOR
        );
        bufferBuilder.addVertex(poseStack.last().pose(), (float) x, (float) slotYEnd, 0.0F)
            .setUv(sprite.getU0(), sprite.getV1()).setColor(r, g, b, 255);
        bufferBuilder.addVertex(poseStack.last().pose(), (float) slotXEnd, (float) slotYEnd, 0.0F)
            .setUv(sprite.getU1(), sprite.getV1()).setColor(r, g, b, 255);
        bufferBuilder.addVertex(poseStack.last().pose(), (float) slotXEnd, (float) y, 0.0F)
            .setUv(sprite.getU1(), sprite.getV0()).setColor(r, g, b, 255);
        bufferBuilder.addVertex(poseStack.last().pose(), (float) x, (float) y, 0.0F)
            .setUv(sprite.getU0(), sprite.getV0()).setColor(r, g, b, 255);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
    }

    private static void render(final PoseStack poseStack,
                               final MultiBufferSource buffers,
                               final int light,
                               final int packedRgb,
                               final TextureAtlasSprite sprite) {
        final VertexConsumer buffer = buffers.getBuffer(RenderType.solid());
        final float scale = 0.3F;
        // y is flipped here
        final var x0 = -scale / 2;
        final var y0 = scale / 2;
        final var x1 = scale / 2;
        final var y1 = -scale / 2;
        final var transform = poseStack.last().pose();
        buffer.addVertex(transform, x0, y1, 0)
            .setColor(packedRgb)
            .setUv(sprite.getU0(), sprite.getV1())
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(0, 0, 1);
        buffer.addVertex(transform, x1, y1, 0)
            .setColor(packedRgb)
            .setUv(sprite.getU1(), sprite.getV1())
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(0, 0, 1);
        buffer.addVertex(transform, x1, y0, 0)
            .setColor(packedRgb)
            .setUv(sprite.getU1(), sprite.getV0())
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(0, 0, 1);
        buffer.addVertex(transform, x0, y0, 0)
            .setColor(packedRgb)
            .setUv(sprite.getU0(), sprite.getV0())
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(0, 0, 1);
    }
}
