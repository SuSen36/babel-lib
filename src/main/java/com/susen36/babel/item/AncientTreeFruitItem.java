package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleLike;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.init.BabelCollectible;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class AncientTreeFruitItem extends CollectibleItem.CustomCollectibleItem {

    private static final int GIVE_COUNT = 3;

    public AncientTreeFruitItem() {
        super(new Item.Properties().stacksTo(1), 25, false, CollectibleTiers.RARE,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.AMETHYST_BLOCK_CHIME, 2F, 1F)
                        .particle(ParticleTypes.HAPPY_VILLAGER, 72)
                        .showOverlay(true)
                        .build());
    }

    @Override
    public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
        if (level instanceof ServerLevel) {
            List<RegistryObject<Item>> normalPool = new ArrayList<>();
            for (RegistryObject<Item> holder : BabelCollectible.BABEL_COLLECTIBLES) {
                Item item = holder.get();
                if (item instanceof CollectibleLike collectible && collectible.getTier() == CollectibleTiers.NORMAL) {
                    normalPool.add(holder);
                }
            }
            for (int i = 0; i < GIVE_COUNT && !normalPool.isEmpty(); i++) {
                RegistryObject<Item> picked = normalPool.remove(level.getRandom().nextInt(normalPool.size()));
                ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(picked.get()));
            }
        }
    }
}