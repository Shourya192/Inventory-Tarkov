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

        // HEAD
        g.fill(x + 55, y, x + 85, y + 30, 0xFF1A1A1A);

        // ARMOR
        g.fill(x + 40, y + 40, x + 100, y + 90, 0xFF1A1A1A);

        // RIG
        g.fill(x, y + 105, x + 60, y + 145, 0xFF1A1A1A);

        // BACKPACK
        g.fill(x + 80, y + 105, x + 140, y + 145, 0xFF1A1A1A);

        // PRIMARY WEAPON
        g.fill(x, y + 165, x + 140, y + 195, 0xFF1A1A1A);

        // SECONDARY WEAPON
        g.fill(x, y + 205, x + 140, y + 235, 0xFF1A1A1A);
    }

    public void renderWithBackground(GuiGraphics g) {

        int x = left;
        int y = top;

        g.fill(
                x - 6,
                y - 6,
                x + 170,
                y + 250,
                0xFF0F0F0F
        );

        render(g);
    }
}
