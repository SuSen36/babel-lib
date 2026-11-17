package com.susen36.babel.collectible;

import com.susen36.babel.api.event.CollectibleRegisterEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CollectibleBuilder {
    private static final Map<ResourceLocation, RegistryObject<Item>> DECLARED_COLLECTIBLES = new LinkedHashMap<>();

    private final String modid;
    private final DeferredRegister<Item> ITEMS;

    private CollectibleBuilder(@NotNull String modid, @NotNull IEventBus bus) {
        this.modid = modid;
        this.ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, modid);
        ITEMS.register(bus);
        bus.post(new CollectibleRegisterEvent(this));
    }

    public static CollectibleBuilder create(@NotNull String modid, @NotNull IEventBus bus) {
        return new CollectibleBuilder(modid, bus);
    }

    public RegistryObject<Item> registerCollectible(String name, CollectibleItem.CollectibleEffect effect) {
        return registerCollectible(name, () -> new LambdaCollectibleItem(effect, 25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0), CollectibleActivation.forTier(CollectibleTiers.NORMAL)));
    }

    public RegistryObject<Item> registerCollectible(String name, boolean canAlwaysUse, CollectibleItem.CollectibleEffect effect) {
        return registerCollectible(name, () -> new LambdaCollectibleItem(effect, 25, canAlwaysUse, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0), CollectibleActivation.forTier(CollectibleTiers.NORMAL)));
    }

    public RegistryObject<Item> registerCollectible(String name, int useTicks, CollectibleItem.CollectibleEffect effect) {
        return registerCollectible(name, () -> new LambdaCollectibleItem(effect, useTicks, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0), CollectibleActivation.forTier(CollectibleTiers.NORMAL)));
    }

    public RegistryObject<Item> registerCollectible(String name, int useTicks, boolean canAlwaysUse, CollectibleItem.CollectibleEffect effect) {
        return registerCollectible(name, () -> new LambdaCollectibleItem(effect, useTicks, canAlwaysUse, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0), CollectibleActivation.forTier(CollectibleTiers.NORMAL)));
    }

    public RegistryObject<Item> registerCollectible(String name, boolean canAlwaysUse, CollectibleItem.Levels levels, CollectibleItem.CollectibleEffect effect) {
        return registerCollectible(name, () -> new LambdaCollectibleItem(effect, 25, canAlwaysUse, CollectibleTiers.NORMAL, levels, CollectibleActivation.forTier(CollectibleTiers.NORMAL)));
    }

    public RegistryObject<Item> registerCollectible(String name, int useTicks, CollectibleItem.Levels levels, CollectibleItem.CollectibleEffect effect) {
        return registerCollectible(name, () -> new LambdaCollectibleItem(effect, useTicks, false, CollectibleTiers.NORMAL, levels, CollectibleActivation.forTier(CollectibleTiers.NORMAL)));
    }

    public RegistryObject<Item> registerCollectible(String name, int useTicks, boolean canAlwaysUse, CollectibleItem.Levels levels, CollectibleItem.CollectibleEffect effect) {
        return registerCollectible(name, () -> new LambdaCollectibleItem(effect, useTicks, canAlwaysUse, CollectibleTiers.NORMAL, levels, CollectibleActivation.forTier(CollectibleTiers.NORMAL)));
    }

    public RegistryObject<Item> registerCollectible(String name, int useTicks, boolean canAlwaysUse, CollectibleTiers tiers, CollectibleItem.Levels levels, CollectibleActivation activation, CollectibleItem.CollectibleEffect effect) {
        return registerCollectible(name, () -> new LambdaCollectibleItem(effect, useTicks, canAlwaysUse, tiers, levels, activation));
    }

    public <T extends CollectibleItem> RegistryObject<Item> registerCollectible(String name, Supplier<T> item) {
        return save(name, ITEMS.register(name, () -> item.get()));
    }

    static Map<ResourceLocation, RegistryObject<Item>> getDeclaredCollectibles() {
        return Collections.unmodifiableMap(DECLARED_COLLECTIBLES);
    }

    private RegistryObject<Item> save(String name, RegistryObject<Item> item) {
        ResourceLocation id = new ResourceLocation(this.modid, name);
        RegistryObject<Item> existing = DECLARED_COLLECTIBLES.get(id);
        if (existing != null) {
            throw new IllegalStateException("Adding duplicate collectible '" + id
                    + "' to collectible registry, conflicting: '" + existing.getId() + "' and '" + item.getId() + "'");
        }
        DECLARED_COLLECTIBLES.put(id, item);
        return item;
    }

    private static final class LambdaCollectibleItem extends CollectibleItem {
        private final CollectibleEffect effect;

        private LambdaCollectibleItem(CollectibleEffect effect, int useTicks, boolean canAlwaysUse, CollectibleTiers tier, Levels levels, CollectibleActivation activation) {
            super(new Properties().stacksTo(1), useTicks, canAlwaysUse, tier, levels, activation);
            this.effect = effect;
        }

        @Override
        public void onEffectStarted(@NotNull ItemStack stack, @NotNull Level level, @NotNull Player player) {
            this.effect.onEffectStarted(stack, level, player);
        }
    }
}