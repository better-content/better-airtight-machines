package com.bettercontent.betterairtightmachines.chemistry;

import com.bettercontent.betterairtightmachines.ModMain;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ChemistryContent {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ModMain.MOD_ID);
    public static final RegistryObject<Item> AIRTIGHT_UPGRADE = ITEMS.register(
            "airtight_upgrade", () -> new Item(new Item.Properties().stacksTo(16)));

    private ChemistryContent() {
    }
}
