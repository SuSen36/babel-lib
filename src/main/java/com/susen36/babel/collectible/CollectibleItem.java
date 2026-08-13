package com.susen36.babel.collectible;

import com.mojang.logging.LogUtils;
import com.susen36.babel.BabelConfig;
import com.susen36.babel.api.event.CollectibleEvent;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CollectibleItem extends Item implements CollectibleLike {
    private final CollectibleEffect effect;
    private final boolean consumeSelf;
    private final int useTicks;
    private final boolean canAlwaysUse;
    private final CollectibleTiers tier;
    private final Levels levels;
    private final CollectibleActivation activation;

    private Level clientLevel;

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf) {
        this(effect, consumeSelf, 25, false, CollectibleTiers.NORMAL, new Levels(0, 1, 0), CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, boolean canAlwaysUse) {
        this(effect, consumeSelf, 25, canAlwaysUse, CollectibleTiers.NORMAL, new Levels(0, 1, 0), CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, int useTicks) {
        this(effect, consumeSelf, useTicks, false, CollectibleTiers.NORMAL, new Levels(0, 1, 0), CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, int useTicks, boolean canAlwaysUse) {
        this(effect, consumeSelf, useTicks, canAlwaysUse, CollectibleTiers.NORMAL, new Levels(0, 1, 0), CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, boolean canAlwaysUse, Levels levels) {
        this(effect, consumeSelf, 25, canAlwaysUse, CollectibleTiers.NORMAL, levels, CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, int useTicks, Levels levels) {
        this(effect, consumeSelf, useTicks, false, CollectibleTiers.NORMAL, levels, CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, int useTicks, boolean canAlwaysUse, Levels levels) {
        this(effect, consumeSelf, useTicks, canAlwaysUse, CollectibleTiers.NORMAL, levels, CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, int useTicks, boolean canAlwaysUse, CollectibleTiers tier, Levels levels, CollectibleActivation activation) {
        super(collectibleProperties(tier));
        this.effect = effect;
        this.consumeSelf = consumeSelf;
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
     * 所有收藏品通用两行描述：键名按注册名生成，颜色由本地化值内嵌的 § 代码控制。
     */
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        String descKey = this.getDescriptionId() + ".description";
        String key0 = descKey + "_0";
        String key1 = descKey + "_1";
        Component line0 = Component.translatable(key0).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC);
        if (!line0.getString().equals(key0)) {
            tooltip.add(line0);
        }
        Component line1 = Component.translatable(key1).withStyle(ChatFormatting.GRAY);
        if (!line1.getString().equals(key1)) {
            tooltip.add(line1);
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
        if (level instanceof ClientLevel) this.clientLevel = level;
        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(player.getItemInHand(usedHand));
    }

    @Override
    public @NotNull ItemStack finishUsingItem(
            @NotNull ItemStack stack,
            @NotNull Level level,
            @NotNull LivingEntity livingEntity
    ) {
        if (livingEntity instanceof Player player && level instanceof ServerLevel serverLevel) {
            var data = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE.get());
            var cd = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE_COOLDOWN.get());
            if (canAlwaysUse || (!data.isUsed(this) && !isBanned() && cd.isReady(this, serverLevel.getServer().getTickCount()))) {
                activate(level, player, stack, this);
                if (consumeSelf) {
                    return consumeSelf(stack);
                }
            } else {
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();
                SoundEvent failSound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.withDefaultNamespace("block.chest.locked"));
                if (failSound != null && clientLevel != null) {
                    clientLevel.playLocalSound(
                            x, y, z,
                            failSound,
                            SoundSource.PLAYERS,
                            1.0f,
                            1.0f,
                            false
                    );
                } else
                    LogUtils.getLogger().error("failSound or clientLevel in {} is null: failSound={}, clientLevel={}", this, failSound, clientLevel);
            }
        }
        return stack;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return this.useTicks;
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
        if (level.isClientSide()) {
            Minecraft.getInstance().gameRenderer.displayItemActivation(stack);
        }
    }

    private void activate(@NotNull Level level, @NotNull Player player, @NotNull ItemStack stack, @NotNull Item item) {
        var data = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE.get());
        data.markUsed(item);
        CollectibleEvent.onUsed(player, item);
        playSuccess(level, player, stack);
        effect.onUse(stack, level, player, this);
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
        if (!activation.autoUseOnGain() || !(entity instanceof Player player)) {
            return;
        }
        // 每0.5秒尝试一次自动使用
        if (entity.tickCount % 10 == 0) {
            var data = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE.get());
            Item item = stack.getItem();
            if (!data.isUsed(item) && !isBanned()) {
                activate(level, player, stack, item);
                if (consumeSelf) {
                    consumeSelf(stack);
                }
            }
        }
    }

    @FunctionalInterface
    public interface CollectibleEffect {
        void onUse(
                @NotNull ItemStack stack,
                @NotNull Level level,
                @NotNull Player player,
                @NotNull CollectibleItem self
        );
    }

    public abstract static class CustomCollectibleItem extends Item implements CollectibleLike {
        private final boolean consumeSelf;
        private final int useTicks;
        private final boolean canAlwaysUse;
        private final CollectibleTiers tier;
        private final Levels levels;
        private final CollectibleActivation activation;

        private Level clientLevel;

        public CustomCollectibleItem(Properties properties, boolean consumeSelf, int useTicks, boolean canAlwaysUse, CollectibleTiers tier, Levels levels, CollectibleActivation activation) {
            super(properties);
            this.consumeSelf = consumeSelf;
            this.useTicks = useTicks;
            this.canAlwaysUse = canAlwaysUse;
            this.tier = tier;
            this.levels = levels;
            this.activation = activation;
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
         * 所有收藏品通用两行描述：键名按注册名生成，颜色由本地化值内嵌的 § 代码控制。
         */
        @Override
        final public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
            super.appendHoverText(stack, context, tooltip, flag);
            String descKey = this.getDescriptionId() + ".description";
            String key0 = descKey + "_0";
            String key1 = descKey + "_1";
            Component line0 = Component.translatable(key0).withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC);
            if (!line0.getString().equals(key0)) {
                tooltip.add(line0);
            }
            Component line1 = Component.translatable(key1).withStyle(ChatFormatting.GRAY);
            if (!line1.getString().equals(key1)) {
                tooltip.add(line1);
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

        /**
         * 诅咒级收藏品：获得（进入背包）时自动激活一次。
         */
        @Override
        final public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
            super.inventoryTick(stack, level, entity, slot, selected);
            if (!activation.autoUseOnGain() || !(entity instanceof Player player)) {
                return;
            }
            // 每0.5秒尝试一次自动使用
            if (entity.tickCount % 10 == 0) {
                var data = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE.get());
                Item item = stack.getItem();
                if (!data.isUsed(item) && !isBanned()) {
                    activate(level, player, stack, item);
                    if (consumeSelf) {
                        consumeSelf(stack);
                    }
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
            if (level.isClientSide()) {
                Minecraft.getInstance().gameRenderer.displayItemActivation(stack);
            }
        }

        private void activate(@NotNull Level level, @NotNull Player player, @NotNull ItemStack stack, @NotNull Item item) {
            var data = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE.get());
            data.markUsed(item);
            CollectibleEvent.onUsed(player, item);
            playSuccess(level, player, stack);
            onUse(stack, level, player, this);
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

        private ItemStack consumeSelf(@NotNull ItemStack stack) {
            var stackCopy = stack.copy();
            stackCopy.shrink(1);
            return stackCopy;
        }

        @Override
        final public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
            return this.useTicks;
        }

        @Override
        final public @NotNull InteractionResultHolder<ItemStack> use(
                @NotNull Level level,
                @NotNull Player player,
                @NotNull InteractionHand usedHand
        ) {
            if (level instanceof ClientLevel) this.clientLevel = level;
            player.startUsingItem(usedHand);
            return InteractionResultHolder.consume(player.getItemInHand(usedHand));
        }

        @Override
        final public @NotNull ItemStack finishUsingItem(
                @NotNull ItemStack stack,
                @NotNull Level level,
                @NotNull LivingEntity livingEntity
        ) {
            if (livingEntity instanceof Player player && level instanceof ServerLevel serverLevel) {
                var data = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE.get());
                var cd = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE_COOLDOWN.get());
                if (canAlwaysUse || (!data.isUsed(this) && !isBanned() && cd.isReady(this, serverLevel.getServer().getTickCount()))) {
                    activate(level, player, stack, this);
                    if (consumeSelf) {
                        return consumeSelf(stack);
                    }
                } else {
                    double x = player.getX();
                    double y = player.getY();
                    double z = player.getZ();
                    SoundEvent failSound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.withDefaultNamespace("block.chest.locked"));
                    if (failSound != null && clientLevel != null) {
                        clientLevel.playLocalSound(
                                x, y, z,
                                failSound,
                                SoundSource.PLAYERS,
                                15.0f,
                                1.0f,
                                false
                        );
                    } else
                        LogUtils.getLogger().error("failSound or clientLevel in {} is null: failSound={}, clientLevel={}", this, failSound, clientLevel);
                }
            }
            return stack;
        }

        public abstract void onUse(
                @NotNull ItemStack stack,
                @NotNull Level level,
                @NotNull Player player,
                @NotNull CustomCollectibleItem self
        );
    }

    public record Levels(int minLevel, int maxLevel, int defaultLevel) {
    }
}
