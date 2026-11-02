package com.susen36.babel.client;

import com.susen36.babel.BabelMod;
import com.susen36.babel.client.render.BabelTranslucentModel;
import com.susen36.babel.effect.InvisibleMobEffect;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BabelMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InvisibleRenderHandler {

    @SubscribeEvent
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        LivingEntityRenderer renderer = event.getRenderer();
        if (InvisibleMobEffect.isBabelInvisible(entity)
                && !(renderer.model instanceof BabelTranslucentModel)) {
            renderer.model = new BabelTranslucentModel((EntityModel<LivingEntity>) renderer.model);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("rawtypes")
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntityRenderer renderer = event.getRenderer();
        if (renderer.model instanceof BabelTranslucentModel wrapper) {
            renderer.model = wrapper.getDelegate();
        }
    }
}
