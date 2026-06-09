package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class InventoryGridRenderer {

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

    public void render(GuiGraphics g) {

        int startX = left;
        int startY = top;

        // BACKGROUND
        g.fill(startX - 6, startY - 22,
               startX + cols * CELL + 6,
               startY + rows * CELL + 6,
               0xFF141414);

        // HEADER
        g.fill(startX - 6, startY - 38,
               startX + cols * CELL + 6,
               startY - 22,
               0xFF1C1C1C);

        // GRID CELLS
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {

                int px = startX + x * CELL;
                int py = startY + y * CELL;

                g.fill(px, py, px + CELL - 1, py + CELL - 1, 0xFF222222);
            }
        }
    }

    public int getSlotAt(double mouseX, double mouseY) {

        int startX = left;
        int startY = top;

        int col = (int)((mouseX - startX) / CELL);
        int row = (int)((mouseY - startY) / CELL);

        if (col < 0 || row < 0 || col >= cols || row >= rows) return -1;

        return col + row * cols;
    }
}
