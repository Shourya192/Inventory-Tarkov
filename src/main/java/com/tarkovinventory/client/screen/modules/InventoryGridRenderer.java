package com.tarkovinventory.client.screen.modules;

import com.tarkovinventory.client.screen.layout.Panel;
import net.minecraft.client.gui.GuiGraphics;

public class InventoryGridRenderer {

    private static final int CELL = 17;

    public void render(GuiGraphics g, Panel p) {

        int startX = p.x + 10;
        int startY = p.y + 25;

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {

                int px = startX + x * CELL;
                int py = startY + y * CELL;

                g.fill(px, py, px + CELL - 1, py + CELL - 1, 0xFF262626);
            }
        }
    }
}
