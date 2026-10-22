package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleAttributes;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.CriticalHitEvent;

import java.util.Map;
import java.util.WeakHashMap;

public class HandOfBarrenItem extends CollectibleItem.CustomCollectibleItem {

    private static final TagKey<Item> MINECRAFT_AXES = ItemTags.create(new ResourceLocation("minecraft:axes"));

    private static final int BUFF_DURATION_TICKS = 160;
    private static final int MAX_STACKS = 7;

    private static final Map<Player, Buff> ACTIVE_BUFFS = new WeakHashMap<>();

    private record Buff(int stacks, int expireTick) {
    }

    public HandOfBarrenItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 25, false, CollectibleTiers.ADVANCED,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(Attributes.ATTACK_DAMAGE, 0.2, AttributeModifier.Operation.MULTIPLY_BASE,
                (player, self) -> activeStacks(player));
    }

    private static int activeStacks(Player player) {
        Buff buff = ACTIVE_BUFFS.get(player);
        return buff != null && player.tickCount < buff.expireTick() ? buff.stacks() : 0;
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
        }
    }

    @Override
    public void onCriticalHit(CriticalHitEvent event, Player player, int layer) {
        ItemStack mainHandItem = player.getMainHandItem();
        if (event.isVanillaCritical()
                && (mainHandItem.getItem() instanceof AxeItem || mainHandItem.is(MINECRAFT_AXES))
                && !player.level().isClientSide()) {
            int stacks = Mth.clamp(activeStacks(player) + 1, 0, MAX_STACKS);
            ACTIVE_BUFFS.put(player, new Buff(stacks, player.tickCount + BUFF_DURATION_TICKS));
            CollectibleAttributes.reconcile(player);
        }
    }
}
