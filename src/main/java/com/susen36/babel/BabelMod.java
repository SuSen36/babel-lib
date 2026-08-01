package com.susen36.babel;

import com.mojang.logging.LogUtils;
import com.susen36.babel.capability.BabelCapability;
import com.susen36.babel.init.BabelAttributes;
import com.susen36.babel.init.BabelMobEffects;
import com.susen36.babel.init.BabelParticles;
import com.susen36.babel.network.BabelNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(BabelMod.MODID)
public class BabelMod {

    public static final String MODID = "babel";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BabelMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(BabelConfig::onLoad);
        modEventBus.addListener(BabelConfig::onReloading);
        BabelAttributes.REGISTER.register(modEventBus);
        BabelMobEffects.REGISTRY.register(modEventBus);
        BabelParticles.REGISTRY.register(modEventBus);
        BabelCapability.register(modEventBus);
        modEventBus.addListener(BabelNetwork::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, BabelConfig.SPEC);
    }
}