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

        int spacing = 20;

        int leftW = 180;
        int centerW = 200;
        int rightW = 320;

        int totalW = leftW + centerW + rightW + spacing * 2;

        int startX = (this.width - totalW) / 2;
        this.topY = (this.height - 220) / 2;

        this.leftX = startX;
        this.centerX = startX + leftW + spacing;
        this.rightX = startX + leftW + centerW + spacing * 2;

        equipment.init(leftX, topY);
        grid.init(centerX, topY);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        renderBackground(g);

        equipment.render(g);
        grid.render(g);

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
