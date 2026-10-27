package com.susen36.babel.block;

import com.susen36.babel.init.BabelBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GoldenChaliseBlockEntity extends BlockEntity {
    private int amount;

    public GoldenChaliseBlockEntity(BlockPos pos, BlockState state) {
        super(BabelBlockEntities.GOLDEN_CHALISE.get(), pos, state);
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Amount", this.amount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.amount = tag.getInt("Amount");
    }
}