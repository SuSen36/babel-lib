package com.susen36.babel;

import com.mojang.logging.LogUtils;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.client.overlay.EPShowOverlay;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.*;
import com.susen36.babel.network.BabelNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BabelMod.MODID)
public class BabelMod {

    public static final String MODID = "babel";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BabelMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(BabelConfig::onLoad);
        modEventBus.addListener(BabelConfig::onReloading);
        modEventBus.addListener(this::commonSetup);
        BabelBlocks.REGISTRY.register(modEventBus);
        BabelBlockEntities.REGISTRY.register(modEventBus);
        BabelItems.REGISTRY.register(modEventBus);
        BabelAttributes.REGISTER.register(modEventBus);
        BabelMobEffects.REGISTRY.register(modEventBus);
        BabelPotions.REGISTRY.register(modEventBus);
        BabelParticles.REGISTRY.register(modEventBus);
        BabelSounds.REGISTRY.register(modEventBus);
        Collectibles.register(modEventBus);
        BabelCapability.register(modEventBus);
        BabelCollectible.register(modEventBus);
        BabelTabs.REGISTRY.register(modEventBus);
        BabelGameRules.init();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, BabelConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(BabelNetwork::register);
    }
}