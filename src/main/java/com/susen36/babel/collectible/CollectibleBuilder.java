package com.susen36.babel.collectible;

import com.susen36.babel.api.collectible.CollectibleRegistrar;
import com.susen36.babel.api.collectible.CollectibleRegistration;
import com.susen36.babel.api.collectible.CollectibleSpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

/** 将当前命名空间的声明加入物品 DeferredRegister。 */
public final class CollectibleBuilder implements CollectibleRegistrar {
    private static final Set<String> DECLARED_NAMESPACES = new HashSet<>();
    private static final Map<ResourceLocation, RegistryObject<Item>> DECLARED_COLLECTIBLES = new LinkedHashMap<>();

    private final String modid;
    private final DeferredRegister<Item> ITEMS;

    private CollectibleBuilder(@NotNull String modid, @NotNull IEventBus bus) {
        if (!DECLARED_NAMESPACES.add(modid)) {
            throw new IllegalStateException("Collectible registration already installed for " + modid);
        }
        this.modid = modid;
        this.ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, modid);
        ITEMS.register(bus);
    }

    public static void installExtensionPoint() {
        ModLoadingContext.get().registerExtensionPoint(CollectibleRegistration.class,
                () -> new CollectibleRegistration(CollectibleBuilder::new));
    }

    static Map<ResourceLocation, RegistryObject<Item>> getDeclaredCollectibles() {
        // 避免过早查询将不完整的目录永久缓存。注册完成后可重试。
        if (DECLARED_COLLECTIBLES.isEmpty() || DECLARED_COLLECTIBLES.values().stream().anyMatch(item -> !item.isPresent())) {
            throw new IllegalStateException("Collectible directory is not ready before item registration");
        }
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

    @Override
    public RegistryObject<Item> registerCollectible(String name, CollectibleSpec spec, Effect effect) {
        CollectibleTiers tier = switch (spec.tier()) {
            case CURSED -> CollectibleTiers.CURSED;
            case NORMAL -> CollectibleTiers.NORMAL;
            case RARE -> CollectibleTiers.RARE;
            case ADVANCED -> CollectibleTiers.ADVANCED;
        };
        CollectibleItem.Levels levels = new CollectibleItem.Levels(spec.minLevel(), spec.maxLevel(), spec.defaultLevel());
        CollectibleSpec.Activation settings = spec.activation();
        CollectibleActivation activation;
        if (settings == null) {
            activation = CollectibleActivation.forTier(tier);
        } else {
            activation = CollectibleActivation.builder()
                    .sound(settings.sound(), settings.volume(), settings.pitch())
                    .particle(settings.particle(), settings.particleCount())
                    .particleYOffset(settings.particleYOffset()).particleSpeed(settings.particleSpeed())
                    .showOverlay(settings.showOverlay()).autoUseOnGain(settings.autoUseOnGain()).build();
        }
        return registerCollectible(name, () -> new LambdaCollectibleItem(effect::onEffectStarted, spec.useTicks(),
                spec.canAlwaysUse(), tier, levels, activation));
    }

    @Override
    public RegistryObject<Item> registerCollectible(String name, Supplier<? extends Item> collectible) {
        return save(name, ITEMS.register(name, () -> {
            Item item = collectible.get();
            if (!(item instanceof CollectibleItem result)) {
                throw new IllegalArgumentException("Expected a collectible item for " + modid + ":" + name);
            }
            return result;
        }));
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