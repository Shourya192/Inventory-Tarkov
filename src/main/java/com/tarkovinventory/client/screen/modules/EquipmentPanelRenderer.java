package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class EquipmentPanelRenderer {

    private int left;
    private int top;

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    // Styled slot renderer (unchanged - good system)
    private void slot(GuiGraphics g, int x1, int y1, int x2, int y2) {

        g.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF000000);
        g.fill(x1, y1, x2, y2, 0xFF1C1C1C);
        g.fill(x1, y1, x2, y1 + 1, 0xFF2A2A2A);
        g.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xFF171717);
    }

    public void render(GuiGraphics g) {

        int x = left;
        int y = top;

        int s = 28;
        int gap = 6;

        // ===== FIXED PANEL WIDTH (important) =====
        int panelWidth = (s * 2) + gap;

        // ===== BODY CENTER COLUMN =====
        int centerX = x + s; // perfect middle column

        // =========================
        // ROW 1
        // =========================
        slot(g, x, y, x + s, y + s);                             // Balaclava
        slot(g, x + s + gap, y, x + (s * 2) + gap, y + s);       // Head

        // =========================
        // ROW 2
        // =========================
        slot(g, x, y + s + gap, x + s, y + (s * 2) + gap);       // Ear
        slot(g, x + s + gap, y + s + gap, x + (s * 2) + gap, y + (s * 2) + gap); // Face

        // =========================
        // ROW 3
        // =========================
        slot(g, x, y + (s + gap) * 2, x + s, y + (s + gap) * 3); // Rig
        slot(g, x + s + gap, y + (s + gap) * 2, x + (s * 2) + gap, y + (s + gap) * 3); // Chest

        // =========================
        // ROW 4 (PANTS CENTERED PROPERLY)
        // =========================
        slot(g,
                centerX,
                y + (s + gap) * 3,
                centerX + s,
                y + (s + gap) * 4
        );

        // =========================
        // ROW 5 (KNEES + BOOTS)
        // =========================
        slot(g,
                centerX,
                y + (s + gap) * 4,
                centerX + s,
                y + (s + gap) * 5
        );

        slot(g,
                centerX + s + gap,
                y + (s + gap) * 4,
                centerX + (s * 2) + gap,
                y + (s + gap) * 5
        );

        // =========================
        // BACKPACK (LEFT SIDE)
        // =========================
        slot(g,
                x,
                y + (s + gap) * 5,
                x + s,
                y + (s + gap) * 7
        );

        // =========================
        // WEAPONS (NO OVERFLOW FIX)
        // =========================
        int wY = y + (s + gap) * 7 + 10;

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

        int x = left;
        int y = top;

        int s = 28;
        int gap = 6;

        int panelWidth = (s * 2) + gap;
        int panelHeight = (s + gap) * 8;

        g.fill(x - 8, y - 8, x + panelWidth + 8, y + panelHeight + 8, 0xFF101010);

        g.fill(x - 8, y - 8, x + panelWidth + 8, y - 6, 0xFF2B2B2B);
        g.fill(x - 8, y + panelHeight + 6, x + panelWidth + 8, y + panelHeight + 8, 0xFF2B2B2B);
        g.fill(x - 8, y - 8, x - 6, y + panelHeight + 8, 0xFF2B2B2B);
        g.fill(x + panelWidth + 6, y - 8, x + panelWidth + 8, y + panelHeight + 8, 0xFF2B2B2B);

        render(g);
    }
}
