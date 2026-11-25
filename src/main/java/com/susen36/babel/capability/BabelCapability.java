package com.susen36.babel.capability;

import com.susen36.babel.BabelMod;
import com.susen36.babel.capability.ep.EPCapability;
import com.susen36.babel.capability.health.HealthCapability;
import com.susen36.babel.entity.robbery.RobberyCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

// Capability 注册与访问入口：为所有 LivingEntity 挂载 EP 与目标生命，为玩家额外挂载劫掠状态。
// getEP / getHealth 可能返回 null，调用前需判空。
@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class BabelCapability {

    public static final Capability<EPCapability> EP_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<HealthCapability> HEALTH_CAP = CapabilityManager.get(new CapabilityToken<>() {
    });

    public static final ResourceLocation EP_CAP_ID = new ResourceLocation(BabelMod.MODID, "ep_cap");
    public static final ResourceLocation HEALTH_CAP_ID = new ResourceLocation(BabelMod.MODID, "health_cap");

    private BabelCapability() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(BabelCapability::onRegisterCapabilities);
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(EPCapability.class);
        event.register(HealthCapability.class);
        event.register(RobberyCapability.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity living) {
            StoredCapability<EPCapability> ep = new StoredCapability<>(EP_CAP, "ep", () -> new EPCapability(living));
            StoredCapability<HealthCapability> health = new StoredCapability<>(HEALTH_CAP, "health", () -> new HealthCapability(living));
            event.addCapability(EP_CAP_ID, ep);
            event.addCapability(HEALTH_CAP_ID, health);
            event.addListener(ep.data::invalidate);
            event.addListener(health.data::invalidate);
        }
        if (event.getObject() instanceof Player) {
            RobberyCapability robbery = new RobberyCapability();
            event.addCapability(RobberyCapability.KEY, robbery);
            event.addListener(robbery::invalidate);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static EPCapability getEP(LivingEntity living) {
        return living.getCapability(EP_CAP).orElse(null);
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    public static HealthCapability getHealth(Entity entity) {
        return entity.getCapability(HEALTH_CAP).orElse(null);
    }

    private static final class StoredCapability<T extends INBTSerializable<CompoundTag>> implements ICapabilitySerializable<CompoundTag> {
        private final EntityCapabilityHandle<T> data;
        private final String key;

        private StoredCapability(Capability<T> type, String key, Supplier<T> factory) {
            this.data = new EntityCapabilityHandle<>(type, factory);
            this.key = key;
        }

        @Nonnull
        @Override
        public <R> LazyOptional<R> getCapability(@Nonnull Capability<R> cap, @Nullable Direction side) {
            return data.getCapability(cap, side);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put(key, data.get().serializeNBT());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            // 沿用旧键与子标签，只读取自己负责的分支，不重放另一键的副本。
            if (tag.contains(key, Tag.TAG_COMPOUND)) {
                data.get().deserializeNBT(tag.getCompound(key));
            }
        }
    }
}