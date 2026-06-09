package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class VicinityRenderer {

    public void render(GuiGraphics g, int left, int top) {

        int x = left + 450;
        int y = top + 10;

        g.fill(x, y, x + 140, y + 200, 0xFF101010);

        for (int i = 0; i < 10; i++) {
            g.fill(x + 5, y + 5 + i * 18, x + 135, y + 20 + i * 18, 0xFF1A1A1A);
        }
    }
}
