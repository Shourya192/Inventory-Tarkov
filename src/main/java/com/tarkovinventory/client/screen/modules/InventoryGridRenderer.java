package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class InventoryGridRenderer implements IModule {

    private static final int CELL = 17;

    private int left;
    private int top;
    private int cols = 5;
    private int rows = 5;

    public void setSize(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
    }

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    @Override
    public void render(GuiGraphics g, int left, int top, int mouseX, int mouseY) {

        int startX = this.left + 200;
        int startY = this.top + 70;

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {

                int px = startX + x * CELL;
                int py = startY + y * CELL;

                g.fill(px, py, px + CELL, py + CELL, 0xFF252525);
            }
        }
    }
}
