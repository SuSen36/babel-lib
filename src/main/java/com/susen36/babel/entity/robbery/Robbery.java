package com.susen36.babel.entity.robbery;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.susen36.babel.init.BabelMobEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Robbery {
    // 懒加载：静态初始化若早于药水效果注册，RegistryObject.get() 会抛异常
    public static Robbery defaultConfig() {
        return new Robbery(4, 6, 5, 4, 10, 3, 300,
            460, 8.0F, 2.0F,
            List.of(
                    new SpawnEntry(5, EntityType.VINDICATOR, null, 1),
                    new SpawnEntry(3, EntityType.PILLAGER, null, 1)
            ),
            List.of(
                    new SupportGroup(List.of(
                            new SupportEffect(MobEffects.DAMAGE_BOOST, 200, 4),
                            new SupportEffect(MobEffects.MOVEMENT_SPEED, 200, 4)
                    )),
                    new SupportGroup(List.of(
                            new SupportEffect(MobEffects.DAMAGE_RESISTANCE, 200, 4),
                            new SupportEffect(MobEffects.REGENERATION, 100, 1)
                    )),
                    new SupportGroup(List.of(
                            new SupportEffect(MobEffects.HEALTH_BOOST, 200, 1),
                            new SupportEffect(MobEffects.REGENERATION, 100, 1)
                    )),
                    new SupportGroup(List.of(
                            new SupportEffect(MobEffects.DAMAGE_BOOST, 200, 4),
                            new SupportEffect(MobEffects.DAMAGE_RESISTANCE, 200, 4)
                    )),
                    new SupportGroup(List.of(
                            new SupportEffect(BabelMobEffects.SLEEP.get(), 200, 0),
                            new SupportEffect(MobEffects.REGENERATION, 200, 1)
                    )),
                    new SupportGroup(List.of(
                            new SupportEffect(BabelMobEffects.PHYSICAL_DODGE.get(), 200, 4),
                            new SupportEffect(BabelMobEffects.MAGIC_DODGE.get(), 200, 4),
                            new SupportEffect(MobEffects.MOVEMENT_SPEED, 200, 4)
                    ))
            ), Component.translatable("event.babel.robbery.bossbar"));
    }

    private final int spawnRange;
    private final int mobsPerWave;
    private final int waveCount;
    private final int simultaneousMobs;
    private final int ticksBetweenSpawn;
    private final int initialSpawnCount;
    private final int waveIntervalTicks;
    private final int supportIntervalTicks;
    private final float supportRadius;
    private final float supportHealPerPulse;
    private final List<SpawnEntry> spawnPotentials;
    private final List<SupportGroup> supportGroups;
    private final Component bossBarName;

    public Robbery(int spawnRange, int mobsPerWave, int waveCount, int simultaneousMobs,
                   int ticksBetweenSpawn, int initialSpawnCount, int waveIntervalTicks, int supportIntervalTicks,
                   float supportRadius, float supportHealPerPulse, List<SpawnEntry> spawnPotentials,
                   List<SupportGroup> supportGroups, Component bossBarName) {
        this.spawnRange = spawnRange;
        this.mobsPerWave = mobsPerWave;
        this.waveCount = waveCount;
        this.simultaneousMobs = simultaneousMobs;
        this.ticksBetweenSpawn = ticksBetweenSpawn;
        this.initialSpawnCount = initialSpawnCount;
        this.waveIntervalTicks = waveIntervalTicks;
        this.supportIntervalTicks = supportIntervalTicks;
        this.supportRadius = supportRadius;
        this.supportHealPerPulse = supportHealPerPulse;
        this.spawnPotentials = List.copyOf(spawnPotentials);
        this.supportGroups = List.copyOf(supportGroups);
        this.bossBarName = bossBarName;
    }

    public int getSpawnRange() {
        return this.spawnRange;
    }

    public int getMobsPerWave() {
        return this.mobsPerWave;
    }

    public int getWaveCount() {
        return this.waveCount;
    }

    public int getWaveIntervalTicks() {
        return this.waveIntervalTicks;
    }

    public int getSimultaneousMobs() {
        return this.simultaneousMobs;
    }

    public int getTicksBetweenSpawn() {
        return this.ticksBetweenSpawn;
    }

    public int getInitialSpawnCount() {
        return this.initialSpawnCount;
    }

    public int getSupportIntervalTicks() {
        return this.supportIntervalTicks;
    }

    public float getSupportRadius() {
        return this.supportRadius;
    }

    public float getSupportHealPerPulse() {
        return this.supportHealPerPulse;
    }

    public List<SpawnEntry> getSpawnPotentials() {
        return this.spawnPotentials;
    }

    public List<SupportGroup> getSupportGroups() {
        return this.supportGroups;
    }

    public Component getBossBarName() {
        return this.bossBarName;
    }

    public static Robbery fromJson(JsonObject json) {
        int spawnRange = GsonHelper.getAsInt(json, "spawn_range", 4);
        int mobsPerWave = GsonHelper.getAsInt(json, "mobs_per_wave", 6);
        int waveCount = GsonHelper.getAsInt(json, "wave_count", 5);
        int waveIntervalTicks = GsonHelper.getAsInt(json, "wave_interval_ticks", 300);
        int supportIntervalTicks = GsonHelper.getAsInt(json, "support_interval_ticks", 460);
        int simultaneousMobs = GsonHelper.getAsInt(json, "simultaneous_mobs", 4);
        int ticksBetweenSpawn = GsonHelper.getAsInt(json, "ticks_between_spawn", 10);
        int initialSpawnCount = GsonHelper.getAsInt(json, "initial_spawn_count", 3);
        float supportRadius = GsonHelper.getAsFloat(json, "support_radius", 8.0F);
        float supportHealPerPulse = GsonHelper.getAsFloat(json, "support_heal_per_pulse", 2.0F);
        List<SpawnEntry> spawnPotentials = new ArrayList<>();
        JsonArray potentials = GsonHelper.getAsJsonArray(json, "spawn_potentials", null);
        if (potentials != null) {
            for (int i = 0; i < potentials.size(); ++i) {
                spawnPotentials.add(SpawnEntry.fromJson(GsonHelper.convertToJsonObject(potentials.get(i), "spawn_potentials[" + i + "]")));
            }
        }
        if (spawnPotentials.isEmpty()) {
            throw new JsonSyntaxException("robbery config requires at least one spawn_potentials entry");
        }
        List<SupportGroup> supportGroups = new ArrayList<>();
        JsonArray groups = GsonHelper.getAsJsonArray(json, "effect_groups", null);
        if (groups != null) {
            for (int i = 0; i < groups.size(); ++i) {
                supportGroups.add(SupportGroup.fromJson(GsonHelper.convertToJsonObject(groups.get(i), "effect_groups[" + i + "]")));
            }
        }
        if (supportGroups.isEmpty()) {
            throw new JsonSyntaxException("robbery config requires at least one effect_groups entry");
        }
        Component bossBarName = Component.translatable("event.babel.robbery.bossbar");
        if (json.has("boss_bar_name")) {
            Component parsed = Component.Serializer.fromJson(json.get("boss_bar_name"));
            if (parsed != null) {
                bossBarName = parsed;
            }
        }
        return new Robbery(spawnRange, mobsPerWave, waveCount, simultaneousMobs,
                ticksBetweenSpawn, initialSpawnCount, waveIntervalTicks, supportIntervalTicks, supportRadius,
                supportHealPerPulse, spawnPotentials, supportGroups, bossBarName);
    }

    public record SpawnEntry(int weight, EntityType<?> type, @Nullable CompoundTag tag, int minWave) {
        private static SpawnEntry fromJson(JsonObject json) {
            int weight = GsonHelper.getAsInt(json, "weight", 1);
            String entityId = GsonHelper.getAsString(json, "entity");
            ResourceLocation entityKey = new ResourceLocation(entityId);
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(entityKey);
            if (type == null) {
                throw new JsonSyntaxException("Unknown entity id in robbery spawn entry: " + entityId);
            }
            CompoundTag tag = null;
            if (json.has("nbt")) {
                try {
                    tag = TagParser.parseTag(GsonHelper.getAsString(json, "nbt"));
                } catch (CommandSyntaxException e) {
                    throw new JsonSyntaxException("Invalid nbt in robbery spawn entry: " + e.getMessage());
                }
            }
            int minWave = Math.max(1, GsonHelper.getAsInt(json, "min_wave", 1));
            return new SpawnEntry(weight, type, tag, minWave);
        }
    }

    public record SupportGroup(List<SupportEffect> effects) {
        private static SupportGroup fromJson(JsonObject json) {
            JsonArray effects = GsonHelper.getAsJsonArray(json, "effects");
            List<SupportEffect> list = new ArrayList<>();
            for (int i = 0; i < effects.size(); ++i) {
                list.add(SupportEffect.fromJson(GsonHelper.convertToJsonObject(effects.get(i), "effects[" + i + "]")));
            }
            if (list.isEmpty()) {
                throw new JsonSyntaxException("effect group requires at least one effect");
            }
            return new SupportGroup(list);
        }
    }

    public record SupportEffect(MobEffect effect, int duration, int amplifier) {
        private static SupportEffect fromJson(JsonObject json) {
            String effectId = GsonHelper.getAsString(json, "effect");
            ResourceLocation effectKey = new ResourceLocation(effectId);
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectKey);
            if (effect == null) {
                throw new JsonSyntaxException("Unknown effect id in robbery support entry: " + effectId);
            }
            int duration = GsonHelper.getAsInt(json, "duration", 100);
            int amplifier = GsonHelper.getAsInt(json, "amplifier", 0);
            return new SupportEffect(effect, duration, amplifier);
        }
    }
}