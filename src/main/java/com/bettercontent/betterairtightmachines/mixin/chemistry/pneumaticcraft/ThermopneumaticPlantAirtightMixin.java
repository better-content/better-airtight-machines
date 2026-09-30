package com.bettercontent.betterairtightmachines.mixin.chemistry.pneumaticcraft;

import com.bettercontent.betterairtightmachines.chemistry.AirtightUpgradeHolder;
import com.bettercontent.betterairtightmachines.chemistry.GasRecipeContainment;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;
import me.desht.pneumaticcraft.common.block.entity.ThermopneumaticProcessingPlantBlockEntity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ThermopneumaticProcessingPlantBlockEntity.class, remap = false)
abstract class ThermopneumaticPlantAirtightMixin implements AirtightUpgradeHolder {
    @Unique private static final String BETTER_CONTENT_FIXES$AIRTIGHT = "better_airtight_machines:Airtight";
    @Unique private boolean betterContentFixes$airtight;
    @Shadow(remap = false) private boolean searchForRecipe;
    @Shadow(remap = false) private ThermoPlantRecipe currentRecipe;

    @Override public boolean isAirtight() { return betterContentFixes$airtight; }

    @Override
    public void betterContentFixes$setAirtight(final boolean airtight) {
        if (betterContentFixes$airtight == airtight) return;
        betterContentFixes$airtight = airtight;
        searchForRecipe = true;
        currentRecipe = null;
        ((ThermopneumaticProcessingPlantBlockEntity) (Object) this).setChanged();
    }

    @Inject(method = "findApplicableRecipe", at = @At("RETURN"), cancellable = true, remap = false)
    private void betterContentFixes$gateGasRecipe(final CallbackInfoReturnable<ThermoPlantRecipe> callback) {
        final ThermoPlantRecipe recipe = callback.getReturnValue();
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
        searchForRecipe = true;
        currentRecipe = null;
    }
}
