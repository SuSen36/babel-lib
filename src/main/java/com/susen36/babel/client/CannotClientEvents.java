package com.susen36.babel.client;

import com.susen36.babel.BabelMod;
import com.susen36.babel.client.gui.CannotScreen;
import com.susen36.babel.client.model.CannotModel;
import com.susen36.babel.client.renderer.CannotRenderer;
import com.susen36.babel.init.BabelEntities;
import com.susen36.babel.init.BabelMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = BabelMod.MODID)
public class CannotClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BabelEntities.CANNOT.get(), CannotRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CannotModel.LAYER_LOCATION, CannotModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(BabelMenus.CANNOT.get(), CannotScreen::new));
    }
}
