package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Ground items UI (near player).
 */
public class VicinityRenderer {

    private int left;
    private int top;

    private static final int PANEL_W = 140;
    private static final int PANEL_H = 200;
    private static final int ROW_H = 18;

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    private int x() {
        return left + 450;
    }

    private int y() {
        return top + 10;
    }

    public void render(GuiGraphics g, int screenX, int screenY, int mouseX, int mouseY) {

        int x = x();
        int y = y();

        // panel background
        g.fill(x, y, x + PANEL_W, y + PANEL_H, 0xFF101010);

        // header strip
        g.fill(x, y, x + PANEL_W, y + 14, 0xFF0D0D0D);

        // item rows
        for (int i = 0; i < 10; i++) {

            int rowY = y + 18 + i * ROW_H;

            g.fill(
                    x + 5,
                    rowY,
                    x + PANEL_W - 5,
                    rowY + ROW_H - 2,
                    0xFF1A1A1A
            );
        }
    }
}
