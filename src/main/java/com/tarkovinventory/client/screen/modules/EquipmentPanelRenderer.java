package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class EquipmentPanelRenderer {

    private int left;
    private int top;

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    // Small helper for styled slot
    private void slot(GuiGraphics g, int x1, int y1, int x2, int y2) {

        // outer shadow
        g.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF000000);

        // main body (slightly lighter)
        g.fill(x1, y1, x2, y2, 0xFF1C1C1C);

        // highlight top edge
        g.fill(x1, y1, x2, y1 + 1, 0xFF2A2A2A);

        // subtle inner depth
        g.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xFF171717);
    }

    public void render(GuiGraphics g) {

        int x = left;
        int y = top;

        int s = 28;
        int gap = 6;

        // ===== Row 1 =====
        slot(g, x, y, x + s, y + s);                               // Balaclava
        slot(g, x + s + gap, y, x + s * 2 + gap, y + s);          // Head

        // ===== Row 2 =====
        slot(g, x, y + s + gap, x + s, y + s * 2 + gap);          // Ear
        slot(g, x + s + gap, y + s + gap, x + s * 2 + gap, y + s * 2 + gap); // Face

        // ===== Row 3 =====
        slot(g, x, y + (s + gap) * 2, x + s, y + (s + gap) * 3);  // Rig
        slot(g, x + s + gap, y + (s + gap) * 2, x + s * 2 + gap, y + (s + gap) * 3); // Chest

        // ===== Row 4 =====
        slot(g, x + s / 2, y + (s + gap) * 3, x + s * 1.5f, y + (s + gap) * 4); // Pants

        // ===== Row 5 =====
        slot(g, x + s / 2, y + (s + gap) * 4, x + s, y + (s + gap) * 5);       // Knees
        slot(g, x + s, y + (s + gap) * 4, x + s * 1.5f, y + (s + gap) * 5);    // Boots

        // ===== Backpack =====
        slot(g, x, y + (s + gap) * 5, x + s, y + (s + gap) * 7);

        // ===== Weapons =====
        int wY = y + (s + gap) * 7 + 10;

        slot(g, x, wY, x + s * 3, wY + s);
        slot(g, x, wY + s + gap, x + s * 3, wY + s * 2);
    }

    public void renderWithBackground(GuiGraphics g) {

        int x = left;
        int y = top;

        // panel background (same style as other panels)
        g.fill(x - 8, y - 8, x + 150, y + 270, 0xFF101010);

        // border frame
        g.fill(x - 8, y - 8, x + 150, y - 6, 0xFF2B2B2B);
        g.fill(x - 8, y + 270, x + 150, y + 272, 0xFF2B2B2B);
        g.fill(x - 8, y - 8, x - 6, y + 272, 0xFF2B2B2B);
        g.fill(x + 150, y - 8, x + 152, y + 272, 0xFF2B2B2B);

        render(g);
    }
}
