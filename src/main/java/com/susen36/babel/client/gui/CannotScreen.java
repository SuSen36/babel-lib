package com.susen36.babel.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.susen36.babel.BabelMod;
import com.susen36.babel.entity.CannotEntity;
import com.susen36.babel.menu.CannotMenu;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CannotScreen extends AbstractContainerScreen<CannotMenu> {
    private static final ResourceLocation GUI_LOCATION = new ResourceLocation(BabelMod.MODID, "textures/gui/container/cannot.png");
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int TRADE_BUTTON_X = 5;
    private static final int TRADE_BUTTON_WIDTH = 88;
    private static final int TRADE_BUTTON_HEIGHT = 20;
    private static final int SCROLL_BAR_TOP_POS_Y = 18;
    private static final int SCROLL_BAR_HEIGHT = 139;
    private static final int SCROLL_BAR_START_X = 94;
    private static final int NUMBER_OF_OFFER_BUTTONS = 7;
    private static final int LABEL_Y = 6;
    private static final int LABEL_COLOR = 0xE9FFFC;
    private static final Component TRADES_LABEL = Component.translatable("merchant.trades");
    private static final int ENTITY_ANCHOR_X = 131;
    private static final int ENTITY_ANCHOR_Y = 77;
    private static final int ENTITY_SCALE = 25;
    private int shopItem;
    private final CannotScreen.TradeOfferButton[] tradeOfferButtons = new CannotScreen.TradeOfferButton[NUMBER_OF_OFFER_BUTTONS];
    int scrollOff;
    private boolean isDragging;
    private float xMouse;
    private float yMouse;

    public CannotScreen(CannotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 276;
    }

    private void postButtonClick() {
        this.menu.setSelectionHint(this.shopItem);
        this.menu.tryMoveItems(this.shopItem);
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, this.shopItem);
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int k = j + 16 + 2;

        for (int l = 0; l < NUMBER_OF_OFFER_BUTTONS; ++l) {
            this.tradeOfferButtons[l] = this.addRenderableWidget(new CannotScreen.TradeOfferButton(i + TRADE_BUTTON_X, k, l, (p_99174_) -> {
                if (p_99174_ instanceof CannotScreen.TradeOfferButton) {
                    this.shopItem = ((CannotScreen.TradeOfferButton) p_99174_).getIndex() + this.scrollOff;
                    this.postButtonClick();
                }

            }));
            k += 20;
        }

    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 49 + this.imageWidth / 2 - this.font.width(this.title) / 2, LABEL_Y, LABEL_COLOR, false);
        int i = this.font.width(TRADES_LABEL);
        guiGraphics.drawString(this.font, TRADES_LABEL, TRADE_BUTTON_X - i / 2 + 48, LABEL_Y, LABEL_COLOR, false);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(GUI_LOCATION, i, j, 0, 0.0F, 0.0F, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        CannotEntity trader = this.menu.getTraderEntity();
        if (trader != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, this.leftPos + ENTITY_ANCHOR_X, this.topPos + ENTITY_ANCHOR_Y, ENTITY_SCALE, (float) (this.leftPos + ENTITY_ANCHOR_X) - this.xMouse, (float) (this.topPos + 75 - 50) - this.yMouse, trader);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.xMouse = (float) mouseX;
        this.yMouse = (float) mouseY;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        MerchantOffers merchantoffers = this.menu.getOffers();
        if (!merchantoffers.isEmpty()) {
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            int k = j + 16 + 1;
            int l = i + TRADE_BUTTON_X + 5;
            this.renderScroller(guiGraphics, i, j, merchantoffers);
            int i1 = 0;

            for (MerchantOffer merchantoffer : merchantoffers) {
                if (!this.canScroll(merchantoffers.size()) || i1 >= this.scrollOff && i1 < NUMBER_OF_OFFER_BUTTONS + this.scrollOff) {
                    ItemStack itemstack = merchantoffer.getBaseCostA();
                    ItemStack itemstack1 = merchantoffer.getCostA();
                    ItemStack itemstack2 = merchantoffer.getCostB();
                    ItemStack itemstack3 = merchantoffer.getResult();
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
                    int j1 = k + 2;
                    this.renderAndDecorateCostA(guiGraphics, itemstack1, itemstack, l, j1);
                    if (!itemstack2.isEmpty()) {
                        guiGraphics.renderFakeItem(itemstack2, i + TRADE_BUTTON_X + 35, j1);
                        guiGraphics.renderItemDecorations(this.font, itemstack2, i + TRADE_BUTTON_X + 35, j1);
                    }

                    this.renderButtonArrows(guiGraphics, merchantoffer, i, j1);
                    guiGraphics.renderFakeItem(itemstack3, i + TRADE_BUTTON_X + 68, j1);
                    guiGraphics.renderItemDecorations(this.font, itemstack3, i + TRADE_BUTTON_X + 68, j1);
                    guiGraphics.pose().popPose();
                    k += 20;
                    ++i1;
                } else {
                    ++i1;
                }
            }

            for (CannotScreen.TradeOfferButton cannotScreen$tradeofferbutton : this.tradeOfferButtons) {
                if (cannotScreen$tradeofferbutton.isHoveredOrFocused()) {
                    cannotScreen$tradeofferbutton.renderToolTip(guiGraphics, mouseX, mouseY);
                }

                cannotScreen$tradeofferbutton.visible = cannotScreen$tradeofferbutton.index < this.menu.getOffers().size();
            }

            RenderSystem.enableDepthTest();
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderButtonArrows(GuiGraphics guiGraphics, MerchantOffer offer, int x, int y) {
        RenderSystem.enableBlend();
        if (offer.isOutOfStock()) {
            guiGraphics.blit(GUI_LOCATION, x + TRADE_BUTTON_X + 35 + 20, y + 3, 0, 25.0F, 171.0F, 10, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        } else {
            guiGraphics.blit(GUI_LOCATION, x + TRADE_BUTTON_X + 35 + 20, y + 3, 0, 15.0F, 171.0F, 10, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

    }

    private void renderScroller(GuiGraphics guiGraphics, int x, int y, MerchantOffers offers) {
        int i = offers.size() + 1 - NUMBER_OF_OFFER_BUTTONS;
        if (i > 1) {
            int j = SCROLL_BAR_HEIGHT - (27 + (i - 1) * SCROLL_BAR_HEIGHT / i);
            int k = 1 + j / i + SCROLL_BAR_HEIGHT / i;
            int l = 113;
            int i1 = Math.min(113, this.scrollOff * k);
            if (this.scrollOff == i - 1) {
                i1 = 113;
            }

            guiGraphics.blit(GUI_LOCATION, x + SCROLL_BAR_START_X, y + SCROLL_BAR_TOP_POS_Y + i1, 0, 0.0F, 199.0F, 6, 27, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        } else {
            guiGraphics.blit(GUI_LOCATION, x + SCROLL_BAR_START_X, y + SCROLL_BAR_TOP_POS_Y, 0, 6.0F, 199.0F, 6, 27, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

    }

    private void renderAndDecorateCostA(GuiGraphics guiGraphics, ItemStack cost, ItemStack baseCost, int x, int y) {
        guiGraphics.renderFakeItem(cost, x, y);
        if (baseCost.getCount() == cost.getCount()) {
            guiGraphics.renderItemDecorations(this.font, cost, x, y);
        } else {
            guiGraphics.renderItemDecorations(this.font, baseCost, x, y, baseCost.getCount() == 1 ? "1" : null);
            // Forge: fixes Forge-8806, code for count rendering taken from GuiGraphics#renderGuiItemDecorations
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
            String count = cost.getCount() == 1 ? "1" : String.valueOf(cost.getCount());
            font.drawInBatch(count, (float) (x + 14) + 19 - 2 - font.width(count), (float) y + 6 + 3, 0xFFFFFF, true, guiGraphics.pose().last().pose(), guiGraphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880, false);
            guiGraphics.pose().popPose();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 300.0F);
            guiGraphics.blit(GUI_LOCATION, x + 7, y + 12, 0, 0.0F, 176.0F, 9, 2, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            guiGraphics.pose().popPose();
        }

    }

    private boolean canScroll(int size) {
        return size > NUMBER_OF_OFFER_BUTTONS;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        int i = this.menu.getOffers().size();
        if (this.canScroll(i)) {
            int j = i - NUMBER_OF_OFFER_BUTTONS;
            this.scrollOff = Mth.clamp((int) ((double) this.scrollOff - delta), 0, j);
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
        int i = this.menu.getOffers().size();
        if (this.isDragging) {
            int j = this.topPos + SCROLL_BAR_TOP_POS_Y;
            int k = j + SCROLL_BAR_HEIGHT;
            int l = i - NUMBER_OF_OFFER_BUTTONS;
            float f = ((float) y - (float) j - 13.5F) / ((float) (k - j) - 27.0F);
            f = f * (float) l + 0.5F;
            this.scrollOff = Mth.clamp((int) f, 0, l);
            return true;
        } else {
            return super.mouseDragged(x, y, button, dragX, dragY);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        this.isDragging = false;
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        if (this.canScroll(this.menu.getOffers().size()) && x > (double) (i + SCROLL_BAR_START_X) && x < (double) (i + SCROLL_BAR_START_X + 6) && y > (double) (j + SCROLL_BAR_TOP_POS_Y) && y <= (double) (j + SCROLL_BAR_TOP_POS_Y + SCROLL_BAR_HEIGHT + 1)) {
            this.isDragging = true;
        }

        return super.mouseClicked(x, y, button);
    }

    @OnlyIn(Dist.CLIENT)
    class TradeOfferButton extends Button {
        final int index;

        public TradeOfferButton(int x, int y, int index, Button.OnPress onPress) {
            super(x, y, TRADE_BUTTON_WIDTH, TRADE_BUTTON_HEIGHT, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void renderToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            if (this.isHovered && CannotScreen.this.menu.getOffers().size() > this.index + CannotScreen.this.scrollOff) {
                if (mouseX < this.getX() + 20) {
                    ItemStack itemstack = CannotScreen.this.menu.getOffers().get(this.index + CannotScreen.this.scrollOff).getCostA();
                    guiGraphics.renderTooltip(CannotScreen.this.font, itemstack, mouseX, mouseY);
                } else if (mouseX < this.getX() + 50 && mouseX > this.getX() + 30) {
                    ItemStack itemstack2 = CannotScreen.this.menu.getOffers().get(this.index + CannotScreen.this.scrollOff).getCostB();
                    if (!itemstack2.isEmpty()) {
                        guiGraphics.renderTooltip(CannotScreen.this.font, itemstack2, mouseX, mouseY);
                    }
                } else if (mouseX > this.getX() + 65) {
                    ItemStack itemstack1 = CannotScreen.this.menu.getOffers().get(this.index + CannotScreen.this.scrollOff).getResult();
                    guiGraphics.renderTooltip(CannotScreen.this.font, itemstack1, mouseX, mouseY);
                }
            }

        }
    }
}