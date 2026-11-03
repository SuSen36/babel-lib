package com.susen36.babel.mixin;

import com.susen36.babel.item.HandOfProtractionItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {
    @Redirect(method = "applyEffects",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
    private static List<Player> babel$captureAffectedPlayers(Level level, Class<Player> entityClass, AABB aabb) {
        List<Player> players = level.getEntitiesOfClass(entityClass, aabb);
        HandOfProtractionItem.markBeaconAffected(level, players);
        return players;
    }
}