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
import net.minecraftforge.common.Tags;

import java.util.List;

public class OldFanItem extends CollectibleItem.CustomCollectibleItem {

    public OldFanItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.ADVANCED,
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
        boolean trident = false;
        boolean potion = false;
        List<NonNullList<ItemStack>> storages = List.of(player.getInventory().items, player.getInventory().offhand);
        for (NonNullList<ItemStack> storage : storages) {
            for (ItemStack stack : storage) {
                if (stack.isEmpty()) {
                    continue;
                }
                sword |= stack.getItem() instanceof SwordItem || stack.is(ItemTags.SWORDS);
                axe |= stack.getItem() instanceof AxeItem || stack.is(ItemTags.AXES);
                hoe |= stack.getItem() instanceof HoeItem || stack.is(ItemTags.HOES);
                bow |= stack.getItem() instanceof BowItem || stack.is(Tags.Items.TOOLS_BOWS);
                crossbow |= stack.getItem() instanceof CrossbowItem || stack.is(Tags.Items.TOOLS_CROSSBOWS);
                shield |= stack.getItem() instanceof ShieldItem || stack.is(Tags.Items.TOOLS_SHIELDS);
                trident |= stack.getItem() instanceof TridentItem || stack.is(Tags.Items.TOOLS_TRIDENTS);
                potion |= stack.getItem() instanceof PotionItem;
            }
        }
        int stacks = 0;
        if (sword) stacks++;
        if (axe) stacks++;
        if (hoe) stacks++;
        if (bow) stacks++;
        if (crossbow) stacks++;
        if (shield) stacks++;
        if (trident) stacks++;
        if (potion) stacks++;
        return stacks;
    }
}