package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.api.event.LostAndFoundEvent;
import com.susen36.babel.item.CannotMarkItem;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class LostAndFoundEvents {

    @SubscribeEvent
    public static void onLostAndFound(LostAndFoundEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() && CannotMarkItem.isUsed(player)) {
            player.giveExperienceLevels(1);
        }
    }
}
