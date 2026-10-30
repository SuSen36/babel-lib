package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleAttributes;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class FearlessBladeItem extends CollectibleItem.CustomCollectibleItem {

    private static final float TRIGGER_CHANCE = 0.05F;
    private static final int BUFF_DURATION_TICKS = 300;

    private static final Map<Player, Buff> ACTIVE_BUFFS = Collections.synchronizedMap(new WeakHashMap<>());

    private record Buff(int expireTick) {
    }

    public FearlessBladeItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(Attributes.ATTACK_DAMAGE, 0.5, AttributeModifier.Operation.MULTIPLY_BASE,
                (player, self) -> buffStacks(player));
        addConditionalAttributeModifier(Attributes.ARMOR, 0.5, AttributeModifier.Operation.MULTIPLY_BASE,
                (player, self) -> buffStacks(player));
    }

    private static int buffStacks(Player player) {
        Buff buff = ACTIVE_BUFFS.get(player);
        return buff != null && player.tickCount < buff.expireTick() ? 1 : 0;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int layer) {
        return tickCount % 10 == 0;
    }

    @Override
    public void applyEffectTick(Player player, int layer) {
        Buff buff = ACTIVE_BUFFS.get(player);
        if (buff != null && player.tickCount > buff.expireTick()) {
            ACTIVE_BUFFS.remove(player);
            CollectibleAttributes.reconcile(player);
        } else if (buff != null && player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.WAX_ON, player.getX(), player.getY() + 1.0, player.getZ(), 4, 0.5, 0.8, 0.5, 0.1);
        }
    }

    @Override
    public void onAttackHit(LivingHurtEvent event, Player player, LivingEntity target, int layer) {
        if (!player.level().isClientSide() && player.getRandom().nextFloat() < TRIGGER_CHANCE) {
            ACTIVE_BUFFS.put(player, new Buff(player.tickCount + BUFF_DURATION_TICKS));
            CollectibleAttributes.reconcile(player);
        }
    }
}
