package com.susen36.babel.client.collectible;

import com.susen36.babel.BabelMod;
import com.susen36.babel.collectible.Collectibles;
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

    private final List<ItemStack> entries = new ArrayList<>();
    private int leftPos;
    private int topPos;
    private int currentPage;
    private int totalPages = 1;
    private @Nullable Button prevButton;
    private @Nullable Button nextButton;
    private int dragAccum;
    private int lastMouseX;

    public CollectiblesScreen(Player player) {
        super(Component.translatable("gui.babel.collectibles.title"));
        var used = Collectibles.getData(player);
        for (var entry : Collectibles.Collectibles.values()) {
            if (used.isUsed(entry.get())) {
                this.entries.add(new ItemStack(entry.get()));
            }
        }
        this.totalPages = Math.max(1, (this.entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
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
        guiGraphics.drawString(this.font, this.title, this.leftPos + 4, this.topPos - 12, -1, false);
        guiGraphics.drawString(this.font, (this.currentPage + 1) + "/" + this.totalPages, this.leftPos + PANEL_WIDTH - 30, this.topPos - 12, -1, false);
        if (hovered != -1) {
            String descriptionId = this.entries.get(hovered).getItem().getDescriptionId();
            String name = Component.translatable(descriptionId).getString();
            String desc = Component.translatable(descriptionId + ".description_0").getString();
            guiGraphics.renderTooltip(this.font, Component.literal(name + ": " + desc), mouseX, mouseY);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 263 && this.currentPage > 0) {
            this.setPage(this.currentPage - 1);
            return true;
        }
        if (keyCode == 262 && this.currentPage < this.totalPages - 1) {
            this.setPage(this.currentPage + 1);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
        return super.mouseClicked(mouseX, mouseY, button);
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
