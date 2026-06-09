package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class InventoryGridRenderer {

    private static final int CELL = 17;

    private int left;
    private int top;

    private int cols = 5;
    private int rows = 5;

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    // 🔥 BACKPACK PANEL WITH BACKGROUND (IMPORTANT)
    public void renderWithBackground(GuiGraphics g) {

        int w = cols * CELL;
        int h = rows * CELL;

        // main panel background (this is what was missing)
        g.fill(left - 10, top - 10, left + w + 10, top + h + 10, 0xFF0B0B0B);

        // inner border shade (gives Tarkov depth feel)
        g.fill(left - 8, top - 8, left + w + 8, top + h + 8, 0xFF151515);

        render(g);
    }

    public void render(GuiGraphics g) {

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {

                int px = left + x * CELL;
                int py = top + y * CELL;

                g.fill(px, py, px + CELL, py + CELL, 0xFF2F2F2F);
            }
        }
    }

    public int getSlotAt(double mouseX, double mouseY) {

        int col = (int)((mouseX - left) / CELL);
        int row = (int)((mouseY - top) / CELL);

        if (col < 0 || row < 0 || col >= cols || row >= rows) return -1;

        return col + row * cols;
    }
}
