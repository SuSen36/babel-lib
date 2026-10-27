package com.susen36.babel.block;

import com.susen36.babel.init.BabelBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class KingPedestalBlockEntity extends BlockEntity {
    private ItemStack item = ItemStack.EMPTY;

    public KingPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(BabelBlockEntities.KING_PEDESTAL.get(), pos, state);
    }

    public ItemStack getItem() {
        return this.item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.item.isEmpty()) {
            tag.put("Item", this.item.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Item")) {
            this.item = ItemStack.of(tag.getCompound("Item"));
        } else {
            this.item = ItemStack.EMPTY;
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}