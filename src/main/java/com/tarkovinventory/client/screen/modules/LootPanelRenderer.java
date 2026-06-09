package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class LootPanelRenderer {

    public void render(GuiGraphics g, int left, int top) {

        int x = left;
        int y = top;

        g.fill(x, y, x + 160, y + 240, 0xFF141414);

        g.fill(x, y, x + 160, y + 18, 0xFF1C1C1C);

        for (int i = 0; i < 12; i++) {
            int rowY = y + 22 + i * 16;

            g.fill(x + 5, rowY,
                   x + 155, rowY + 14,
                   0xFF222222);
        }
    }
}
