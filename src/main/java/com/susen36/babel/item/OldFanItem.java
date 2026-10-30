package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

import java.util.List;

public class OldFanItem extends CollectibleItem.CustomCollectibleItem {

    public OldFanItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
                        .particle(ParticleTypes.CLOUD, 72)
                        .showOverlay(true)
                        .build());
        addConditionalAttributeModifier(Attributes.ATTACK_DAMAGE, 0.1, AttributeModifier.Operation.MULTIPLY_BASE, OldFanItem::professionStacks);
    }

    private static int professionStacks(Player player, Item self) {
        boolean sword = false;
        boolean axe = false;
        boolean hoe = false;
        boolean bow = false;
        boolean crossbow = false;
        boolean shield = false;
        List<NonNullList<ItemStack>> storages = List.of(player.getInventory().items, player.getInventory().offhand);
        for (NonNullList<ItemStack> storage : storages) {
            for (ItemStack stack : storage) {
                if (stack.isEmpty()) {
                    continue;
                }
                sword |= stack.getItem() instanceof SwordItem || stack.is(ItemTags.SWORDS);
                axe |= stack.getItem() instanceof AxeItem || stack.is(ItemTags.AXES);
                hoe |= stack.getItem() instanceof HoeItem || stack.is(ItemTags.HOES);
                bow |= stack.getItem() instanceof BowItem;
                crossbow |= stack.getItem() instanceof CrossbowItem;
                shield |= stack.getItem() instanceof ShieldItem;
            }
        }
        int stacks = 0;
        if (sword) stacks++;
        if (axe) stacks++;
        if (hoe) stacks++;
        if (bow) stacks++;
        if (crossbow) stacks++;
        if (shield) stacks++;
        return stacks;
    }
}
