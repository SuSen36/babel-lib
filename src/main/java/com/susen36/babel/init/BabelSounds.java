package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BabelSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BabelMod.MODID);

    public static final RegistryObject<SoundEvent> MONEY_IN = REGISTRY.register("money_in", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BabelMod.MODID, "money_in")));
    public static final RegistryObject<SoundEvent> MONEY_OUT = REGISTRY.register("money_out", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BabelMod.MODID, "money_out")));
    public static final RegistryObject<SoundEvent> FLUTESONG = REGISTRY.register("flutesong", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BabelMod.MODID, "flutesong")));
    public static final RegistryObject<SoundEvent> TARGET_DAMAGED = REGISTRY.register("target_damaged", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BabelMod.MODID, "target_damaged")));
    public static final RegistryObject<SoundEvent> BARRIER = REGISTRY.register("barrier", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BabelMod.MODID, "barrier")));

    private BabelSounds() {
        throw new UnsupportedOperationException("Utility class");
    }
}