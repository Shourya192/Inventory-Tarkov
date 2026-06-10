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

        int s = 28;
        int gap = 6;

        // fixed center offset for body alignment
        int centerX = x + 40;

        // =========================
        // ROW 1
        // =========================
        g.fill(x, y, x + s, y + s, 0xFF1A1A1A);                         // Balaclava
        g.fill(x + s + gap, y, x + (s * 2) + gap, y + s, 0xFF1A1A1A);   // Head

        // =========================
        // ROW 2
        // =========================
        g.fill(x, y + s + gap, x + s, y + (s * 2) + gap, 0xFF1A1A1A);   // Ear
        g.fill(x + s + gap, y + s + gap, x + (s * 2) + gap, y + (s * 2) + gap, 0xFF1A1A1A); // Face

        // =========================
        // ROW 3
        // =========================
        g.fill(x, y + (s + gap) * 2, x + s, y + (s + gap) * 3, 0xFF1A1A1A); // Rig
        g.fill(x + s + gap, y + (s + gap) * 2, x + (s * 2) + gap, y + (s + gap) * 3, 0xFF1A1A1A); // Chest

        // =========================
        // ROW 4 (CENTERED)
        // =========================
        g.fill(centerX, y + (s + gap) * 3, centerX + s, y + (s + gap) * 4, 0xFF1A1A1A); // Pants

        // =========================
        // ROW 5
        // =========================
        g.fill(centerX, y + (s + gap) * 4, centerX + s, y + (s + gap) * 5, 0xFF1A1A1A); // Knees
        g.fill(centerX + s + gap, y + (s + gap) * 4, centerX + (s * 2) + gap, y + (s + gap) * 5, 0xFF1A1A1A); // Boots

        // =========================
        // BACKPACK
        // =========================
        g.fill(x, y + (s + gap) * 5, x + s, y + (s + gap) * 7, 0xFF1A1A1A);

        // =========================
        // WEAPONS (FIXED INSIDE PANEL)
        // =========================
        int wY = y + (s + gap) * 7 + 10;

        g.fill(x, wY, x + (s * 3), wY + s, 0xFF1A1A1A);                 // Primary
        g.fill(x, wY + s + gap, x + (s * 3), wY + (s * 2) + gap, 0xFF1A1A1A); // Secondary
    }

    public void renderWithBackground(GuiGraphics g) {

        int x = left;
        int y = top;

        int s = 28;
        int gap = 6;

        int width = (s * 3) + (gap * 2) + 40;
        int height = 260;

        // background panel
        g.fill(x - 8, y - 8, x + width, y + height, 0xFF101010);

        // border
        g.fill(x - 8, y - 8, x + width, y - 6, 0xFF2B2B2B);
        g.fill(x - 8, y + height - 2, x + width, y + height, 0xFF2B2B2B);
        g.fill(x - 8, y - 8, x - 6, y + height, 0xFF2B2B2B);
        g.fill(x + width - 2, y - 8, x + width, y + height, 0xFF2B2B2B);

        render(g);
    }
}
