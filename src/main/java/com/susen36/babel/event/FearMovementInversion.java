package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.client.player.Input;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 玩家恐惧时反转移动输入，与生物恐惧取反逃逸方向的行为对应。 */
@Mod.EventBusSubscriber(modid = BabelMod.MODID, value = Dist.CLIENT)
public class FearMovementInversion {

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (event.getEntity().hasEffect(BabelMobEffects.FEAR.get())) {
            Input input = event.getInput();
            input.forwardImpulse = -input.forwardImpulse;
            input.leftImpulse = -input.leftImpulse;
            boolean up = input.up;
            input.up = input.down;
            input.down = up;
            boolean left = input.left;
            input.left = input.right;
            input.right = left;
        }
    }
}