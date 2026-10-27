package com.susen36.babel.client;

import com.susen36.babel.BabelMod;
import com.susen36.babel.client.gui.CannotScreen;
import com.susen36.babel.client.model.CannotModel;
import com.susen36.babel.client.renderer.CannotRenderer;
import com.susen36.babel.client.renderer.KingPedestalRenderer;
import com.susen36.babel.init.BabelBlockEntities;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.init.BabelEntities;
import com.susen36.babel.init.BabelMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
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
        event.registerBlockEntityRenderer(BabelBlockEntities.KING_PEDESTAL.get(), KingPedestalRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CannotModel.LAYER_LOCATION, CannotModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(BabelMenus.CANNOT.get(), CannotScreen::new);
            ItemProperties.register(BabelCollectible.ROCK_HORN.get(), new ResourceLocation("tooting"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        });
    }
}