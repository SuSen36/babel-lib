package com.susen36.babel.collectible;

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

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf) {
        this(effect, consumeSelf, 25);
    }

    public CollectibleItem(CollectibleEffect effect, boolean consumeSelf, int useTicks) {
        super(new Item.Properties());
        this.effect = effect;
        this.consumeSelf = consumeSelf;
        this.useTicks = useTicks;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand usedHand
    ) {
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
            effect.onUse(stack, level, player);
            if (consumeSelf) {
                return consumeSelf(stack);
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
                @NotNull Player player
        );
    }

    public abstract static class CustomCollectibleItem extends Item implements CollectibleLike {
        private final boolean consumeSelf;
        private final int useTicks;

        public CustomCollectibleItem(Properties properties, boolean consumeSelf, int useTicks) {
            super(properties);
            this.consumeSelf = consumeSelf;
            this.useTicks = useTicks;
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
            var data = player.getData(Collectibles.ATTACHMENT_COLLECTIBLE.get());
            if (!data.isUsed(player.getItemInHand(usedHand).getItem())) {
                data.markUsed(player.getItemInHand(usedHand).getItem());
                player.startUsingItem(usedHand);
            } else if (level instanceof ClientLevel clientLevel) {
                double x = player.getX();
                double y = player.getY();
                double z = player.getZ();
                SoundEvent failSound = BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("minecraft:block.spawner.break"));
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
            return InteractionResultHolder.consume(player.getItemInHand(usedHand));
        }

        @Override
        final public @NotNull ItemStack finishUsingItem(
                @NotNull ItemStack stack,
                @NotNull Level level,
                @NotNull LivingEntity livingEntity
        ) {
            if (livingEntity instanceof Player player) {
                this.onUse(stack, level, player);
                if (consumeSelf) {
                    stack.shrink(1);
                    return stack;
                }
            }
            return stack;
        }

        public abstract void onUse(
                @NotNull ItemStack stack,
                @NotNull Level level,
                @NotNull Player player
        );
    }
}
