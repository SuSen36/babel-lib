package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.difficulty.NDifficulty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BabelMod.MODID, value = Dist.CLIENT)
public class ClientPlayerLoggingOut {

    /**客户端玩家登出时清除{@code 同步NDifficultyValue}*/
    @SubscribeEvent
    public static void onLogout(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut e) {
        NDifficulty.clearSynced();
    }

}
