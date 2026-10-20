package com.bettercontent.betterairtightmachines.chemistry;

import net.minecraft.world.InteractionResult;
import com.bettercontent.betterairtightmachines.diagnostics.PlaytestDiagnostics;
import com.bettercontent.betterairtightmachines.diagnostics.PlaytestLog;
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
                observe(event.getLevel(), "airtight.install", "applied", "upgrade_installed", false, true);
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
            event.setCanceled(true);
        } else if (held.isEmpty() && event.getEntity().isShiftKeyDown() && holder.isAirtight()) {
            if (!event.getLevel().isClientSide()) {
                holder.betterContentFixes$setAirtight(false);
                final ItemStack refund = new ItemStack(ChemistryContent.AIRTIGHT_UPGRADE.get());
                final boolean inserted = event.getEntity().getInventory().add(refund);
                if (!inserted) {
                    Block.popResource(event.getLevel(), event.getPos(), refund);
                }
                observe(event.getLevel(), "airtight.remove", "applied", inserted ? "inventory_refund" : "drop_requested", true, false);
            }
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide()));
            event.setCanceled(true);
        } else if (!event.getLevel().isClientSide() && held.is(ChemistryContent.AIRTIGHT_UPGRADE.get())) {
            observe(event.getLevel(), "airtight.install", "rejected", "already_sealed", true, true);
        } else if (!event.getLevel().isClientSide() && held.isEmpty() && event.getEntity().isShiftKeyDown()) {
            observe(event.getLevel(), "airtight.remove", "rejected", "not_sealed", false, false);
        }
    }

    private static void observe(Level level, String event, String result, String reason, boolean before, boolean after) {
        var log = PlaytestDiagnostics.LOG;
        if (!log.enabled("containment")) return;
        log.debug("containment", event, PlaytestLog.Context.server(level.dimension().location().toString(), level.getGameTime()),
                result, reason, () -> com.bettercontent.betterairtightmachines.diagnostics.FeatureObservation.fields(before, after));
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
            observe(level, "airtight.break_refund", "requested", "drop_requested", true, false);
        }
    }
}
