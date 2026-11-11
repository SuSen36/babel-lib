package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.CollectibleDropEvent;
import com.susen36.babel.api.event.LifePointConsumeEvent;
import com.susen36.babel.api.event.OriginiumIngotDropEvent;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.init.BabelCollectible;
import com.susen36.babel.item.EndOfTimesItem;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

// 具体收藏品的功能实现事件
@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class CollectiblesEvents {

    @SubscribeEvent
    public static void onOriginiumIngotDrop(@NotNull OriginiumIngotDropEvent event) {
        if (event.getKiller() instanceof Player player && Collectibles.isUsed(player, BabelCollectible.SUPREME_RING)) {
            event.setMultiplier(event.getMultiplier() * 1.5F);
        }
    }

    @SubscribeEvent
    public static void onCollectibleDrop(@NotNull CollectibleDropEvent event) {
        if (event.getKiller() instanceof Player player && Collectibles.isUsed(player, BabelCollectible.FOUR_LEAF_CLOVER_FOSSIL)) {
            event.setChance(event.getChance() * 2.0F);
        }
    }

    @SubscribeEvent
    public static void onLifePointConsume(@NotNull LifePointConsumeEvent.Post event) {
        if (event.getType() == LifePointConsumeEvent.Type.FAIL
                && event.getEntity() instanceof Player player && !player.level().isClientSide()
                && Collectibles.isUsed(player, BabelCollectible.END_OF_TIMES)) {
            event.setCanceled(true);
            EndOfTimesItem.revive(player);
        }
    }
}