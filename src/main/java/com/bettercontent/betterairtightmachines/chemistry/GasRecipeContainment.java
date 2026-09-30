package com.bettercontent.betterairtightmachines.chemistry;

import com.bettercontent.betterchemlibhazards.sim.GasFluidCodec;
import com.smashingmods.chemlib.api.Chemical;
import com.smashingmods.chemlib.api.MatterState;
import java.util.Arrays;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.FluidMixerRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

public final class GasRecipeContainment {
    private GasRecipeContainment() {
    }

    public static boolean requiresAirtight(final ThermoPlantRecipe recipe) {
        return containsGas(recipe.getInputItem())
                || containsGas(recipe.getInputFluid())
                || containsGas(recipe.getOutputItem())
                || containsGas(recipe.getOutputFluid());
    }

    public static boolean requiresAirtight(final FluidMixerRecipe recipe) {
        return containsGas(recipe.getInput1())
                || containsGas(recipe.getInput2())
                || containsGas(recipe.getOutputItem())
                || containsGas(recipe.getOutputFluid());
    }

    static boolean containsGas(final Ingredient ingredient) {
        return Arrays.stream(ingredient.getItems()).anyMatch(GasRecipeContainment::containsGas);
    }

    static boolean containsGas(final FluidIngredient ingredient) {
        return ingredient.getFluidStacks().stream().anyMatch(GasRecipeContainment::containsGas);
    }

    public static boolean containsGas(final ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof Chemical chemical
                && chemical.getMatterState() == MatterState.GAS;
    }

    public static boolean containsGas(final FluidStack stack) {
        return !stack.isEmpty() && GasFluidCodec.isGasFluid(stack.getFluid());
    }
}
