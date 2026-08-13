package com.susen36.babel.collectible;

import com.mojang.logging.LogUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class CollectibleItem extends Item implements CollectibleLike {
    private final CollectibleEffect effect;
    private final boolean consumeSelf;
    private final int useTicks;
    private final boolean canAlwaysUse;
    private Level clientLevel;
    private InteractionHand hand;

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf) {
        this(effect, consumeSelf, 25, false);
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, boolean canAlwaysUse) {
        this(effect, consumeSelf, 25, canAlwaysUse);
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, int useTicks) {
        this(effect, consumeSelf, useTicks, false);
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, int useTicks, boolean canAlwaysUse) {
        super(new Item.Properties());
        this.effect = effect;
        this.consumeSelf = consumeSelf;
        this.useTicks = useTicks;
        this.canAlwaysUse = canAlwaysUse;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand usedHand
    ) {
        if (level instanceof ClientLevel) this.clientLevel = level;
        this.hand = usedHand;
        player.startUsingItem(usedHand);
        return InteractionResultHolder.consume(player.getItemInHand(usedHand));
    }

    @Override
    public @NotNull ItemStack finishUsingItem(
            @NotNull ItemStack stack,
            @NotNull Level level,
            @NotNull LivingEntity livingEntity
    ) {
        if (livingEntity instanceof Player player) {
            var data = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE.get());
            if (!data.isUsed(player.getItemInHand(hand).getItem())) {
                data.markUsed(player.getItemInHand(hand).getItem());
                effect.onUse(stack, level, player, this);
                if (consumeSelf) {
                    return consumeSelf(stack);
                }
            } else if (!canAlwaysUse) {
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

    private ItemStack consumeSelf(@NotNull ItemStack stack) {
        var stackCopy = stack.copy();
        stackCopy.shrink(1);
        return stackCopy;
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
        private Level clientLevel;
        private InteractionHand hand;

        public CustomCollectibleItem(Properties properties, boolean consumeSelf, int useTicks, boolean canAlwaysUse) {
            super(properties);
            this.consumeSelf = consumeSelf;
            this.useTicks = useTicks;
            this.canAlwaysUse = canAlwaysUse;
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
                var data = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE.get());
                if (!data.isUsed(player.getItemInHand(hand).getItem())) {
                    data.markUsed(player.getItemInHand(hand).getItem());
                    this.onUse(stack, level, player, this);
                    if (consumeSelf) {
                        stack.shrink(1);
                        return stack;
                    }
                } else if (!canAlwaysUse) {
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
}
