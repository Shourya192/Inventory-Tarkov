package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class InventoryGridRenderer {

    private static final int CELL = 18;

    private int left;
    private int top;

    // Bigger Tarkov-style stash
    private final int cols = 10;
    private final int rows = 14;

    private int hoverSlot = -1;

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    public void updateHover(double mouseX, double mouseY) {
        hoverSlot = getSlotAt(mouseX, mouseY);
    }

    public void renderWithBackground(GuiGraphics g) {

        int w = cols * CELL;
        int h = rows * CELL;

        int x = left;
        int y = top;

        // Outer frame
        g.fill(x - 16, y - 16, x + w + 16, y + h + 16, 0xFF080808);

        // Main panel
        g.fill(x - 12, y - 12, x + w + 12, y + h + 12, 0xFF111111);

        // Inner frame
        g.fill(x - 8, y - 8, x + w + 8, y + h + 8, 0xFF1A1A1A);

        render(g);
    }

    public void render(GuiGraphics g) {

        int slotIndex = 0;

        for (int row = 0; row < rows; row++) {

            for (int col = 0; col < cols; col++) {

                int px = left + col * CELL;
                int py = top + row * CELL;

                int color = 0xFF2B2B2B;

                if (slotIndex == hoverSlot) {
                    color = 0xFF555555;
                }

                // Slot background
                g.fill(px, py, px + CELL - 1, py + CELL - 1, color);

                // Slot border
                g.fill(px, py, px + CELL - 1, py + 1, 0xFF3A3A3A);
                g.fill(px, py, px + 1, py + CELL - 1, 0xFF3A3A3A);

                slotIndex++;
            }
        }
    }

    public int getSlotAt(double mouseX, double mouseY) {

        int col = (int)((mouseX - left) / CELL);
        int row = (int)((mouseY - top) / CELL);

        if (col < 0 || row < 0 || col >= cols || row >= rows) {
            return -1;
        }

        return row * cols + col;
    }

    public int getWidth() {
        return cols * CELL;
    }

    public int getHeight() {
        return rows * CELL;
    }
}
