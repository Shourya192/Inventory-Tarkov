package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

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

        g.fill(x, y, x + 26, y + 26, 0xFF1A1A1A);
        g.fill(x + 40, y, x + 70, y + 30, 0xFF1A1A1A);
        g.fill(x + 90, y, x + 116, y + 26, 0xFF1A1A1A);
        g.fill(x + 130, y, x + 156, y + 26, 0xFF1A1A1A);

        g.fill(x, y + 50, x + 40, y + 90, 0xFF1A1A1A);
        g.fill(x + 60, y + 50, x + 100, y + 90, 0xFF1A1A1A);

        g.fill(x, y + 110, x + 40, y + 150, 0xFF1A1A1A);
    }
}
