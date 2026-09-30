package com.bettercontent.betterairtightmachines.chemistry;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class AirtightUpgradeInteraction {
    private AirtightUpgradeInteraction() {
    }

    @SubscribeEvent
    public static void onRightClick(final PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) {
            return;
        }
        final BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (!(blockEntity instanceof AirtightUpgradeHolder holder)) {
            return;
        }
        final ItemStack held = event.getItemStack();
        if (held.is(ChemistryContent.AIRTIGHT_UPGRADE.get()) && !holder.isAirtight()) {
            if (!event.getLevel().isClientSide()) {
                holder.betterContentFixes$setAirtight(true);
                if (!event.getEntity().getAbilities().instabuild) {
                    held.shrink(1);
                }
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
            event.setCanceled(true);
        } else if (held.isEmpty() && event.getEntity().isShiftKeyDown() && holder.isAirtight()) {
            if (!event.getLevel().isClientSide()) {
                holder.betterContentFixes$setAirtight(false);
                final ItemStack refund = new ItemStack(ChemistryContent.AIRTIGHT_UPGRADE.get());
                if (!event.getEntity().getInventory().add(refund)) {
                    Block.popResource(event.getLevel(), event.getPos(), refund);
                }
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBreak(final BlockEvent.BreakEvent event) {
        if (event.getPlayer().getAbilities().instabuild) {
            return;
        }
        final BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (event.getLevel() instanceof Level level
                && blockEntity instanceof AirtightUpgradeHolder holder && holder.isAirtight()) {
            holder.betterContentFixes$setAirtight(false);
            Block.popResource(level, event.getPos(),
                    new ItemStack(ChemistryContent.AIRTIGHT_UPGRADE.get()));
        }
    }
}
