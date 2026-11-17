package com.susen36.babel.event;

import com.susen36.babel.BabelMod;
import com.susen36.babel.difficulty.NDifficulty;
import com.susen36.babel.init.BabelEntityTypeTags;
import com.susen36.babel.init.BabelGameRules;
import com.susen36.babel.network.BabelNetwork;
import com.susen36.babel.network.record.NDifficultySyncPacket;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * 难度（探索等级）通用实体应用逻辑。
 * <p>
 * 仅复制 N18 相关通用部分：实体生成时按 {@link NDifficulty#multiplier} 提升
 * 生命、攻击、盔甲值与盔甲韧性，并保持当前血量百分比。不包含任何海嗣专属依赖。
 */
@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class NDifficultyEvent {

    /** 强制应用标签：加入该标签的实体即使不属于默认敌对目标也会应用难度倍率。 */
    public static final TagKey<EntityType<?>> APPLIES_DIFFICULTY = TagKey.create(
            Registries.ENTITY_TYPE, new ResourceLocation(BabelMod.MODID, "applies_difficulty"));

    /** 绕过标签：加入该标签的实体即使属于默认敌对目标也不会应用难度倍率。 */
    public static final TagKey<EntityType<?>> BYPASSES_DIFFICULTY = TagKey.create(
            Registries.ENTITY_TYPE, new ResourceLocation(BabelMod.MODID, "bypasses_difficulty"));

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        Entity entity = event.getEntity();
        Level world = entity.level();

        if (NDifficulty.difficultyLevel(world).value() >= 12
                && world.getLevelData().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                && (entity.getType().is(BabelEntityTypeTags.ELITE) || entity.getType().is(BabelEntityTypeTags.FORGE_BOSSES))) {

            LivingEntity target = entity instanceof Mob mob ? mob.getTarget() : null;
            if (target != null && target.isAlive() && entity.getVehicle() instanceof Boat boat) {
                boat.hurt(entity.damageSources().generic(), 20);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        LivingEntity living = event.getEntity();
        EntityType<?> type = living.getType();

        if (!type.is(BYPASSES_DIFFICULTY) && (type.is(APPLIES_DIFFICULTY) || living instanceof Monster)) {
            int level = NDifficulty.difficultyLevel(living.level()).value();
            if (level > 0) {
                event.setDroppedExperience(Mth.ceil(event.getDroppedExperience() * (1.0D + 0.08D * level)));
            }
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        Level world = event.getLevel();
        EntityType<?> type = entity.getType();

        if (entity instanceof LivingEntity living && !type.is(BYPASSES_DIFFICULTY) && (type.is(APPLIES_DIFFICULTY) || entity instanceof Monster)) {

            double n = NDifficulty.multiplier(world);
            CompoundTag data = living.getPersistentData();

            // 防止区块重新加载时重复乘算导致数值无限膨胀
            if (n > 1.0 && !data.getBoolean("NDifficultyApplied")) {
                double percentage = living.getHealth() / living.getMaxHealth();

                AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
                if (maxHealth != null) {
                    maxHealth.setBaseValue(maxHealth.getBaseValue() * n);
                }
                AttributeInstance armor = living.getAttribute(Attributes.ARMOR);
                if (armor != null) {
                    armor.setBaseValue(armor.getBaseValue() * n);
                }
                AttributeInstance armorToughness = living.getAttribute(Attributes.ARMOR_TOUGHNESS);
                if (armorToughness != null) {
                    armorToughness.setBaseValue(armorToughness.getBaseValue() * n);
                }
                AttributeInstance attackDamage = living.getAttribute(Attributes.ATTACK_DAMAGE);
                if (attackDamage != null) {
                    attackDamage.setBaseValue(attackDamage.getBaseValue() * n);
                }

                AttributeInstance newMaxHealth = living.getAttribute(Attributes.MAX_HEALTH);
                if (newMaxHealth != null) {
                    living.setHealth((float) (newMaxHealth.getValue() * percentage));
                }

                data.putBoolean("NDifficultyApplied", true);
            }
        }
    }

    private static int lastSent = Integer.MIN_VALUE;

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            push(sp.server, PacketDistributor.PLAYER.with(() -> sp));
        }
    }

    /** 每20tick更新一次 */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null && server.getTickCount() % 20 == 0) {
                int value = current(server);
                if (value != lastSent) {
                    lastSent = value;
                    BabelNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new NDifficultySyncPacket(value));
                }
            }
        }
    }

    /**服务器关闭时设置为初始值*/
    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        lastSent = Integer.MIN_VALUE;
    }

    private static int current(MinecraftServer server) {
        return server.overworld().getGameRules().getInt(BabelGameRules.DIFFICULTY_LEVEL);
    }

    private static void push(MinecraftServer server, PacketDistributor.PacketTarget target) {
        lastSent = current(server);
        BabelNetwork.CHANNEL.send(target, new NDifficultySyncPacket(lastSent));
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = BabelMod.MODID)
    public static class ClientEvents {
        /**客户端玩家登出时清除{@code 同步NDifficultyValue}*/
        @SubscribeEvent
        public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            NDifficulty.clearSynced();
        }
    }
}