package com.susen36.babel.robbery;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.susen36.babel.BabelMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = BabelMod.MODID)
public class Robberies extends SimpleJsonResourceReloadListener {
    public static final ResourceLocation DEFAULT_ID = new ResourceLocation(BabelMod.MODID, "default");
    private static final Map<ResourceLocation, Robbery> CONFIGS = new HashMap<>();

    public Robberies() {
        super(new Gson(), "robberies");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager, ProfilerFiller profiler) {
        CONFIGS.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : files.entrySet()) {
            try {
                CONFIGS.put(entry.getKey(), Robbery.fromJson(GsonHelper.convertToJsonObject(entry.getValue(), "robbery config")));
            } catch (Exception e) {
                BabelMod.LOGGER.error("Failed to parse robbery config {}", entry.getKey(), e);
            }
        }
        BabelMod.LOGGER.info("Loaded {} robbery config(s)", CONFIGS.size());
    }

    public static Robbery get(ResourceLocation id) {
        return CONFIGS.getOrDefault(id, Robbery.DEFAULT);
    }

    @Nullable
    public static Map.Entry<ResourceLocation, Robbery> getRandom(RandomSource random) {
        Map.Entry<ResourceLocation, Robbery> picked = null;
        if (!CONFIGS.isEmpty()) {
            List<Map.Entry<ResourceLocation, Robbery>> entries = new ArrayList<>(CONFIGS.entrySet());
            picked = entries.get(random.nextInt(entries.size()));
        }
        return picked;
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new Robberies());
    }
}
