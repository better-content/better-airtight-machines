package com.bettercontent.betterairtightmachines.mixin.chemistry.create;

import com.bettercontent.betterchemlibhazards.api.AirtightInventory;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BasinRenderer.class, remap = false)
abstract class BasinRendererMixin {
    @Inject(method = "renderSafe(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At("TAIL"))
    private void betterContentFixes$renderAirtightLid(final BasinBlockEntity basin, final float partialTicks,
                                                      final PoseStack poseStack, final MultiBufferSource buffers,
                                                      final int light, final int overlay, final CallbackInfo callback) {
        if (!(basin instanceof AirtightInventory inventory) || !inventory.isAirtight()) return;
        poseStack.pushPose();
        poseStack.translate(0.0, 0.8125, 0.0);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                Blocks.IRON_TRAPDOOR.defaultBlockState(), poseStack, buffers, light, overlay);
        poseStack.popPose();
    }
}
