package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class InventoryGridRenderer {

    private static final int CELL = 18;

    private int left;
    private int top;

    private int hoverSlot = -1;
    private int activeCols = 6;
    private int activeRows = 4;


    public void init(int left, int top) {
        init(left, top, activeCols, activeRows);
    }

    public void init(int left, int top, int activeCols, int activeRows) {
        this.left = left;
        this.top = top;
        this.activeCols = Math.max(1, activeCols);
        this.activeRows = Math.max(1, activeRows);
    }

    public void updateHover(double mouseX, double mouseY) {
        hoverSlot = getSlotAt(mouseX, mouseY);
    }

    public void renderWithBackground(GuiGraphics g) {

        int width = Math.max(8, activeCols) * CELL;
        int height = (4 + activeRows + 6) * CELL;

        g.fill(
                left - 16,
                top - 16,
                left + width + 16,
                top + height + 16,
                0xFF080808
        );

        g.fill(
                left - 12,
                top - 12,
                left + width + 12,
                top + height + 12,
                0xFF111111
        );

        g.fill(
                left - 8,
                top - 8,
                left + width + 8,
                top + height + 8,
                0xFF1A1A1A
        );

        render(g);
    }

    public void render(GuiGraphics g) {

        // ===== POCKETS =====

        int pocketsY = top;

        for (int i = 0; i < 7; i++) {

            int x = left + i * CELL;

            g.fill(
                    x,
                    pocketsY,
                    x + CELL - 1,
                    pocketsY + CELL - 1,
                    0xFF2B2B2B
            );
        }

        // ===== RIG =====

        int rigY = pocketsY + 40;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {

                int x = left + col * CELL;
                int y = rigY + row * CELL;

                g.fill(
                        x,
                        y,
                        x + CELL - 1,
                        y + CELL - 1,
                        0xFF2B2B2B
                );
            }
        }

        // ===== BACKPACK =====

        int backpackY = rigY + (3 * CELL) + 40;

        for (int row = 0; row < activeRows; row++) {
            for (int col = 0; col < activeCols; col++) {

                int x = left + col * CELL;
                int y = backpackY + row * CELL;

                int slot = row * 12 + col;
                g.fill(
                        x,
                        y,
                        x + CELL - 1,
                        y + CELL - 1,
                        hoverSlot == slot ? 0xFF4A4A4A : 0xFF2B2B2B
                );
            }
        }
    }

    public int getSlotAt(double mouseX, double mouseY) {
        int backpackY = getBackpackY();
        int col = ((int) mouseX - left) / CELL;
        int row = ((int) mouseY - backpackY) / CELL;
        if (mouseX >= left && mouseY >= backpackY
                && col >= 0 && col < activeCols && row >= 0 && row < activeRows) {
            return row * 12 + col;
        }
        return -1;
    }

    public int getPocketsY() { return top; }
    public int getPocketX(int i) { return left + i * CELL; }
    public int getBackpackY() { return top + 40 + (3 * CELL) + 40; }

    public int getWidth() {
        return Math.max(8, activeCols) * CELL;
    }

    public int getHeight() {
        return (4 + activeRows + 6) * CELL;
    }
}
