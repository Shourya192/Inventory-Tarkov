package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class EquipmentPanelRenderer {

    private int left;
    private int top;

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    private void slot(GuiGraphics g, int x1, int y1, int x2, int y2) {

        g.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF000000);
        g.fill(x1, y1, x2, y2, 0xFF1C1C1C);
        g.fill(x1, y1, x2, y1 + 1, 0xFF2A2A2A);
        g.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xFF171717);
    }

    public void render(GuiGraphics g) {

        int s = 28;
        int gap = 6;

        // =========================
        // PANEL SIZE (IMPORTANT FIX)
        // =========================
        int panelWidth = (s * 3) + (gap * 2);
        int panelHeight = 260;

        int x = left;
        int y = top;

        // center content horizontally inside panel
        int cx = x + (panelWidth / 2) - (s / 2);

        // =========================
        // ROW OFFSETS (NO OVERLAP)
        // =========================
        int r1 = y;
        int r2 = y + (s + gap);
        int r3 = y + (s + gap) * 2;
        int r4 = y + (s + gap) * 3;
        int r5 = y + (s + gap) * 4;
        int r6 = y + (s + gap) * 5;

        // =========================
        // TOP ROWS (CENTERED PAIRS)
        // =========================

        slot(g, x, r1, x + s, r1 + s);                 // Balaclava
        slot(g, x + s + gap, r1, x + s * 2 + gap, r1 + s); // Head

        slot(g, x, r2, x + s, r2 + s);                 // Ear
        slot(g, x + s + gap, r2, x + s * 2 + gap, r2 + s); // Face

        slot(g, x, r3, x + s, r3 + s);                 // Rig
        slot(g, x + s + gap, r3, x + s * 2 + gap, r3 + s); // Chest

        // =========================
        // MIDDLE BODY (CENTERED)
        // =========================

        slot(g,
                cx,
                r4,
                cx + s,
                r4 + s
        ); // Pants

        slot(g,
                cx,
                r5,
                cx + s,
                r5 + s
        ); // Knees

        slot(g,
                cx + s + gap,
                r5,
                cx + (s * 2) + gap,
                r5 + s
        ); // Boots

        // =========================
        // BACKPACK (LEFT SIDE)
        // =========================

        slot(g,
                x,
                r4,
                x + s,
                r6 + s
        );

        // =========================
        // WEAPONS (CLAMPED INSIDE PANEL)
        // =========================

        int wY = r6 + s + 10;

        slot(g,
                x,
                wY,
                x + panelWidth,
                wY + s
        ); // Primary

        slot(g,
                x,
                wY + s + gap,
                x + panelWidth,
                wY + (s * 2) + gap
        ); // Secondary
    }

    public void renderWithBackground(GuiGraphics g) {

        int s = 28;
        int gap = 6;

        int panelWidth = (s * 3) + (gap * 2);
        int panelHeight = 260;

        int x = left;
        int y = top;

        g.fill(x - 8, y - 8, x + panelWidth + 8, y + panelHeight + 8, 0xFF101010);

        g.fill(x - 8, y - 8, x + panelWidth + 8, y - 6, 0xFF2B2B2B);
        g.fill(x - 8, y + panelHeight + 6, x + panelWidth + 8, y + panelHeight + 8, 0xFF2B2B2B);
        g.fill(x - 8, y - 8, x - 6, y + panelHeight + 8, 0xFF2B2B2B);
        g.fill(x + panelWidth + 6, y - 8, x + panelWidth + 8, y + panelHeight + 8, 0xFF2B2B2B);

        render(g);
    }
}
