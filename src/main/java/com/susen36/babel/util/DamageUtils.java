package com.susen36.babel.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class DamageUtils {
    // Suppresses default constructor, ensuring non-instantiability.
    private DamageUtils() {
    }

    /** 同一击的附带段独立结算，保留主击的无敌帧与差额比较基准。 */
    public static void hurtIndependently(LivingEntity target, DamageSource source, float amount) {
        int originalTime = target.invulnerableTime;
        float originalDamage = target.lastHurt;
        try {
            target.invulnerableTime = 0;
            target.hurt(source, amount);
        } finally {
            target.invulnerableTime = originalTime;
            target.lastHurt = originalDamage;
        }
    }
}