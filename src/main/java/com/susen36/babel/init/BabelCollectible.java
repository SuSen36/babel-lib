package com.susen36.babel.init;

import com.susen36.babel.BabelMod;
import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleBuilder;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.item.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.function.Supplier;

public class BabelCollectible {
    public static CollectibleBuilder REGISTRY;

    public static RegistryObject<Item> ORIGINIUM_IRIS_FLOWER;
    public static RegistryObject<Item> UTIL_OMNIKEY;
    public static RegistryObject<Item> VOYAGE_OF_GOLD;
    public static RegistryObject<Item> PIGLIN_DIARY;
    public static RegistryObject<Item> KETTLE;
    public static RegistryObject<Item> BAT_BED;
    public static RegistryObject<Item> RAINBOW_CANDY;
    public static RegistryObject<Item> HAND_FERTILITY;
    public static RegistryObject<Item> HAND_THORNS;
    public static RegistryObject<Item> HAND_STRANGLE;
    public static RegistryObject<Item> HAND_SPEED;
    public static RegistryObject<Item> HAND_FIREWORK;
    public static RegistryObject<Item> HAND_SWIPE;
    public static RegistryObject<Item> HAND_OF_BARREN;
    public static RegistryObject<Item> HAND_SWORD;
    public static RegistryObject<Item> HAND_OF_ENGRAVE;
    public static RegistryObject<Item> HAND_OF_SNATCHER;
    public static RegistryObject<Item> HAND_OF_DIFFUSION;
    public static RegistryObject<Item> HAND_OF_FLOWING_WATER;
    public static RegistryObject<Item> FLAWLESS_JADESTONE;

    public static RegistryObject<Item> LEGEND_CHITIN;
    public static RegistryObject<Item> DISO;
    public static RegistryObject<Item> AHND_SWIPE;
    public static RegistryObject<Item> HANSHAND_SPIKE;
    public static RegistryObject<Item> SARKAZ_KING_RYLFATE;
    public static RegistryObject<Item> DURIN_OVERGROUND_ODYSSEY;
    public static RegistryObject<Item> CURSED_HEART;
    public static RegistryObject<Item> KING_CROWN;
    public static RegistryObject<Item> SARKAZ_KING_BED;

    public static RegistryObject<Item> VAMPIRES_BED;
    public static RegistryObject<Item> SARKAZ_KING_FLAG;
    public static RegistryObject<Item> SARKAZ_KING_ARTIFACT;
    public static RegistryObject<Item> TREATY;
    public static RegistryObject<Item> BOWL_SEAGRASS;
    public static RegistryObject<Item> UTIL_STARE;
    public static RegistryObject<Item> PROOF_OF_LONGEVITY;
    public static RegistryObject<Item> HEMOST;
    public static RegistryObject<Item> SOLO_MUSIC_BOX;
    public static RegistryObject<Item> TOPONYM_TEXTOLOGY;
    public static RegistryObject<Item> YEARNING;

    public static RegistryObject<Item> UTIL_ALLAY;
    public static RegistryObject<Item> KING_ARMOR;
    public static RegistryObject<Item> KING_SPEAR;
    public static RegistryObject<Item> KING_EXTENSION;
    public static RegistryObject<Item> KING_CRYSTAL;
    public static RegistryObject<Item> RELIC_CROWN;
    public static RegistryObject<Item> SURVIVOR_CONTRACT;
    public static RegistryObject<Item> ROYAL_FATE;
    public static RegistryObject<Item> CANNED_CHERRY;
    public static RegistryObject<Item> GOLDEN_CHALISE;

    public static RegistryObject<Item> COFFEE_CANDY;
    public static RegistryObject<Item> GOLDEN_STORM;
    public static RegistryObject<Item> ODD_FLUTE;
    public static RegistryObject<Item> FEATURED_CANNED_MEAT;
    public static RegistryObject<Item> UTIL_RESCISSION;
    public static RegistryObject<Item> OLD_STEAM_ARMOR;
    public static RegistryObject<Item> BRILLIANT_LAMENT;
    public static RegistryObject<Item> LIVE_ROSE;
    public static RegistryObject<Item> KNIGHTS_CODE;
    public static RegistryObject<Item> DARKNIGHT_WHISPERER;
    public static RegistryObject<Item> GODMOTHERS_TOKEN;
    public static RegistryObject<Item> GAULISH_SILVER_COIN;
    public static RegistryObject<Item> GAULISH_BANK_CHECK;
    public static RegistryObject<Item> TOPOGRAPHICAL_MAP;
    public static RegistryObject<Item> DIVINE_CONCH;

    public static final ArrayList<RegistryObject<Item>> BABEL_COLLECTIBLES = new ArrayList<>();

    private static final CollectibleActivation BOOLEAN_ACTIVATION = CollectibleActivation.builder()
            .sound(SoundEvents.PLAYER_LEVELUP, 2F, 1F)
            .particle(ParticleTypes.HAPPY_VILLAGER, 72)
            .showOverlay(true)
            .build();

    public static void register(IEventBus bus) {
        REGISTRY = CollectibleBuilder.create(BabelMod.MODID, bus);
        BABEL_COLLECTIBLES.add(ORIGINIUM_IRIS_FLOWER = REGISTRY.registerCollectible("originium_iris_flower", OriginiumIrisFlowerItem::new));
        BABEL_COLLECTIBLES.add(UTIL_OMNIKEY = REGISTRY.registerCollectible("util_omnikey", OmniKeyItem::new));
        BABEL_COLLECTIBLES.add(VOYAGE_OF_GOLD = REGISTRY.registerCollectible("voyage_of_gold", VoyageOfGoldItem::new));
        BABEL_COLLECTIBLES.add(PIGLIN_DIARY = REGISTRY.registerCollectible("piglin_diary", PiglinDiaryItem::new));
        BABEL_COLLECTIBLES.add(KETTLE = REGISTRY.registerCollectible("kettle", KettleItem::new));
        BABEL_COLLECTIBLES.add(BAT_BED = REGISTRY.registerCollectible("bat_bed", BatBedItem::new));
        BABEL_COLLECTIBLES.add(RAINBOW_CANDY = REGISTRY.registerCollectible("rainbow_candy", RainbowCandyItem::new));
        BABEL_COLLECTIBLES.add(HAND_FERTILITY = REGISTRY.registerCollectible("hand_fertility", HandFertilityItem::new));
        BABEL_COLLECTIBLES.add(HAND_THORNS = REGISTRY.registerCollectible("hand_thorns", HandThornsItem::new));
        BABEL_COLLECTIBLES.add(HAND_STRANGLE = REGISTRY.registerCollectible("hand_strangle", HandStrangleItem::new));
        BABEL_COLLECTIBLES.add(HAND_SPEED = REGISTRY.registerCollectible("hand_speed", HandSpeedItem::new));
        BABEL_COLLECTIBLES.add(HAND_FIREWORK = REGISTRY.registerCollectible("hand_firework", HandFireworkItem::new));
        BABEL_COLLECTIBLES.add(HAND_SWIPE = REGISTRY.registerCollectible("hand_swipe", HandSwipeItem::new));
        BABEL_COLLECTIBLES.add(HAND_OF_BARREN = REGISTRY.registerCollectible("hand_of_barren", HandOfBarrenItem::new));
        BABEL_COLLECTIBLES.add(HAND_SWORD = REGISTRY.registerCollectible("hand_sword", HandSwordItem::new));
        BABEL_COLLECTIBLES.add(HAND_OF_ENGRAVE = REGISTRY.registerCollectible("hand_of_engrave", HandOfEngraveItem::new));
        BABEL_COLLECTIBLES.add(HAND_OF_SNATCHER = REGISTRY.registerCollectible("hand_of_snatcher", HandOfSnatcherItem::new));
        BABEL_COLLECTIBLES.add(HAND_OF_DIFFUSION = REGISTRY.registerCollectible("hand_of_diffusion", HandOfDiffusionItem::new));
        BABEL_COLLECTIBLES.add(HAND_OF_FLOWING_WATER = REGISTRY.registerCollectible("hand_of_flowing_water", HandOfFlowingWaterItem::new));

        LEGEND_CHITIN = REGISTRY.registerCollectible("legend_chitin", LegendChitinItem::new);
        DISO = registerBoolean("diso");
        AHND_SWIPE = registerBoolean("ahnd_swipe");
        HANSHAND_SPIKE = registerBoolean("hanshand_spike");
        SARKAZ_KING_RYLFATE = registerBoolean("sarkaz_king_rylfate");
        DURIN_OVERGROUND_ODYSSEY = registerBoolean("durin_overground_odyssey");
        CURSED_HEART = registerBoolean("cursed_heart");
        KING_CROWN = REGISTRY.registerCollectible("king_crown", KingCrownItem::new);
        SARKAZ_KING_BED = REGISTRY.registerCollectible("sarkaz_king_bed", SarkazKingBedItem::new);

        BABEL_COLLECTIBLES.add(VAMPIRES_BED = REGISTRY.registerCollectible("vampires_bed", ArchfiendsBedItem::new));
        BABEL_COLLECTIBLES.add(SARKAZ_KING_FLAG = REGISTRY.registerCollectible("sarkaz_king_flag", ArchfiendsFlagItem::new));
        BABEL_COLLECTIBLES.add(SARKAZ_KING_ARTIFACT = REGISTRY.registerCollectible("sarkaz_king_artifact", ArchfiendsArtifactItem::new));
        BABEL_COLLECTIBLES.add(TREATY = REGISTRY.registerCollectible("treaty", CrimsonTreatyItem::new));
        BABEL_COLLECTIBLES.add(BOWL_SEAGRASS = REGISTRY.registerCollectible("bowl_seagrass", BowlSeagrassItem::new));
        BABEL_COLLECTIBLES.add(UTIL_STARE = REGISTRY.registerCollectible("util_stare", GuardianStareItem::new));
        BABEL_COLLECTIBLES.add(PROOF_OF_LONGEVITY = REGISTRY.registerCollectible("proof_of_longevity", ProofOfLongevityItem::new));
        BABEL_COLLECTIBLES.add(HEMOST = REGISTRY.registerCollectible("hemost", SmellyHemostaticItem::new));
        BABEL_COLLECTIBLES.add(SOLO_MUSIC_BOX = REGISTRY.registerCollectible("solo_music_box", SoloMusicBoxItem::new));
        BABEL_COLLECTIBLES.add(TOPONYM_TEXTOLOGY = REGISTRY.registerCollectible("toponym_textology", ToponymTextologyItem::new));
        BABEL_COLLECTIBLES.add(YEARNING = REGISTRY.registerCollectible("yearning", UnripeYearningItem::new));
        BABEL_COLLECTIBLES.add(UTIL_ALLAY = REGISTRY.registerCollectible("util_allay", AlleySculptureItem::new));
        BABEL_COLLECTIBLES.add(KING_ARMOR = REGISTRY.registerCollectible("king_armor", KingsArmourItem::new));
        BABEL_COLLECTIBLES.add(KING_SPEAR = REGISTRY.registerCollectible("king_spear", KingsSpearItem::new));
        BABEL_COLLECTIBLES.add(KING_EXTENSION = REGISTRY.registerCollectible("king_extension", KingsExtensionItem::new));
        BABEL_COLLECTIBLES.add(KING_CRYSTAL = REGISTRY.registerCollectible("king_crystal", KingsCrystalItem::new));
        BABEL_COLLECTIBLES.add(RELIC_CROWN = REGISTRY.registerCollectible("relic_crown", RelicCrownItem::new));
        BABEL_COLLECTIBLES.add(SURVIVOR_CONTRACT = REGISTRY.registerCollectible("survivor_contract", SurvivorContractItem::new));
        BABEL_COLLECTIBLES.add(ROYAL_FATE = REGISTRY.registerCollectible("royal_fate", RoyalFateItem::new));
        BABEL_COLLECTIBLES.add(CANNED_CHERRY = REGISTRY.registerCollectible("canned_cherry", CannedCherryItem::new));
        BABEL_COLLECTIBLES.add(GOLDEN_CHALISE = REGISTRY.registerCollectible("golden_chalise", GoldenChaliseItem::new));
        BABEL_COLLECTIBLES.add(COFFEE_CANDY = REGISTRY.registerCollectible("coffee_candy", CoffeeCandyItem::new));
        BABEL_COLLECTIBLES.add(GOLDEN_STORM = REGISTRY.registerCollectible("golden_storm", GoldenStormItem::new));
        BABEL_COLLECTIBLES.add(ODD_FLUTE = REGISTRY.registerCollectible("odd_flute", OddFluteItem::new));
        BABEL_COLLECTIBLES.add(FEATURED_CANNED_MEAT = REGISTRY.registerCollectible("featured_canned_meat", MeatCanItem::new));
        BABEL_COLLECTIBLES.add(UTIL_RESCISSION = REGISTRY.registerCollectible("util_rescission", RescissionItem::new));
        BABEL_COLLECTIBLES.add(OLD_STEAM_ARMOR = REGISTRY.registerCollectible("old_steam_armor", OldSteamArmorItem::new));
        BABEL_COLLECTIBLES.add(BRILLIANT_LAMENT = REGISTRY.registerCollectible("brilliant_lament", BrilliantLamentItem::new));
        BABEL_COLLECTIBLES.add(LIVE_ROSE = REGISTRY.registerCollectible("live_rose", LiveRoseItem::new));
        BABEL_COLLECTIBLES.add(KNIGHTS_CODE = REGISTRY.registerCollectible("knights_code", KnightsCodeItem::new));
        BABEL_COLLECTIBLES.add(DARKNIGHT_WHISPERER = REGISTRY.registerCollectible("darknight_whisperer", DarknightWhispererItem::new));
        BABEL_COLLECTIBLES.add(GAULISH_SILVER_COIN = REGISTRY.registerCollectible("gaulish_silver_coin", GaulishSilverCoinItem::new));
        BABEL_COLLECTIBLES.add(GAULISH_BANK_CHECK = REGISTRY.registerCollectible("gaulish_bank_check", GaulishBankCheckItem::new));
        BABEL_COLLECTIBLES.add(TOPOGRAPHICAL_MAP = REGISTRY.registerCollectible("topographical_map", TopographicalMapItem::new));
        BABEL_COLLECTIBLES.add(GODMOTHERS_TOKEN = REGISTRY.registerCollectible("godmothers_token", GodmothersTokenItem::new));
        BABEL_COLLECTIBLES.add(DIVINE_CONCH = REGISTRY.registerCollectible("divine_conch", DivineConchItem::new));
        BABEL_COLLECTIBLES.add(FLAWLESS_JADESTONE = REGISTRY.registerCollectible("flawless_jadestone", FlawlessJadestoneItem::new));
    }

    private static RegistryObject<Item> registerBoolean(String name) {
        Supplier<CollectibleItem.CustomCollectibleItem> supplier = () -> new CollectibleItem.CustomCollectibleItem(
                new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),
                25, false, CollectibleTiers.NORMAL, new CollectibleItem.Levels(0, 1, 0), BOOLEAN_ACTIVATION) {
            @Override
            public void onEffectStarted(ItemStack stack, Level level, Player player, int layer) {
            }
        };
        return REGISTRY.registerCollectible(name, supplier);
    }

    private BabelCollectible() {
        throw new UnsupportedOperationException("Utility class");
    }
}
