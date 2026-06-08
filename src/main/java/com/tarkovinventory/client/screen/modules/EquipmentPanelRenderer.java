package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Renders armor / rig / backpack UI.
 * No game logic. Only visuals + hover detection.
 */
public class EquipmentPanelRenderer {

    private int left;
    private int top;

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    public void render(GuiGraphics g) {

        int x = left + 7;
        int y = top + 7;

        // HEAD
        g.fill(x, y, x + 26, y + 26, 0xFF1A1A1A);

        // CHEST
        g.fill(x + 40, y, x + 70, y + 30, 0xFF1A1A1A);

        // LEGS
        g.fill(x + 90, y, x + 116, y + 26, 0xFF1A1A1A);

        // FEET
        g.fill(x + 130, y, x + 156, y + 26, 0xFF1A1A1A);

        // RIG
        g.fill(x, y + 50, x + 40, y + 90, 0xFF1A1A1A);

        // BACKPACK
        g.fill(x + 60, y + 50, x + 100, y + 90, 0xFF1A1A1A);

        // WEAPONS
        g.fill(x, y + 110, x + 40, y + 150, 0xFF1A1A1A);
        g.fill(x + 80, y + 110, x + 120, y + 150, 0xFF1A1A1A);
    }
}
