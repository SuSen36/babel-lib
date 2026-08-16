package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.util.HealthUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid = BabelMod.MODID)
public class PlayerEatEventHandler {

    @SubscribeEvent
    public static void onUseItemFinish(LivingEntityUseItemEvent.Finish event) {
        Entity entity = event.getEntity();
        ItemStack itemStack = event.getItem();
        String itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString();
        if (!itemId.equals("alexscaves:biome_treat")) {
            if (entity instanceof LivingEntity livingEntity && itemStack.getComponents().has(DataComponents.FOOD) && livingEntity.getHealth() >= livingEntity.getMaxHealth() * 0.6) {
                if (entity instanceof Player player && player.getFoodData().getFoodLevel() < 20 && player.getFoodData().getSaturationLevel() < 20) {
                    double reviveChance = itemStack.getItem().getFoodProperties(itemStack, livingEntity).nutrition() * 0.01;
                    if (player.getFoodData().getFoodLevel() > 16) {
                        reviveChance = reviveChance * 1.5;
                    }
                    if (itemStack.getItem().getFoodProperties(itemStack, livingEntity).saturation() > 0.5) {
                        reviveChance = reviveChance * 1.25;
                    }
                    if (Math.random() < reviveChance) {
                        double maxReviveAmount = 1;
                        String messageText = Component.translatable("gameplay.life_point.revive.2").getString();
                        if (itemStack.getItem().getFoodProperties(itemStack, livingEntity).saturation() > 0.1 && Math.random() < 0.33) {
                            maxReviveAmount = 2;
                            messageText = Component.translatable("gameplay.life_point.revive.1").getString();
                        }
                        if (player.getFoodData().getFoodLevel() > 16 && Math.random() < 0.33) {
                            messageText = Component.translatable("gameplay.life_point.revive.0").getString();
                        }
                        int lifeGain = Mth.nextInt(RandomSource.create(), 1, (int) maxReviveAmount);
                        double maxLives = HealthUtils.getMaxLifePoint(entity);
                        double currentLives = HealthUtils.getLifePoint(entity);
                        if (currentLives < maxLives) {
                            int setval = (int) Math.min(currentLives + lifeGain, maxLives);
                            HealthUtils.setLifePoint(entity, setval);
                            if (!player.level().isClientSide()) {
                                player.displayClientMessage(Component.literal("§a" + messageText.replace("{num}", "" + Math.round(lifeGain))), true);
                            }
                        }
                    }
                }
            }
        }
    }
}
