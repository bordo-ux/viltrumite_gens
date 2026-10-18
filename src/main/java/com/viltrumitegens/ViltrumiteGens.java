package com.viltrumitegens;

import com.mojang.logging.LogUtils;
import com.viltrumitegens.capability.GensCapability;
import com.viltrumitegens.config.GensConfig;
import com.viltrumitegens.network.GensNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ViltrumiteGens.MODID)
public class ViltrumiteGens {
    public static final String MODID = "viltrumite_gens";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ViltrumiteGens() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GensConfig.SPEC);
        modBus.addListener(GensCapability::register);
        modBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(GensNetwork::register);
    }
}
