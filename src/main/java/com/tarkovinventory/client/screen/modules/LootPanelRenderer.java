package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class LootPanelRenderer {

    public void render(GuiGraphics g, int left, int top) {

        int x = left + 300;
        int y = top + 10;

        g.fill(x, y, x + 150, y + 220, 0xFF2A1010);

        for (int i = 0; i < 12; i++) {
            g.fill(x + 5, y + 5 + i * 16, x + 145, y + 18 + i * 16, 0xFF3A1A1A);
        }
    }

    public void mouseClicked(double mx, double my, int btn, int x, int y) {}
    public void mouseReleased(double mx, double my, int btn) {}
    public void mouseDragged(double mx, double my, int btn, double dx, double dy, Object dragState) {}
}
