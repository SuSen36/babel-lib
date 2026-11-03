package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleAttributes;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.Map;
import java.util.WeakHashMap;

public class HandOfPulverizationItem extends CollectibleItem.CustomCollectibleItem {

    private static final int BUFF_DURATION_TICKS = 160;
    private static final int MAX_STACKS = 10;

    private static final Map<Player, Stacks> ACTIVE_STACKS = new WeakHashMap<>();

    private record Stacks(int count, int expireTick) {
    }

    public HandOfPulverizationItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.FIREWORK, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onAttackHit(LivingHurtEvent event, Player player, LivingEntity target, int layer) {
        if (!player.level().isClientSide()
                && event.getSource().getEntity() == player
                && event.getSource().is(DamageTypes.FIREWORKS)
                && event.getSource().getDirectEntity() instanceof FireworkRocketEntity rocket
                && rocket.isShotAtAngle()) {
            int stacks = currentStacks(player);
            if (stacks > 0) {
                event.setAmount(event.getAmount() * (1.0F + stacks * 0.15F));
            }
            stacks = Mth.clamp(stacks + 1, 0, MAX_STACKS);
            ACTIVE_STACKS.put(player, new Stacks(stacks, player.tickCount + BUFF_DURATION_TICKS));
        }
    }

    @Override
    public void applyEffectTick(Player player, int layer) {
        if (!player.level().isClientSide() && activeStacksExpired(player)) {
            ACTIVE_STACKS.remove(player);
            CollectibleAttributes.reconcile(player);
        }
    }

    private static boolean activeStacksExpired(Player player) {
        Stacks stacks = ACTIVE_STACKS.get(player);
        return stacks != null && player.tickCount >= stacks.expireTick();
    }

    private static int currentStacks(Player player) {
        Stacks stacks = ACTIVE_STACKS.get(player);
        return stacks != null && player.tickCount < stacks.expireTick() ? stacks.count() : 0;
    }
}