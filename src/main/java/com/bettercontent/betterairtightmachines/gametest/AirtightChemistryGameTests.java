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
            helper.fail("Required Better Chemlib Hazards fixture is missing");
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

    @GameTest(templateNamespace = ModMain.MOD_ID, template = "empty")
    public static void registeredInteractionInstallsAndSneakRemovesExactlyOneUpgrade(final GameTestHelper helper) {
        final var player = helper.makeMockSurvivalPlayer();
        player.getAbilities().instabuild = false;
        final Block[] supported = {AllBlocks.BASIN.get(), ModBlocks.FLUID_MIXER.get(), ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT.get()};
        for (int i = 0; i < supported.length; i++) {
            final BlockPos relative = new BlockPos(i + 1, 1, 1);
            helper.setBlock(relative, supported[i]);
            final BlockPos position = helper.absolutePos(relative);
            final var holder = (AirtightUpgradeHolder) helper.getBlockEntity(relative);
            final ItemStack upgrades = new ItemStack(ChemistryContent.AIRTIGHT_UPGRADE.get(), 2);
            player.getInventory().clearContent();
            player.setShiftKeyDown(false);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, upgrades);
            final var installed = click(player, position);
            helper.assertTrue(installed.isCanceled() && installed.getCancellationResult().consumesAction(), "Registered install interaction must be handled");
            helper.assertTrue(holder.isAirtight() && upgrades.getCount() == 1, "Installation must seal the native holder and consume exactly one upgrade");
            click(player, position);
            helper.assertTrue(holder.isAirtight() && upgrades.getCount() == 1, "Already sealed holder must not consume another upgrade");

            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            click(player, position);
            helper.assertTrue(holder.isAirtight() && player.getInventory().isEmpty(), "Ordinary empty-hand use must not remove or refund the upgrade");
            player.setShiftKeyDown(true);
            final var removed = click(player, position);
            helper.assertTrue(removed.isCanceled() && removed.getCancellationResult().consumesAction(), "Registered sneak removal must be handled");
            helper.assertTrue(!holder.isAirtight() && player.getInventory().countItem(ChemistryContent.AIRTIGHT_UPGRADE.get()) == 1,
                    "Removal must unseal and return exactly one upgrade");
            // Clear the refunded held slot before repeating the empty-hand removal attempt.
            player.getInventory().clearContent();
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            click(player, position);
            helper.assertTrue(!holder.isAirtight() && player.getInventory().isEmpty(), "Unsealed holder cannot refund a second upgrade");
        }
        final BlockPos unsupported = new BlockPos(1, 1, 2);
        helper.setBlock(unsupported, net.minecraft.world.level.block.Blocks.STONE);
        final ItemStack untouched = new ItemStack(ChemistryContent.AIRTIGHT_UPGRADE.get(), 2);
        player.setShiftKeyDown(false);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, untouched);
        final var ignored = click(player, helper.absolutePos(unsupported));
        helper.assertTrue(!ignored.isCanceled() && untouched.getCount() == 2, "Unsupported blocks must not admit or consume upgrades");
        helper.succeed();
    }

    private static net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock click(
            net.minecraft.world.entity.player.Player player, BlockPos position) {
        final var event = new net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock(
                player, net.minecraft.world.InteractionHand.MAIN_HAND, position,
                new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(position),
                        net.minecraft.core.Direction.UP, position, false));
        // Dispatch through the real registration, not directly to the interaction handler.
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
        return event;
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
