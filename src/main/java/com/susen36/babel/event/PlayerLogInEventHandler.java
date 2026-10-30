package com.susen36.babel.event;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.BabelMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class PlayerLogInEventHandler {

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer serverPlayer) {
			ResourceLocation relicBanNoticeId = new ResourceLocation(BabelMod.MODID, "ban_relic_notice");
			if (BabelConfig.banAdvancedCollectibles) {
				Advancement relicBanNoticeAdvancement = serverPlayer.server.getAdvancements().getAdvancement(relicBanNoticeId);
				AdvancementProgress relicBanNoticeProgress = serverPlayer.getAdvancements().getOrStartProgress(relicBanNoticeAdvancement);
				if (!relicBanNoticeProgress.isDone()) {
					for (String remainingCriterion : relicBanNoticeProgress.getRemainingCriteria()) {
						serverPlayer.getAdvancements().award(relicBanNoticeAdvancement, remainingCriterion);
					}
					serverPlayer.displayClientMessage(Component.translatable("gameplay.relic_ban.notice.0"), false);
					serverPlayer.displayClientMessage(Component.translatable("gameplay.relic_ban.notice.1"), false);
				}
			}
		}
	}
}