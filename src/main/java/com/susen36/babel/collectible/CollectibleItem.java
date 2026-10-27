package com.susen36.babel.collectible;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.api.event.CollectibleEvent;
import com.susen36.babel.api.event.LifePointConsumeEvent;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class CollectibleItem extends Item implements CollectibleLike {
    private final CollectibleEffect effect;
    private final int useTicks;
    private final boolean canAlwaysUse;
    private final CollectibleTiers tier;
    private final Levels levels;
    private final CollectibleActivation activation;

    public CollectibleItem(CollectibleEffect effect) {
        this(effect, 25, false, CollectibleTiers.NORMAL, new Levels(0, 1, 0), CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, boolean canAlwaysUse) {
        this(effect, 25, canAlwaysUse, CollectibleTiers.NORMAL, new Levels(0, 1, 0), CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, int useTicks) {
        this(effect, useTicks, false, CollectibleTiers.NORMAL, new Levels(0, 1, 0), CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, int useTicks, boolean canAlwaysUse) {
        this(effect, useTicks, canAlwaysUse, CollectibleTiers.NORMAL, new Levels(0, 1, 0), CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, boolean canAlwaysUse, Levels levels) {
        this(effect, 25, canAlwaysUse, CollectibleTiers.NORMAL, levels, CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, int useTicks, Levels levels) {
        this(effect, useTicks, false, CollectibleTiers.NORMAL, levels, CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, int useTicks, boolean canAlwaysUse, Levels levels) {
        this(effect, useTicks, canAlwaysUse, CollectibleTiers.NORMAL, levels, CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, int useTicks, boolean canAlwaysUse, CollectibleTiers tier, Levels levels, CollectibleActivation activation) {
        super(collectibleProperties(tier));
        this.effect = effect;
        this.useTicks = useTicks;
        this.canAlwaysUse = canAlwaysUse;
        this.tier = tier;
        this.levels = levels;
        this.activation = activation;
    }

    /**
     * 收藏品统一物品属性：堆叠 1，稀有度按等级映射。
     */
    private static Item.Properties collectibleProperties(CollectibleTiers tier) {
        return new Item.Properties().stacksTo(1).rarity(rarityForTier(tier));
    }

    /**
     * 等级 → 基础稀有度；高级为 EPIC，稀有为 RARE，其余为 COMMON（诅咒另染红）。
     */
    private static Rarity rarityForTier(CollectibleTiers tier) {
        if (tier == CollectibleTiers.RARE) {
            return Rarity.RARE;
        }
        if (tier == CollectibleTiers.ADVANCED) {
            return Rarity.EPIC;
        }
        return Rarity.COMMON;
    }

    @Override
    public CollectibleTiers getTier() {
        return tier;
    }

    /**
     * 诅咒级收藏品物品名染红（同原版负面附魔），其余沿用等级稀有度颜色。
     */
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        if (tier == CollectibleTiers.CURSED) {
            return Component.translatable(this.getDescriptionId(stack)).withStyle(ChatFormatting.RED);
        }
        return super.getName(stack);
    }

    /**
     * 所有收藏品通用逐行描述：键名按注册名生成，直到 lang 中不存在下一个 description_N 为止。
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String descKey = this.getDescriptionId() + ".description";
        for (int i = 0; ; i++) {
            String key = descKey + "_" + i;
            Component line = i == 0
                    ? Component.translatable(key).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)
                    : Component.translatable(key).withStyle(ChatFormatting.GRAY);
            if (line.getString().equals(key)) {
                break;
            }
            tooltip.add(line);
        }
    }

    @Override
    public int minLevel() {
        return levels.minLevel();
    }

    @Override
    public int maxLevel() {
        return levels.maxLevel();
    }

    @Override
    public int defaultLevel() {
        return levels.defaultLevel();
    }

    /**
     * 高级收藏品被游戏规则禁用时，视为不可使用。
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isBanned() {
        return tier == CollectibleTiers.ADVANCED && BabelConfig.banAdvancedCollectibles;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand usedHand
    ) {
        if (!player.isShiftKeyDown()) {
            player.startUsingItem(usedHand);
            return InteractionResultHolder.consume(player.getItemInHand(usedHand));
        } else {
            return onSneakUse(level, player, usedHand);
        }
    }

    /**
     * 蹲下使用时的特殊 hook，子类可重写；默认不执行任何收藏品流程。
     */
    @NotNull
    protected InteractionResultHolder<ItemStack> onSneakUse(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand
    ) {
        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    @Override
    public @NotNull ItemStack finishUsingItem(
            @NotNull ItemStack stack,
            @NotNull Level level,
            @NotNull LivingEntity livingEntity
    ) {
        if (livingEntity instanceof Player player && level instanceof ServerLevel serverLevel) {
            var cd = Collectibles.getCooldownData(player);
            if (canAlwaysUse || (!isBanned() && cd.isReady(this, serverLevel.getServer()))) {
                activate(level, player, stack, this);
                return consumeSelf(stack);
            } else {
                playFailure(level, player, stack);
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return this.useTicks;
    }

    private void playSuccess(@NotNull Level level, @NotNull Player player, @NotNull ItemStack stack) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    activation.particle(),
                    x, y + activation.particleYOffset(), z,
                    activation.paticleCount(),
                    1, 1, 1,
                    activation.particleSpeed()
            );
        }
        level.playSound(null, BlockPos.containing(x, y, z), activation.soundEvent(), SoundSource.NEUTRAL,
                activation.volume(), activation.pitch());
        if (player instanceof ServerPlayer serverPlayer) BabelNetwork.syncCollectibleUse(serverPlayer, stack);
    }

    private void playFailure(@NotNull Level level, @NotNull Player player, @NotNull ItemStack stack) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        var sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("minecraft", "block.chest.locked"));
        if (sound == null)
            throw new RuntimeException("cannot found sound event: block.chest.locked");
        level.playSound(
                null,
                BlockPos.containing(x, y, z),
                sound,
                SoundSource.NEUTRAL,
                activation.volume(),
                activation.pitch()
        );
    }

    private void activate(@NotNull Level level, @NotNull Player player, @NotNull ItemStack stack, @NotNull Item item) {
        var data = Collectibles.getData(player);
        data.markUsed(item);
        CollectibleEvent.onUsed(player, item);
        playSuccess(level, player, stack);
        int layer = Collectibles.getLayerData(player).getLayer(item);
        effect.onEffectStarted(stack, level, player, layer);
        if (level instanceof ServerLevel) {
            BabelNetwork.syncCollectibles(player);
        }
    }

    private ItemStack consumeSelf(@NotNull ItemStack stack) {
        var stackCopy = stack.copy();
        stackCopy.shrink(1);
        return stackCopy;
    }

    /**
     * 诅咒级收藏品：获得（进入背包）时自动激活一次。
     */
    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (activation.autoUseOnGain() && entity instanceof Player player && entity.tickCount % 10 == 0) {
            // 每0.5秒尝试一次自动使用
            var data = Collectibles.getData(player);
            Item item = stack.getItem();
            if (!data.isUsed(item) && !isBanned()) {
                activate(level, player, stack, item);
                consumeSelf(stack);
            }
        }
    }

    @FunctionalInterface
    public interface CollectibleEffect {
        void onEffectStarted(
                @NotNull ItemStack stack,
                @NotNull Level level,
                @NotNull Player player,
                int layer
        );
    }

    public abstract static class CustomCollectibleItem extends Item implements CollectibleLike {
        private final int useTicks;
        private final boolean canAlwaysUse;
        private final CollectibleTiers tier;
        private final Levels levels;
        private final CollectibleActivation activation;
        private final List<AttributeBonus> attributeBonuses = new ArrayList<>();

        public CustomCollectibleItem(Properties properties, int useTicks, boolean canAlwaysUse, CollectibleTiers tier, Levels levels, CollectibleActivation activation) {
            super(properties);
            this.useTicks = useTicks;
            this.canAlwaysUse = canAlwaysUse;
            this.tier = tier;
            this.levels = levels;
            this.activation = activation;
        }

        /**
         * 层数来源：每 10 tick 对账时按返回层数乘算属性值；未激活的收藏品统一按 0 层处理，此回调不会被调用。
         */
        @FunctionalInterface
        public interface AttributeStacks {
            int getStacks(Player player, Item self);
        }

        public record AttributeBonus(Attribute attribute,
                                     double perStack, AttributeModifier.Operation operation,
                                     AttributeStacks stacks) {
        }

        public static final AttributeStacks ONE_STACK = (player, self) -> 1;

        public static final AttributeStacks LAYER_STACKS = (player, self) ->
                Collectibles.getLayerData(player).getLayer(self);

        protected final CustomCollectibleItem addAttributeModifier(Attribute attribute, double perStack, AttributeModifier.Operation operation) {
            this.attributeBonuses.add(new AttributeBonus(attribute, perStack, operation, ONE_STACK));
            return this;
        }

        protected final CustomCollectibleItem addLayerAttributeModifier(Attribute attribute, double perStack, AttributeModifier.Operation operation) {
            this.attributeBonuses.add(new AttributeBonus(attribute, perStack, operation, LAYER_STACKS));
            return this;
        }

        protected final CustomCollectibleItem addConditionalAttributeModifier(Attribute attribute, double perStack, AttributeModifier.Operation operation, AttributeStacks stacks) {
            this.attributeBonuses.add(new AttributeBonus(attribute, perStack, operation, stacks));
            return this;
        }

        public final List<AttributeBonus> getAttributeBonuses() {
            return this.attributeBonuses;
        }

        protected static boolean matchesConfigured(Iterable<? extends String> rules, String name) {
            boolean matched = false;
            for (String rule : rules) {
                if (rule != null && name != null && ("*".equals(rule) || name.equals(rule))) {
                    matched = true;
                } else if (rule != null && name != null) {
                    int wildcardIndex = rule.indexOf('*');
                    if (wildcardIndex > 0 && name.startsWith(rule.substring(0, wildcardIndex))) {
                        matched = true;
                    }
                }
                if (matched) {
                    break;
                }
            }
            return matched;
        }

        protected static int countUsedRelics(Player player, TagKey<Item> tag) {
            int count = 0;
            for (Item relic : Collectibles.getData(player).getUsed()) {
                if (relic.builtInRegistryHolder().is(tag)) {
                    count++;
                }
            }
            return count;
        }

        /**
         * 分发器每 tick 对所有已激活收藏品调用一次；返回 true 的 tick 才继续执行 applyEffectTick，间隔节流在此方法内判定。
         */
        public boolean shouldApplyEffectTickThisTick(int tickCount, int layer) {
            return false;
        }

        public void applyEffectTick(Player player, int layer) {
        }

        /** 以玩家为中心的立方体半边长，32×32×32 即 16.0。 */
        public static final double MONSTER_RANGE = 16.0D;

        @FunctionalInterface
        public interface RangeEffect<T extends LivingEntity> {
            void apply(T entity, Player player, int layer);
        }

        public record RangeBinding<T extends LivingEntity>(Class<T> entityType, double range, RangeEffect<T> effect) {
        }

        private final List<RangeBinding<?>> rangeEffects = new ArrayList<>();

        /** 注册范围效果：每 20 tick 对玩家周围 range×2 立方体内的每只指定类型实体回调一次。 */
        protected final <T extends LivingEntity> void addRangeEffect(Class<T> entityType, double range, RangeEffect<T> effect) {
            this.rangeEffects.add(new RangeBinding<>(entityType, range, effect));
        }

        protected final <T extends LivingEntity> void addRangeEffect(Class<T> entityType, RangeEffect<T> effect) {
            this.addRangeEffect(entityType, MONSTER_RANGE, effect);
        }

        public final List<RangeBinding<?>> getRangeEffects() {
            return this.rangeEffects;
        }

        /** LivingHurtEvent 阶段回调；事件可能已取消、直接伤害来源可能是持有者本人，需要时由实现自行判定。 */
        public void onAttackHit(LivingHurtEvent event, Player player, LivingEntity target, int layer) {
        }

        /** 持有者近战造成伤害（LivingDamageEvent 阶段）。 */
        public void onDealMeleeDamage(LivingDamageEvent event, Player player, LivingEntity target, int layer) {
        }

        /**
         * 持有者作为伤害来源对生物造成伤害（LivingDamageEvent 阶段），近战、投射物与法术来源都会触发。
         * 分发器只转发事件，目标类型、伤害类型、范围等判定由覆写方自行完成；此时护甲与魔抗结算已结束，setAmount 改的是最终伤害。
         */
        public void onDealDamage(LivingDamageEvent event, Player player, LivingEntity target, int layer) {
        }

        /** 持有者承受伤害（LivingDamageEvent 阶段），可通过 event 调整最终伤害。 */
        public void onMobHurt(LivingDamageEvent event, Player player, int layer) {
        }

        /** 持有者失去生命值或护盾（LifePointConsumeEvent 阶段）；type 为扣除的资源类型，amount 为扣除量，事件只读。 */
        public void onLifePointLoss(LifePointConsumeEvent event, Player player, int layer) {
        }

        /** 持有者接受治疗或生命回复（LivingHealEvent 阶段）；药水治疗与饱食自然回复都走 LivingEntity.heal，setAmount 可调整治疗量。 */
        public void onOwnerHeal(LivingHealEvent event, Player player, int layer) {
        }

        public void onPlayerKill(LivingDeathEvent event, Player player, Entity victim, int layer) {
        }

        public void onCriticalHit(CriticalHitEvent event, Player player, int layer) {
        }

        public void onUseItemEnd(LivingEntityUseItemEvent event, Player player, int layer) {
        }

        public void onExperienceDrop(LivingExperienceDropEvent event, Player player, int layer) {
        }

        /**
         * 诅咒级收藏品物品名染红（同原版负面附魔），其余沿用等级稀有度颜色。
         */
        @Override
        final public @NotNull Component getName(@NotNull ItemStack stack) {
            if (tier == CollectibleTiers.CURSED) {
                return Component.translatable(this.getDescriptionId(stack)).withStyle(ChatFormatting.RED);
            }
            return super.getName(stack);
        }

        /**
         * 所有收藏品通用逐行描述：键名按注册名生成，直到 lang 中不存在下一个 description_N 为止。
         */
        @Override
        final public void appendHoverText(@NotNull ItemStack stack, @NotNull Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
            super.appendHoverText(stack, level, tooltip, flag);
            String descKey = this.getDescriptionId() + ".description";
            for (int i = 0; ; i++) {
                String key = descKey + "_" + i;
                Component line = i == 0
                        ? Component.translatable(key).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)
                        : Component.translatable(key).withStyle(ChatFormatting.GRAY);
                if (line.getString().equals(key)) {
                    break;
                }
                tooltip.add(line);
            }
        }

        @Override
        final public int minLevel() {
            return levels.minLevel();
        }

        @Override
        final public int maxLevel() {
            return levels.maxLevel();
        }

        @Override
        final public int defaultLevel() {
            return levels.defaultLevel();
        }

        @Override
        final public CollectibleTiers getTier() {
            return tier;
        }

        /**
         * 诅咒级收藏品：获得（进入背包）时自动激活一次。
         */
        @Override
        final public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
            super.inventoryTick(stack, level, entity, slot, selected);
            if (activation.autoUseOnGain() && entity instanceof Player player && entity.tickCount % 10 == 0) {
                // 每0.5秒尝试一次自动使用
                var data = Collectibles.getData(player);
                Item item = stack.getItem();
                if (!data.isUsed(item) && !isBanned()) {
                    activate(level, player, stack, item);
                    consumeSelf(stack, player);
                }
            }
        }

        private void playSuccess(@NotNull Level level, @NotNull Player player, @NotNull ItemStack stack) {
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(activation.particle(), x, y + activation.particleYOffset(), z,
                        activation.paticleCount(), 1, 1, 1, activation.particleSpeed());
            }
            level.playSound(null, BlockPos.containing(x, y, z), activation.soundEvent(), SoundSource.NEUTRAL,
                    activation.volume(), activation.pitch());
            if (player instanceof ServerPlayer serverPlayer) {
                BabelNetwork.syncCollectibleUse(serverPlayer, stack);
            }
        }

        private void playFailure(@NotNull Level level, @NotNull Player player, @NotNull ItemStack stack) {
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            var sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("minecraft", "block.chest.locked"));
            if (sound == null)
                throw new RuntimeException("cannot found sound event: block.chest.locked");
            level.playSound(
                    null,
                    BlockPos.containing(x, y, z),
                    sound,
                    SoundSource.NEUTRAL,
                    activation.volume(),
                    activation.pitch()
            );
        }

        private void activate(@NotNull Level level, @NotNull Player player, @NotNull ItemStack stack, @NotNull Item item) {
            var data = Collectibles.getData(player);
            data.markUsed(item);
            CollectibleEvent.onUsed(player, item);
            playSuccess(level, player, stack);
            int layer = Collectibles.getLayerData(player).getLayer(item);
            onEffectStarted(stack, level, player, layer);
            if (level instanceof ServerLevel) {
                BabelNetwork.syncCollectibles(player);
            }
        }

        /**
         * 高级收藏品被游戏规则禁用时，视为不可使用。
         */
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean isBanned() {
            return tier == CollectibleTiers.ADVANCED && BabelConfig.banAdvancedCollectibles;
        }

        private ItemStack consumeSelf(@NotNull ItemStack stack, Player player) {
            ItemStack stackCopy = stack.copy();
            if (!player.getAbilities().instabuild) {
                stackCopy.shrink(1);
            }
            return stackCopy;
        }

        @Override
        final public int getUseDuration(@NotNull ItemStack stack) {
            return this.useTicks;
        }

        @Override
        final public @NotNull InteractionResultHolder<ItemStack> use(
                @NotNull Level level,
                @NotNull Player player,
                @NotNull InteractionHand usedHand
        ) {
            if (!player.isShiftKeyDown()) {
                player.startUsingItem(usedHand);
                return InteractionResultHolder.consume(player.getItemInHand(usedHand));
            } else {
                return onSneakUse(level, player, usedHand);
            }
        }

        /**
         * 蹲下使用时的特殊操作，供子类重写,不执行任何收藏品流程。
         */
        @NotNull
        protected InteractionResultHolder<ItemStack> onSneakUse(
                @NotNull Level level,
                @NotNull Player player,
                @NotNull InteractionHand hand
        ) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        @Override
        final public @NotNull ItemStack finishUsingItem(
                @NotNull ItemStack stack,
                @NotNull Level level,
                @NotNull LivingEntity livingEntity
        ) {
            if (livingEntity instanceof Player player && level instanceof ServerLevel serverLevel) {
                var cd = Collectibles.getCooldownData(player);
                if (canAlwaysUse || (!isBanned() && cd.isReady(this, serverLevel.getServer()))) {
                    activate(level, player, stack, this);
                    return consumeSelf(stack, player);
                } else {
                    playFailure(level, player, stack);
                }
            }
            return stack;
        }

        /**
         * 收藏品激活时的效果，供子类重写，默认无效果。
         */
        public void onEffectStarted(
                @NotNull ItemStack stack,
                @NotNull Level level,
                @NotNull Player player,
                int layer
        ) {
        }
    }

    public record Levels(int minLevel, int maxLevel, int defaultLevel) {
    }
}