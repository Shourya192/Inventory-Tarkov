package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class InventoryGridRenderer {

    private static final int CELL = 18;

    private int left;
    private int top;

    private int hoverSlot = -1;

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    public void updateHover(double mouseX, double mouseY) {
        hoverSlot = -1;
    }

    public void renderWithBackground(GuiGraphics g) {

        int width = 8 * CELL;
        int height = 16 * CELL;

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

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 6; col++) {

                int x = left + col * CELL;
                int y = backpackY + row * CELL;

                g.fill(
                        x,
                        y,
                        x + CELL - 1,
                        y + CELL - 1,
                        0xFF2B2B2B
                );
            }
        }
    }

    public int getSlotAt(double mouseX, double mouseY) {
        return -1;
    }

    public int getWidth() {
        return 8 * CELL;
    }

    public int getHeight() {
        return 16 * CELL;
    }
}
