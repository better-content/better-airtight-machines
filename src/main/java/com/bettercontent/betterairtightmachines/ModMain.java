package com.bettercontent.betterairtightmachines;

import com.bettercontent.betterairtightmachines.gametest.AirtightChemistryGameTests;
import com.bettercontent.betterairtightmachines.chemistry.AirtightUpgradeInteraction;
import com.bettercontent.betterairtightmachines.chemistry.ChemistryContent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModMain.MOD_ID)
public final class ModMain {
    public static final String MOD_ID = "better_airtight_machines";

    public ModMain() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ChemistryContent.ITEMS.register(bus);
        bus.addListener(this::registerTests);
        MinecraftForge.EVENT_BUS.register(AirtightUpgradeInteraction.class);
    }
    private void registerTests(net.minecraftforge.event.RegisterGameTestsEvent event) {
        event.register(AirtightChemistryGameTests.class);
    }
}
