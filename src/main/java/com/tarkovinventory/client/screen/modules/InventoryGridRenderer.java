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

        int startX = left + 200;
        int startY = top + 70;

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                int px = startX + x * CELL;
                int py = startY + y * CELL;
                g.fill(px, py, px + CELL, py + CELL, 0xFF252525);
            }
        }
    }

    public int getSlotAt(double mouseX, double mouseY) {
        int startX = left + 200;
        int startY = top + 70;

        int col = (int)((mouseX - startX) / CELL);
        int row = (int)((mouseY - startY) / CELL);

        if (col < 0 || row < 0 || col >= cols || row >= rows) return -1;

        return col + row * cols;
    }

    public void mouseClicked(double mx, double my, int btn, int x, int y) {}
    public void mouseReleased(double mx, double my, int btn) {}
    public void mouseDragged(double mx, double my, int btn, double dx, double dy, Object dragState) {}
}
