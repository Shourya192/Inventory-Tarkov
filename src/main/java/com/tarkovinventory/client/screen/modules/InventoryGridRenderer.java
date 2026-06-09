package com.tarkovinventory.client.screen.modules;

import com.tarkovinventory.client.screen.layout.Panel;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Backpack grid renderer (panel-based).
 */
public class InventoryGridRenderer {

    private static final int CELL = 17;
    private int cols = 5;
    private int rows = 5;

    public void setSize(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
    }

    public void render(GuiGraphics g, Panel p) {

        int startX = p.x + 8;
        int startY = p.y + 25;

        // grid background
        g.fill(p.x, p.y, p.x + p.w, p.y + p.h, 0xFF1A1A1A);

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {

                int px = startX + x * CELL;
                int py = startY + y * CELL;

                g.fill(px, py, px + CELL - 1, py + CELL - 1, 0xFF2A2A2A);
            }
        }
    }
}
