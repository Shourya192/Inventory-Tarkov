package com.tarkovinventory.client.screen;

import com.tarkovinventory.client.screen.modules.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TarkovInventoryScreen extends Screen {

    private final EquipmentPanelRenderer equipment = new EquipmentPanelRenderer();
    private final InventoryGridRenderer grid = new InventoryGridRenderer();
    private final LootPanelRenderer loot = new LootPanelRenderer();
    private final VicinityRenderer vicinity = new VicinityRenderer();
    private final DragState dragState = new DragState();

    private int leftX;
    private int centerX;
    private int rightX;
    private int topY;

    public TarkovInventoryScreen() {
        super(Component.literal("Tarkov Inventory"));
    }

    @Override
    protected void init() {
        super.init();

        // REAL measured widths (matches your actual UI sizes)
        int equipmentW = 170;
        int gridW = 220;
        int rightW = 320;

        int spacing = 40; // increased to prevent “too close / left feel”

        int totalW = equipmentW + gridW + rightW + spacing * 2;

        int startX = (this.width - totalW) / 2;
        this.topY = (this.height - 220) / 2;

        // PERFECT anchors
        this.leftX = startX;
        this.centerX = startX + equipmentW + spacing;
        this.rightX = startX + equipmentW + gridW + spacing * 2;

        equipment.init(leftX, topY);
        grid.init(centerX, topY);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        renderBackground(g);

        // LEFT
        equipment.renderWithBackground(g);

        // CENTER (IMPORTANT: now centered properly)
        grid.renderWithBackground(g);

        // RIGHT
        loot.render(g, rightX, topY);
        vicinity.render(g, rightX, topY + 240);

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
    public boolean isPauseScreen() {
        return false;
    }
}
