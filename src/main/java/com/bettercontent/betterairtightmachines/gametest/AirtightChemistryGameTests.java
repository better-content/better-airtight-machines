package com.bettercontent.betterairtightmachines.gametest;

import com.bettercontent.betterairtightmachines.ModMain;
import com.bettercontent.betterairtightmachines.chemistry.AirtightUpgradeHolder;
import com.bettercontent.betterairtightmachines.chemistry.ChemistryContent;
import com.bettercontent.betterairtightmachines.chemistry.GasRecipeContainment;
import com.simibubi.create.AllBlocks;
import me.desht.pneumaticcraft.common.core.ModBlocks;
import me.desht.pneumaticcraft.common.item.EmptyPCBItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@PrefixGameTestTemplate(false)
public final class AirtightChemistryGameTests {
    private AirtightChemistryGameTests() {
    }

    @GameTest(templateNamespace = ModMain.MOD_ID, template = "empty")
    public static void supportedMachinesPersistAirtightState(final GameTestHelper helper) {
        if (!ModList.get().isLoaded("better_chemlib_hazards")) {
            helper.succeed();
            return;
        }
        verifyPersistence(helper, new BlockPos(1, 1, 1), AllBlocks.BASIN.get());
        verifyPersistence(helper, new BlockPos(2, 1, 1), ModBlocks.FLUID_MIXER.get());
        verifyPersistence(helper, new BlockPos(3, 1, 1), ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get());
        helper.succeed();
    }

    @GameTest(templateNamespace = ModMain.MOD_ID, template = "empty")
    public static void pcbEtchingUsesChemLibNitricAcid(final GameTestHelper helper) {
        final ResourceLocation fluidId = ForgeRegistries.FLUIDS.getKey(EmptyPCBItem.getEtchingFluid().getFluid());
        if (!ResourceLocation.fromNamespaceAndPath("chemlib", "nitric_acid_fluid").equals(fluidId)) {
            helper.fail("Empty PCB etching fluid was " + fluidId + " instead of ChemLib nitric acid");
            return;
        }
        if (!ForgeRegistries.ITEMS.containsKey(ResourceLocation.fromNamespaceAndPath(
                ModMain.MOD_ID, "airtight_upgrade")) || !ChemistryContent.AIRTIGHT_UPGRADE.isPresent()) {
            helper.fail("Airtight upgrade item was not registered");
            return;
        }
        helper.succeed();
    }

    @GameTest(templateNamespace = ModMain.MOD_ID, template = "empty")
    public static void chemLibGasFormsRequireContainment(final GameTestHelper helper) {
        final var hydrogen = ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath("chemlib", "hydrogen"));
        final var vanadium = ForgeRegistries.ITEMS.getValue(
                ResourceLocation.fromNamespaceAndPath("chemlib", "vanadium"));
        final var oxygen = ForgeRegistries.FLUIDS.getValue(
                ResourceLocation.fromNamespaceAndPath("chemlib", "oxygen_fluid"));
        if (hydrogen == null || vanadium == null || oxygen == null
                || !GasRecipeContainment.containsGas(new ItemStack(hydrogen))
                || GasRecipeContainment.containsGas(new ItemStack(vanadium))
                || !GasRecipeContainment.containsGas(new FluidStack(oxygen, 250))) {
            helper.fail("ChemLib gas-state recipe detection did not distinguish sealed inputs");
            return;
        }
        helper.succeed();
    }

    private static void verifyPersistence(final GameTestHelper helper, final BlockPos relativePos, final Block block) {
        helper.setBlock(relativePos, block);
        final BlockEntity blockEntity = helper.getBlockEntity(relativePos);
        if (!(blockEntity instanceof AirtightUpgradeHolder holder)) {
            helper.fail(ForgeRegistries.BLOCKS.getKey(block) + " does not expose the airtight contract");
            return;
        }
        holder.betterContentFixes$setAirtight(true);
        final CompoundTag saved = blockEntity.saveWithFullMetadata();
        if (!saved.getBoolean("better_airtight_machines:Airtight")) {
            helper.fail(ForgeRegistries.BLOCKS.getKey(block) + " did not write airtight state");
            return;
        }
        holder.betterContentFixes$setAirtight(false);
        blockEntity.load(saved);
        if (!holder.isAirtight()) {
            helper.fail(ForgeRegistries.BLOCKS.getKey(block) + " did not restore airtight state");
        }
    }
}
