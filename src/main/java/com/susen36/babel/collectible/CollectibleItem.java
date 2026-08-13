package com.susen36.babel.collectible;

import com.susen36.babel.BabelConfig;
import com.susen36.babel.api.event.CollectibleEvent;
import com.susen36.babel.network.BabelNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
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

public class CollectibleItem extends Item implements CollectibleLike {
    private final CollectibleEffect effect;
    private final boolean consumeSelf;
    private final int useTicks;
    private final CollectibleTiers tier;
    private final int minLevel;
    private final int maxLevel;
    private final int defaultLevel;
    private final CollectibleActivation activation;
    private InteractionHand hand;

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf) {
        this(collectibleProperties(CollectibleTiers.NORMAL), effect, consumeSelf, 25, CollectibleTiers.NORMAL, 0, 1, 0, CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, int useTicks) {
        this(collectibleProperties(CollectibleTiers.NORMAL), effect, consumeSelf, useTicks, CollectibleTiers.NORMAL, 0, 1, 0, CollectibleActivation.forTier(CollectibleTiers.NORMAL));
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, int useTicks, CollectibleTiers tier) {
        this(collectibleProperties(tier), effect, consumeSelf, useTicks, tier, 0, 1, 0, CollectibleActivation.forTier(tier));
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, int useTicks, CollectibleTiers tier, int minLevel, int maxLevel, int defaultLevel) {
        this(collectibleProperties(tier), effect, consumeSelf, useTicks, tier, minLevel, maxLevel, defaultLevel, CollectibleActivation.forTier(tier));
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, int useTicks, CollectibleTiers tier, int minLevel, int maxLevel, int defaultLevel, CollectibleActivation activation) {
        this(collectibleProperties(tier), effect, consumeSelf, useTicks, tier, minLevel, maxLevel, defaultLevel, activation);
    }

    /** 收藏品统一物品属性：堆叠 1，稀有度按等级映射。 */
    private static Item.Properties collectibleProperties(CollectibleTiers tier) {
        return new Item.Properties().stacksTo(1).rarity(rarityForTier(tier));
    }

    /** 等级 → 基础稀有度；高级为 EPIC，稀有为 RARE，其余为 COMMON（诅咒另染红）。 */
    private static Rarity rarityForTier(CollectibleTiers tier) {
        if (tier == CollectibleTiers.RARE) {
            return Rarity.RARE;
        }
        if (tier == CollectibleTiers.ADVANCED) {
            return Rarity.EPIC;
        }
        return Rarity.COMMON;
    }

    /** 供自定义子类传入物品属性；effect 为 null 时由子类重写 {@link #onUse} 提供效果。 */
    protected CollectibleItem(Properties properties, CollectibleEffect effect, boolean consumeSelf, int useTicks, CollectibleTiers tier, int minLevel, int maxLevel, int defaultLevel, CollectibleActivation activation) {
        super(properties);
        this.effect = effect;
        this.consumeSelf = consumeSelf;
        this.useTicks = useTicks;
        this.tier = tier;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.defaultLevel = defaultLevel;
        this.activation = activation;
    }

    public CollectibleTiers getTier() {
        return tier;
    }

    /** 诅咒级收藏品物品名染红（同原版负面附魔），其余沿用等级稀有度颜色。 */
    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        if (tier == CollectibleTiers.CURSED) {
            return Component.translatable(this.getDescriptionId(stack)).withStyle(ChatFormatting.RED);
        }
        return super.getName(stack);
    }

    /** 所有收藏品通用两行描述：键名按注册名生成，颜色由本地化值内嵌的 § 代码控制。 */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
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
        return minLevel;
    }

    @Override
    public int maxLevel() {
        return maxLevel;
    }

    @Override
    public int defaultLevel() {
        return defaultLevel;
    }

    /** 高级收藏品被游戏规则禁用时，视为不可使用。 */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isBanned() {
        return tier == CollectibleTiers.ADVANCED && BabelConfig.banAdvancedCollectibles;
    }

    /** 收藏品效果模板方法：lambda 版委托 {@code effect}，自定义子类重写。 */
    protected void onUse(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player) {
        effect.onUse(stack, level, player);
    }

    @Override
    final public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand usedHand
    ) {
        this.hand = usedHand;
        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(player.getItemInHand(usedHand));
    }

    @Override
    final public @NotNull ItemStack finishUsingItem(
            @NotNull ItemStack stack,
            @NotNull Level level,
            @NotNull LivingEntity livingEntity
    ) {
        if (livingEntity instanceof Player player) {
            Item item = player.getItemInHand(hand).getItem();
            var data = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE.get());
            if (!data.isUsed(item) && !isBanned()) {
                activate(level, player, stack, item);
                if (consumeSelf) {
                    return consumeSelf(stack);
                }
            } else if (level instanceof ClientLevel clientLevel) {
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();
                SoundEvent failSound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.withDefaultNamespace("block.spawner.break"));
                if (failSound != null) {
                    clientLevel.playLocalSound(
                            x, y, z,
                            failSound,
                            SoundSource.PLAYERS,
                            1.0f,
                            1.0f,
                            false
                    );
                }
            }
        }
        return stack;
    }

    @Override
    final public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return this.useTicks;
    }

    private ItemStack consumeSelf(@NotNull ItemStack stack) {
        stack.shrink(1);
        return stack;
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
        onUse(stack, level, player);
        if (level instanceof ServerLevel) {
            BabelNetwork.syncCollectibles(player);
        }
    }

    /** 诅咒级收藏品：获得（进入背包）时自动激活一次。 */
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
                @NotNull Player player
        );
    }

    public abstract static class CustomCollectibleItem extends CollectibleItem {
        public CustomCollectibleItem(Properties properties, boolean consumeSelf, int useTicks) {
            super(properties, null, consumeSelf, useTicks, CollectibleTiers.NORMAL, 0, 1, 0, CollectibleActivation.forTier(CollectibleTiers.NORMAL));
        }

        public CustomCollectibleItem(Properties properties, boolean consumeSelf, int useTicks, CollectibleTiers tier) {
            super(properties, null, consumeSelf, useTicks, tier, 0, 1, 0, CollectibleActivation.forTier(tier));
        }

        public CustomCollectibleItem(Properties properties, boolean consumeSelf, int useTicks, CollectibleTiers tier, int minLevel, int maxLevel, int defaultLevel) {
            super(properties, null, consumeSelf, useTicks, tier, minLevel, maxLevel, defaultLevel, CollectibleActivation.forTier(tier));
        }

        public CustomCollectibleItem(Properties properties, boolean consumeSelf, int useTicks, CollectibleTiers tier, int minLevel, int maxLevel, int defaultLevel, CollectibleActivation activation) {
            super(properties, null, consumeSelf, useTicks, tier, minLevel, maxLevel, defaultLevel, activation);
        }

        @Override
        public abstract void onUse(
                @NotNull ItemStack stack,
                @NotNull Level level,
                @NotNull Player player
        );
    }
}