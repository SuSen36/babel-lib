package com.susen36.babel.elemental;

import com.susen36.babel.effect.LessArmorMobEffect;
import com.susen36.babel.elemental.base.AbstractEPCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

// 侵蚀损伤的爆条与减益实现。
public class CorrosionInjury extends AbstractEPCapability {

    private static final int NON_PLAYER_REVIVE_TICKS = 160;
    private static final int LEGACY_REVIVE_TICKS = 200;

    public CorrosionInjury(LivingEntity livingEntity) {
        super(EPType.CORROSION, livingEntity);
        if (!(livingEntity instanceof Player)) {
            setMaxReviveTick(NON_PLAYER_REVIVE_TICKS);
        }
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        if (!(livingEntity instanceof Player)) {
            int savedLeft = tag.getInt("corrosion.leftReviveTick");
            // 旧首次爆条按 200 tick 启动，随后才把上限写成 160。保留到期点，不推断免疫来源。
            if (maxReviveTick == NON_PLAYER_REVIVE_TICKS && savedLeft > NON_PLAYER_REVIVE_TICKS && savedLeft <= LEGACY_REVIVE_TICKS) {
                maxReviveTick = LEGACY_REVIVE_TICKS;
                reviveTick = savedLeft;
            } else if (!underBurst() && maxReviveTick == LEGACY_REVIVE_TICKS) {
                maxReviveTick = NON_PLAYER_REVIVE_TICKS;
            }
        }
    }

    @Override
    public void doPlayerBurst() {
        LessArmorMobEffect.apply(livingEntity, 1, 1800);
        livingEntity.clearFire();
        applyBurstDamage(9.0F);
    }

    @Override
    public void doNonPlayerBurst() {
        LessArmorMobEffect.apply(livingEntity, 2, 1800);
        applyBurstDamage(21.0F);
    }

    @Override
    public void doPlayerBurstTick() {
    }

    @Override
    public void doNonPlayerBurstTick() {
        if (reviveTick == 0 && maxReviveTick == LEGACY_REVIVE_TICKS) {
            setMaxReviveTick(NON_PLAYER_REVIVE_TICKS);
        }
    }
}