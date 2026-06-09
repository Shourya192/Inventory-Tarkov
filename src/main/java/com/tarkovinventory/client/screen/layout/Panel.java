package com.tarkovinventory.client.screen.layout;

import net.minecraft.client.gui.GuiGraphics;

public class Panel {

    public final int x, y, w, h;

    public Panel(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public void draw(GuiGraphics g) {

        // main panel background
        g.fill(x, y, x + w, y + h, 0xFF151515);

        // header bar
        g.fill(x, y, x + w, y + 18, 0xFF1C1C1C);

        // border
        g.fill(x, y, x + w, y + 1, 0xFF2A2A2A);
        g.fill(x, y + h - 1, x + w, y + h, 0xFF2A2A2A);
        g.fill(x, y, x + 1, y + h, 0xFF2A2A2A);
        g.fill(x + w - 1, y, x + w, y + h, 0xFF2A2A2A);
    }
}
