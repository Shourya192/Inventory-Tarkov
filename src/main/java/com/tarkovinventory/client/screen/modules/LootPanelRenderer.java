package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Corpse loot UI (stable modular version).
 */
public class LootPanelRenderer {

    private int left;
    private int top;

    // panel tuning
    private static final int PANEL_W = 150;
    private static final int PANEL_H = 220;

    private static final int ROW_H = 16;

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    private int x() {
        return left + 300;
    }

    private int y() {
        return top + 10;
    }

    public void render(GuiGraphics g, int screenX, int screenY, int mouseX, int mouseY) {

        int x = x();
        int y = y();

        // main panel background
        g.fill(x, y, x + PANEL_W, y + PANEL_H, 0xFF2A1010);

        // header bar
        g.fill(x, y, x + PANEL_W, y + 14, 0xFF1A0A0A);

        // rows
        for (int i = 0; i < 12; i++) {

            int rowY = y + 16 + i * ROW_H;

            g.fill(
                    x + 5,
                    rowY,
                    x + PANEL_W - 5,
                    rowY + ROW_H - 2,
                    0xFF3A1A1A
            );
        }
    }
}
