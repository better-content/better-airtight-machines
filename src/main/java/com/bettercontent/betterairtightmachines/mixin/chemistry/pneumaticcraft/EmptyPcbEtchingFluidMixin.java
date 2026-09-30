package com.bettercontent.betterairtightmachines.mixin.chemistry.pneumaticcraft;

import me.desht.pneumaticcraft.common.item.EmptyPCBItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EmptyPCBItem.class, remap = false)
abstract class EmptyPcbEtchingFluidMixin {
    @Inject(method = "getEtchingFluid", at = @At("HEAD"), cancellable = true, remap = false)
    private static void betterContentFixes$useNitricAcid(final CallbackInfoReturnable<FluidStack> callback) {
        final Fluid nitricAcid = ForgeRegistries.FLUIDS.getValue(
                ResourceLocation.fromNamespaceAndPath("chemlib", "nitric_acid_fluid"));
        if (nitricAcid != null) {
            callback.setReturnValue(new FluidStack(nitricAcid, 1_000));
        }
    }
}
