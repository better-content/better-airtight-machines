package com.bettercontent.betterairtightmachines.mixin.chemistry.pneumaticcraft;

import com.bettercontent.betterairtightmachines.chemistry.AirtightUpgradeHolder;
import com.bettercontent.betterairtightmachines.chemistry.GasRecipeContainment;
import me.desht.pneumaticcraft.api.crafting.recipe.FluidMixerRecipe;
import me.desht.pneumaticcraft.common.block.entity.FluidMixerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidMixerBlockEntity.class, remap = false)
abstract class FluidMixerAirtightMixin implements AirtightUpgradeHolder {
    @Unique private static final String BETTER_CONTENT_FIXES$AIRTIGHT = "better_airtight_machines:Airtight";
    @Unique private boolean betterContentFixes$airtight;
    @Shadow(remap = false) private boolean searchRecipes;
    @Shadow(remap = false) private FluidMixerRecipe currentRecipe;

    @Override public boolean isAirtight() { return betterContentFixes$airtight; }

    @Override
    public void betterContentFixes$setAirtight(final boolean airtight) {
        if (betterContentFixes$airtight == airtight) return;
        betterContentFixes$airtight = airtight;
        searchRecipes = true;
        currentRecipe = null;
        ((FluidMixerBlockEntity) (Object) this).setChanged();
    }

    @Inject(method = "findApplicableRecipe", at = @At("RETURN"), cancellable = true, remap = false)
    private void betterContentFixes$gateGasRecipe(final CallbackInfoReturnable<FluidMixerRecipe> callback) {
        final FluidMixerRecipe recipe = callback.getReturnValue();
        if (!betterContentFixes$airtight && recipe != null && GasRecipeContainment.requiresAirtight(recipe)) {
            callback.setReturnValue(null);
        }
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"), remap = false)
    private void betterContentFixes$saveAirtight(final CompoundTag tag, final CallbackInfo callback) {
        tag.putBoolean(BETTER_CONTENT_FIXES$AIRTIGHT, betterContentFixes$airtight);
    }

    @Inject(method = "load", at = @At("TAIL"), remap = false)
    private void betterContentFixes$loadAirtight(final CompoundTag tag, final CallbackInfo callback) {
        betterContentFixes$airtight = tag.getBoolean(BETTER_CONTENT_FIXES$AIRTIGHT);
        searchRecipes = true;
        currentRecipe = null;
    }
}
