package com.susen36.babel.mixin;

import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin {

    @Shadow
    @Final
    private static EntityDataAccessor<Boolean> DATA_IS_IGNITED;

    // 膨胀逻辑先于 AI 步骤执行，HEAD 处冻结 swellDir 可阻断本 tick 引信增长；ignite() 无公开反向方法，只能直写实体数据
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void suppressSwellWhenDebuffed(CallbackInfo ci) {
        Creeper self = (Creeper) (Object) this;
        if (self.hasEffect(BabelMobEffects.SILENCED.get()) || self.hasEffect(BabelMobEffects.FROZEN.get())) {
            self.getEntityData().set(DATA_IS_IGNITED, false);
            self.setSwellDir(0);
        }
    }
}