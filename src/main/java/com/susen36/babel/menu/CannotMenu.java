package com.susen36.babel.menu;

import com.susen36.babel.api.event.LostAndFoundEvent;
import com.susen36.babel.collectible.CollectibleItem;
import com.susen36.babel.collectible.CollectibleTiers;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.entity.CannotEntity;
import com.susen36.babel.init.BabelMenus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CannotMenu extends AbstractContainerMenu {
    public static final int PAYMENT_SLOT = 0;
    public static final int RESULT_SLOT = 1;
    public static final int LOST_AND_FOUND_ID = 1000;
    public static final int ATTACK_BUTTON_ID = 2000;
    private static final double ATTACK_REACH_SQR = 81.0D;
    private static final int RESULT_CONTAINER_SLOT = 2;
    private static final int INV_SLOT_START = 2;
    private static final int INV_SLOT_END = 29;
    private static final int USE_ROW_SLOT_START = 29;
    private static final int USE_ROW_SLOT_END = 38;
    private static final int PAYMENT_SLOT_X = 163;
    private static final int RESULT_SLOT_X = 221;
    private static final int SLOT_Y = 38;
    private final Merchant trader;
    private final MerchantContainer tradeContainer;
    @Nullable
    private final CannotEntity traderEntity;
    private int hasLostAndFoundData;

    public CannotMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new ClientSideMerchant(playerInventory.player), -1);
    }

    public CannotMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new ClientSideMerchant(playerInventory.player), extraData.readVarInt());
        // 商品随 extraData 下发，构造时即应用，避免首帧商品未渲染
        CompoundTag offersTag = extraData.readNbt();
        if (offersTag != null) {
            this.setOffers(new MerchantOffers(offersTag));
        }
    }

    public CannotMenu(int containerId, Inventory playerInventory, Merchant trader, int traderEntityId) {
        super(BabelMenus.CANNOT.get(), containerId);
        this.trader = trader;
        CannotEntity resolved = trader instanceof CannotEntity cannot ? cannot : null;
        if (resolved == null && playerInventory.player.level().getEntity(traderEntityId) instanceof CannotEntity cannot) {
            resolved = cannot;
        }
        this.traderEntity = resolved;
        this.tradeContainer = new MerchantContainer(trader);
        this.addSlot(new Slot(this.tradeContainer, PAYMENT_SLOT, PAYMENT_SLOT_X, SLOT_Y));
        this.addSlot(new MerchantResultSlot(playerInventory.player, trader, this.tradeContainer, RESULT_CONTAINER_SLOT, RESULT_SLOT_X, SLOT_Y));
        this.addDataSlot(new DataSlot() {
            public int get() {
                return CannotMenu.this.traderEntity != null && CannotMenu.this.traderEntity.hasLostAndFound() ? 1 : 0;
            }

            public void set(int value) {
                CannotMenu.this.hasLostAndFoundData = value;
            }
        });

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 108 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 108 + k * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        this.tradeContainer.updateSellItem();
        super.slotsChanged(container);
    }

    public void setSelectionHint(int index) {
        this.tradeContainer.setSelectionHint(index);
    }

    public boolean hasLostAndFound() {
        return this.hasLostAndFoundData > 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        boolean result;
        if (id == ATTACK_BUTTON_ID) {
            result = this.attackTrader(player);
        } else if (id >= LOST_AND_FOUND_ID) {
            result = this.lostAndFoundExchange(player, id - LOST_AND_FOUND_ID);
        } else {
            this.setSelectionHint(id);
            this.tryMoveItems(id);
            result = true;
        }
        return result;
    }

    // 服务端走玩家真实近战攻击路径（含攻速冷却、附魔、横扫），客户端不执行
    private boolean attackTrader(Player player) {
        boolean attacked = false;
        if (!player.level().isClientSide() && this.traderEntity != null && this.traderEntity.isAlive()
                && player.distanceToSqr(this.traderEntity) <= ATTACK_REACH_SQR) {
            player.attack(this.traderEntity);
            attacked = true;
        }
        return attacked;
    }

    // index 为已激活收藏品列表中的位置
    private boolean lostAndFoundExchange(Player player, int index) {
        boolean exchanged = false;
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer
                && (this.traderEntity == null || this.traderEntity.hasLostAndFound())) {
            Collectibles data = Collectibles.getData(player);
            List<Item> used = new ArrayList<>();
            List<Item> pool = new ArrayList<>();
            for (RegistryObject<Item> entry : Collectibles.Collectibles.values()) {
                Item candidate = entry.get();
                if (data.isUsed(candidate)) {
                    used.add(candidate);
                } else if (!(candidate instanceof CollectibleItem collectible) || collectible.getTier() != CollectibleTiers.CURSED) {
                    pool.add(candidate);
                }
            }
            if (index >= 0 && index < used.size() && !pool.isEmpty()) {
                Item lost = used.get(index);
                Item gained = LostAndFoundEvent.onPre(serverPlayer, this.traderEntity, lost, pool.get(player.getRandom().nextInt(pool.size())));
                if (gained != null) {
                    Collectibles.deactivateAndSync(serverPlayer, lost);
                    Collectibles.activateAndSync(serverPlayer, gained);
                    if (this.traderEntity != null) {
                        this.traderEntity.setHasLostAndFound(false);
                    }
                    LostAndFoundEvent.afterLostAndFound(serverPlayer, this.traderEntity, lost, gained);
                    exchanged = true;
                }
            }
        }
        return exchanged;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.trader.getTradingPlayer() == player;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            boolean moved;
            if (index == RESULT_SLOT) {
                moved = this.moveItemStackTo(itemstack1, INV_SLOT_START, USE_ROW_SLOT_END, true);
                if (moved) {
                    slot.onQuickCraft(itemstack1, itemstack);
                    this.playTradeSound();
                }
            } else if (index >= INV_SLOT_START && index < USE_ROW_SLOT_START) {
                moved = this.moveItemStackTo(itemstack1, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false);
            } else if (index >= USE_ROW_SLOT_START && index < USE_ROW_SLOT_END) {
                moved = this.moveItemStackTo(itemstack1, INV_SLOT_START, INV_SLOT_END, false);
            } else {
                moved = this.moveItemStackTo(itemstack1, INV_SLOT_START, USE_ROW_SLOT_END, false);
            }

            if (moved) {
                if (itemstack1.isEmpty()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }

                if (itemstack1.getCount() != itemstack.getCount()) {
                    slot.onTake(player, itemstack1);
                } else {
                    itemstack = ItemStack.EMPTY;
                }
            } else {
                itemstack = ItemStack.EMPTY;
            }
        }

        return itemstack;
    }

    private void playTradeSound() {
        if (!this.trader.isClientSide()) {
            Entity entity = (Entity) this.trader;
            entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.trader.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.trader.setTradingPlayer(null);
        if (!this.trader.isClientSide()) {
            ItemStack payment = this.tradeContainer.removeItemNoUpdate(PAYMENT_SLOT);
            if (!payment.isEmpty()) {
                if (!player.isAlive() || player instanceof ServerPlayer serverPlayer && serverPlayer.hasDisconnected()) {
                    player.drop(payment, false);
                } else if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.getInventory().placeItemBackInInventory(payment);
                }
            }
        }
    }

    public void tryMoveItems(int index) {
        if (index >= 0 && this.getOffers().size() > index) {
            ItemStack payment = this.tradeContainer.getItem(PAYMENT_SLOT);
            if (payment.isEmpty() || this.moveItemStackTo(payment, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                if (!payment.isEmpty()) {
                    this.tradeContainer.setItem(PAYMENT_SLOT, payment);
                }

                if (this.tradeContainer.getItem(PAYMENT_SLOT).isEmpty()) {
                    this.moveFromInventoryToPaymentSlot(PAYMENT_SLOT, this.getOffers().get(index).getCostA());
                }
            }
        }
    }

    private void moveFromInventoryToPaymentSlot(int paymentSlot, ItemStack cost) {
        if (!cost.isEmpty()) {
            for (int i = INV_SLOT_START; i < USE_ROW_SLOT_END; ++i) {
                ItemStack itemstack = this.slots.get(i).getItem();
                if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(cost, itemstack)) {
                    ItemStack itemstack1 = this.tradeContainer.getItem(paymentSlot);
                    int j = itemstack1.isEmpty() ? 0 : itemstack1.getCount();
                    int k = Math.min(cost.getMaxStackSize() - j, itemstack.getCount());
                    ItemStack itemstack2 = itemstack.copy();
                    int l = j + k;
                    itemstack.shrink(k);
                    itemstack2.setCount(l);
                    this.tradeContainer.setItem(paymentSlot, itemstack2);
                    if (l >= cost.getMaxStackSize()) {
                        break;
                    }
                }
            }
        }
    }

    public void setOffers(MerchantOffers offers) {
        this.trader.overrideOffers(offers);
    }

    public MerchantOffers getOffers() {
        return this.trader.getOffers();
    }

    @Nullable
    public CannotEntity getTraderEntity() {
        return this.traderEntity;
    }
}