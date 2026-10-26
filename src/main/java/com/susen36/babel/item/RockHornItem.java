package com.susen36.babel.item;

import com.susen36.babel.collectible.CollectibleActivation;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;

import java.util.Map;
import java.util.WeakHashMap;

public class RockHornItem extends CollectibleItem.CustomCollectibleItem {

    /** 以玩家为中心的 16×16×16 立方体半边长。 */
    private static final double PLAYER_RANGE = 8.0D;
    private static final int MAX_PLAYERS = 10;

    /** 以扫描所在的 server tick 为键，同一周期内的回调累计计数，进入下一周期自动重置。 */
    private record Scan(int tick, int count) {
    }

    private static final Map<Player, Scan> NEARBY_SCANS = new WeakHashMap<>();

    public RockHornItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON), 25, false, CollectibleTiers.NORMAL,
                new CollectibleItem.Levels(0, 1, 0),
                CollectibleActivation.builder()
                        .sound(SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0).value(), 16F, 1F)
                        .particle(ParticleTypes.NOTE, 72)
                        .showOverlay(true)
                        .build());
        addRangeEffect(Player.class, PLAYER_RANGE, (nearby, player, layer) -> {
            Scan scan = NEARBY_SCANS.get(player);
            int count = scan != null && scan.tick() == player.tickCount ? scan.count() : 0;
            NEARBY_SCANS.put(player, new Scan(player.tickCount, count + 1));
        });
        addConditionalAttributeModifier(Attributes.ATTACK_DAMAGE, 0.2, AttributeModifier.Operation.MULTIPLY_BASE,
                (player, self) -> nearbyPlayerStacks(player));
    }

    private static int nearbyPlayerStacks(Player player) {
        Scan scan = NEARBY_SCANS.get(player);
        int stacks = 0;
        if (scan != null && player.tickCount - scan.tick() <= 20) {
            stacks = Mth.clamp(scan.count(), 0, MAX_PLAYERS);
        }
        return stacks;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }
}