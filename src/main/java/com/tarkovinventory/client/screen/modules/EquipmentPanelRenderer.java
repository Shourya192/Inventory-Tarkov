package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Equipment panel (clean Tarkov-style blocks).
 */
public class EquipmentPanelRenderer {

    private int left;
    private int top;

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    public void render(GuiGraphics g) {

        int x = left;
        int y = top;

        // PANEL BACKGROUND
        g.fill(x, y, x + 180, y + 180, 0xFF141414);

        // HEADER
        g.fill(x, y, x + 180, y + 18, 0xFF1C1C1C);

        // SLOT BLOCKS
        g.fill(x + 10, y + 25, x + 50, y + 55, 0xFF222222);  // head
        g.fill(x + 60, y + 25, x + 160, y + 55, 0xFF222222); // chest

        g.fill(x + 10, y + 65, x + 50, y + 95, 0xFF222222);  // legs
        g.fill(x + 60, y + 65, x + 160, y + 95, 0xFF222222); // rig

        g.fill(x + 10, y + 105, x + 160, y + 140, 0xFF222222);
    }
}
