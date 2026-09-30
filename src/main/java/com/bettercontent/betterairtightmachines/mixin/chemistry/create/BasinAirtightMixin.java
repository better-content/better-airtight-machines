package com.bettercontent.betterairtightmachines.mixin.chemistry.create;

import com.bettercontent.betterairtightmachines.chemistry.AirtightUpgradeHolder;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = BasinBlockEntity.class, remap = false)
abstract class BasinAirtightMixin implements AirtightUpgradeHolder {
    @Unique private boolean betterContentFixes$airtight;

    @Override
    public boolean isAirtight() {
        return betterContentFixes$airtight;
    }

    @Override
    public void betterContentFixes$setAirtight(final boolean airtight) {
        if (betterContentFixes$airtight == airtight) return;
        betterContentFixes$airtight = airtight;
        final BasinBlockEntity basin = (BasinBlockEntity) (Object) this;
        basin.notifyChangeOfContents();
        basin.notifyUpdate();
    }
}
