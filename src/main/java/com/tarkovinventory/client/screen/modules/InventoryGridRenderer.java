package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class InventoryGridRenderer {

    private static final int CELL = 17;

    private int left;
    private int top;

    private final int cols = 5;
    private final int rows = 5;

    private int hoverSlot = -1;

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    public void updateHover(double mouseX, double mouseY) {
        hoverSlot = getSlotAt(mouseX, mouseY);
    }

    // 🔥 FULL TARKOV-STYLE BACKGROUND (MATCHES EQUIPMENT NOW)
    public void renderWithBackground(GuiGraphics g) {

        int w = cols * CELL;
        int h = rows * CELL;

        int x = left;
        int y = top;

        // OUTER DARK FRAME (same weight as equipment)
        g.fill(x - 14, y - 14, x + w + 14, y + h + 14, 0xFF0B0B0B);

        // MID FRAME (depth layer)
        g.fill(x - 12, y - 12, x + w + 12, y + h + 12, 0xFF121212);

        // INNER FRAME (main panel body)
        g.fill(x - 10, y - 10, x + w + 10, y + h + 10, 0xFF1A1A1A);

        // subtle inner border
        g.fill(x - 8, y - 8, x + w + 8, y + h + 8, 0xFF202020);

        render(g);
    }

    public void render(GuiGraphics g) {

        int slotIndex = 0;

        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {

                int px = left + x * CELL;
                int py = top + y * CELL;

                int color = 0xFF2F2F2F;

                // hover highlight
                if (slotIndex == hoverSlot) {
                    color = 0xFF5A5A5A;
                }

                g.fill(px, py, px + CELL, py + CELL, color);

                slotIndex++;
            }
        }
    }

    public int getSlotAt(double mouseX, double mouseY) {

        int col = (int)((mouseX - left) / CELL);
        int row = (int)((mouseY - top) / CELL);

        if (col < 0 || row < 0 || col >= cols || row >= rows) return -1;

        return col + row * cols;
    }
}
