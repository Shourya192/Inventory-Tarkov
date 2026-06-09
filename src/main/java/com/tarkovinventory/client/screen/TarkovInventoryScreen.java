package com.tarkovinventory.client.screen;

import com.tarkovinventory.client.screen.modules.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * REAL TARKOV-STYLE 3 COLUMN UI LAYOUT
 * LEFT: Equipment
 * CENTER: Backpack Grid
 * RIGHT: Loot + Vicinity
 */
public class TarkovInventoryScreen extends Screen {

    private final EquipmentPanelRenderer equipment = new EquipmentPanelRenderer();
    private final InventoryGridRenderer grid = new InventoryGridRenderer();
    private final LootPanelRenderer loot = new LootPanelRenderer();
    private final VicinityRenderer vicinity = new VicinityRenderer();
    private final DragState dragState = new DragState();

    // layout cached
    private int baseX;
    private int baseY;

    public TarkovInventoryScreen() {
        super(Component.literal("Tarkov Inventory"));
    }

    @Override
    protected void init() {
        super.init();

        int spacing = 20;

        int leftW = 180;
        int centerW = 200;
        int rightW = 320;

        int totalW = leftW + centerW + rightW + spacing * 2;

        this.baseX = (this.width - totalW) / 2;
        this.baseY = (this.height - 220) / 2;

        // LEFT PANEL
        equipment.init(baseX, baseY);

        // CENTER PANEL
        grid.init(baseX + leftW + spacing, baseY);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        renderBackground(g);

        int spacing = 20;

        int leftW = 180;
        int centerW = 200;

        int rightX = baseX + leftW + centerW + spacing * 2;

        // LEFT → Equipment
        equipment.render(g);

        // CENTER → Backpack grid
        grid.render(g);

        // RIGHT TOP → Loot
        loot.render(g, rightX, baseY);

        // RIGHT BOTTOM → Vicinity
        vicinity.render(g, rightX, baseY + 240);

        // Drag preview
        if (dragState.isDragging()) {
            ItemStack stack = dragState.getDragging();
            g.renderItem(stack, mouseX - 8, mouseY - 8);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics g) {
        g.fill(0, 0, width, height, 0xAA000000);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        int slot = grid.getSlotAt(mouseX, mouseY);

        if (slot >= 0) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
