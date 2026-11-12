package com.susen36.babel.client.collectible;

import com.susen36.babel.BabelMod;
import com.susen36.babel.client.gui.CannotScreen;
import com.susen36.babel.collectible.Collectibles;
import com.susen36.babel.menu.CannotMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CollectiblesScreen extends Screen {
    private static final int PANEL_WIDTH = 312;
    private static final int PANEL_HEIGHT = 192;
    private static final int SLOT_ORIGIN_X = 4;
    private static final int SLOT_ORIGIN_Y = 4;
    private static final int SLOT_SIZE = 16;
    private static final int SLOT_STEP = 24;
    private static final int COLS_PER_PAGE = 13;
    private static final int ROWS_PER_PAGE = 8;
    private static final int PAGE_SIZE = COLS_PER_PAGE * ROWS_PER_PAGE;
    private static final int DRAG_THRESHOLD = 40;
    private static final ResourceLocation BACKGROUND = new ResourceLocation(BabelMod.MODID, "textures/gui/screen/relic_bg.png");
    private static final Component LOST_AND_FOUND_HINT = Component.translatable("gui.babel.collectibles.lost_and_found_hint");

    private final List<ItemStack> entries = new ArrayList<>();
    // >=0 表示失与得模式
    private final int lostAndFoundContainerId;
    private int leftPos;
    private int topPos;
    private int currentPage;
    private int totalPages = 1;
    private @Nullable Button prevButton;
    private @Nullable Button nextButton;
    private int dragAccum;
    private int lastMouseX;

    public CollectiblesScreen(Player player) {
        this(player, -1);
    }

    public CollectiblesScreen(Player player, int lostAndFoundContainerId) {
        super(Component.translatable("gui.babel.collectibles.title"));
        this.lostAndFoundContainerId = lostAndFoundContainerId;
        this.refreshEntries(player);
    }

    private void refreshEntries(Player player) {
        this.entries.clear();
        var used = Collectibles.getData(player);
        for (var entry : Collectibles.Collectibles.values()) {
            if (used != null && used.isUsed(entry.get())) {
                ItemStack stack = new ItemStack(entry.get());
                stack.setCount(Math.max(1, Collectibles.getLayerData(player).getLayer(entry.get())));
                this.entries.add(stack);
            }
        }
        this.totalPages = Math.max(1, (this.entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        this.setPage(this.currentPage);
    }

    @Override
    public void tick() {
        if (this.lostAndFoundContainerId >= 0 && this.minecraft != null && this.minecraft.player != null) {
            this.refreshEntries(this.minecraft.player);
        }
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - PANEL_WIDTH) / 2;
        this.topPos = (this.height - PANEL_HEIGHT) / 2 + 2;
        this.prevButton = new PlainTextButton(this.leftPos + 4, this.topPos + PANEL_HEIGHT + 6, 16, 16,
                Component.literal("◀"), e -> this.setPage(this.currentPage - 1), this.font);
        this.addRenderableWidget(this.prevButton);
        this.nextButton = new PlainTextButton(this.leftPos + 24, this.topPos + PANEL_HEIGHT + 6, 16, 16,
                Component.literal("▶"), e -> this.setPage(this.currentPage + 1), this.font);
        this.addRenderableWidget(this.nextButton);
        this.updatePageButtons();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderBackground(guiGraphics);
        guiGraphics.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT);
        int pageStart = this.currentPage * PAGE_SIZE;
        int pageEnd = Math.min(this.entries.size(), pageStart + PAGE_SIZE);
        int hovered = -1;
        for (int i = pageStart; i < pageEnd; i++) {
            int within = i - pageStart;
            int bx = this.leftPos + SLOT_ORIGIN_X + within % COLS_PER_PAGE * SLOT_STEP;
            int by = this.topPos + SLOT_ORIGIN_Y + within / COLS_PER_PAGE * SLOT_STEP;
            ItemStack stack = this.entries.get(i);
            guiGraphics.renderItem(stack, bx, by);
            guiGraphics.renderItemDecorations(this.font, stack, bx, by);
            if (mouseX >= bx && mouseX < bx + SLOT_SIZE && mouseY >= by && mouseY < by + SLOT_SIZE) {
                hovered = i;
                guiGraphics.fill(bx, by, bx + SLOT_SIZE, by + SLOT_SIZE, 0x60FFFFFF);
            }
        }
        guiGraphics.drawString(this.font, this.title.copy().append(": ").append(String.valueOf(this.entries.size())), this.leftPos + 4, this.topPos - 12, -1, false);
        guiGraphics.drawString(this.font, (this.currentPage + 1) + "/" + this.totalPages, this.leftPos + PANEL_WIDTH - 30, this.topPos - 12, -1, false);
        if (this.lostAndFoundContainerId >= 0) {
            guiGraphics.drawString(this.font, LOST_AND_FOUND_HINT, this.leftPos + 60, this.topPos - 12, -1, false);
        }
        if (hovered != -1) {
            String descriptionId = this.entries.get(hovered).getItem().getDescriptionId();
            String name = Component.translatable(descriptionId).getString();
            Component desc = Component.translatable(descriptionId + ".description_0").withStyle(ChatFormatting.AQUA);
            guiGraphics.renderTooltip(this.font, Component.literal(name + ": ").append(desc), mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean result;
        if (keyCode == 256 && this.lostAndFoundContainerId >= 0) {
            this.returnToCannotScreen();
            result = true;
        } else if (keyCode == 263 && this.currentPage > 0) {
            this.setPage(this.currentPage - 1);
            result = true;
        } else if (keyCode == 262 && this.currentPage < this.totalPages - 1) {
            this.setPage(this.currentPage + 1);
            result = true;
        } else {
            result = super.keyPressed(keyCode, scanCode, modifiers);
        }
        return result;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta < 0 && this.currentPage < this.totalPages - 1) {
            this.setPage(this.currentPage + 1);
            return true;
        }
        if (delta > 0 && this.currentPage > 0) {
            this.setPage(this.currentPage - 1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.dragAccum = 0;
            this.lastMouseX = (int) mouseX;
        }
        boolean result;
        if (button == 0 && this.lostAndFoundContainerId >= 0 && this.clickLostAndFoundSlot(mouseX, mouseY)) {
            result = true;
        } else {
            result = super.mouseClicked(mouseX, mouseY, button);
        }
        return result;
    }

    private boolean clickLostAndFoundSlot(double mouseX, double mouseY) {
        boolean clicked = false;
        int pageStart = this.currentPage * PAGE_SIZE;
        int pageEnd = Math.min(this.entries.size(), pageStart + PAGE_SIZE);
        for (int i = pageStart; i < pageEnd && !clicked; i++) {
            int within = i - pageStart;
            int bx = this.leftPos + SLOT_ORIGIN_X + within % COLS_PER_PAGE * SLOT_STEP;
            int by = this.topPos + SLOT_ORIGIN_Y + within / COLS_PER_PAGE * SLOT_STEP;
            if (mouseX >= bx && mouseX < bx + SLOT_SIZE && mouseY >= by && mouseY < by + SLOT_SIZE) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.lostAndFoundContainerId, CannotMenu.LOST_AND_FOUND_ID + i);
                this.returnToCannotScreen();
                clicked = true;
            }
        }
        return clicked;
    }

    private void returnToCannotScreen() {
        if (this.minecraft != null && this.minecraft.player != null
                && this.minecraft.player.containerMenu instanceof CannotMenu cannotMenu
                && cannotMenu.getTraderEntity() != null) {
            this.minecraft.setScreen(new CannotScreen(cannotMenu, this.minecraft.player.getInventory(), cannotMenu.getTraderEntity().getDisplayName()));
        }
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            this.dragAccum += (int) mouseX - this.lastMouseX;
            this.lastMouseX = (int) mouseX;
            if (this.dragAccum <= -DRAG_THRESHOLD && this.currentPage < this.totalPages - 1) {
                this.setPage(this.currentPage + 1);
                this.dragAccum = 0;
            } else if (this.dragAccum >= DRAG_THRESHOLD && this.currentPage > 0) {
                this.setPage(this.currentPage - 1);
                this.dragAccum = 0;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void setPage(int page) {
        this.currentPage = Mth.clamp(page, 0, this.totalPages - 1);
        this.updatePageButtons();
    }

    private void updatePageButtons() {
        if (this.prevButton != null) {
            this.prevButton.active = this.currentPage > 0;
        }
        if (this.nextButton != null) {
            this.nextButton.active = this.currentPage < this.totalPages - 1;
        }
    }
}