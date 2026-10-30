package com.susen36.babel.mixin;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.susen36.babel.BabelMod;
import com.susen36.babel.loot.CollectibleLootModifier;
import com.susen36.babel.loot.OriginiumIngotLootModifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifierManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(LootModifierManager.class)
public class LootModifierManagerMixin {

    @SuppressWarnings("NullAway.Init")
    @Shadow(remap = false)
    private Map<ResourceLocation, IGlobalLootModifier> registeredLootModifiers;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("RETURN"), remap = false)
    private void babel$injectCollectibles(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        Map<ResourceLocation, IGlobalLootModifier> modifiers = new HashMap<>(registeredLootModifiers);
        modifiers.put(new ResourceLocation(BabelMod.MODID, "collectibles_in_chests"),
                new CollectibleLootModifier(new LootItemCondition[0]));
        modifiers.put(new ResourceLocation(BabelMod.MODID, "originium_ingot_from_entities"),
                new OriginiumIngotLootModifier(new LootItemCondition[0]));
        registeredLootModifiers = ImmutableMap.copyOf(modifiers);
    }
}