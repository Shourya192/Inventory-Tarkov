package com.tarkovinventory.client.screen.modules;

import com.tarkovinventory.client.screen.layout.Panel;
import net.minecraft.client.gui.GuiGraphics;

public class InventoryGridRenderer {

    private static final int CELL = 17;
    private int cols = 5;
    private int rows = 5;

    public void render(GuiGraphics g, Panel p) {

        int startX = p.x + 10;
        int startY = p.y + 25;

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {

                int px = startX + x * CELL;
                int py = startY + y * CELL;

                g.fill(px, py, px + CELL - 1, py + CELL - 1, 0xFF2A2A2A);
            }
        }
    }
}
