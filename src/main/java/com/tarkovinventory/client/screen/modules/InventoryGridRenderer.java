package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class InventoryGridRenderer {

    private static final int CELL = 17;

    private int left;
    private int top;

    private int cols = 5;
    private int rows = 5;

    // grid anchor offset INSIDE screen (not absolute magic numbers)
    private static final int OFFSET_X = 210;
    private static final int OFFSET_Y = 70;

    public void setSize(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
    }

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    private int startX() {
        return left + OFFSET_X;
    }

    private int startY() {
        return top + OFFSET_Y;
    }

    public void render(GuiGraphics g, int screenX, int screenY, int mouseX, int mouseY) {

        int sx = startX();
        int sy = startY();

        // background frame
        g.fill(
                sx - 4,
                sy - 4,
                sx + cols * CELL + 4,
                sy + rows * CELL + 4,
                0xFF141414
        );

        // grid cells
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {

                int px = sx + x * CELL;
                int py = sy + y * CELL;

                g.fill(px, py, px + CELL, py + CELL, 0xFF252525);

                // subtle grid lines
                g.fill(px, py, px + CELL, py + 1, 0xFF333333);
                g.fill(px, py, px + 1, py + CELL, 0xFF333333);
            }
        }
    }

    public int getSlotAt(double mouseX, double mouseY) {

        int sx = startX();
        int sy = startY();

        int col = (int)((mouseX - sx) / CELL);
        int row = (int)((mouseY - sy) / CELL);

        if (col < 0 || row < 0 || col >= cols || row >= rows) {
            return -1;
        }

        return col + row * cols;
    }

    public boolean isInside(double mouseX, double mouseY) {

        int sx = startX();
        int sy = startY();

        return mouseX >= sx &&
               mouseX < sx + cols * CELL &&
               mouseY >= sy &&
               mouseY < sy + rows * CELL;
    }
}
