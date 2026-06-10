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

        int x = left;
        int y = top;

        int s = 36;    // was 28 — bigger slots
        int gap = 10;  // was 6 — more breathing room

        int panelWidth = (s * 2) + gap;

        // ROW 1
        slot(g, x, y, x + s, y + s);                                                              // Balaclava
        slot(g, x + s + gap, y, x + (s * 2) + gap, y + s);                                        // Head

        // ROW 2
        slot(g, x, y + (s + gap), x + s, y + (s * 2) + gap);                                      // Ear
        slot(g, x + s + gap, y + (s + gap), x + (s * 2) + gap, y + (s * 2) + gap);                // Face

        // ROW 3
        slot(g, x, y + (s + gap) * 2, x + s, y + (s + gap) * 2 + s);                             // Rig
        slot(g, x + s + gap, y + (s + gap) * 2, x + (s * 2) + gap, y + (s + gap) * 2 + s);       // Chest

        // ROW 4 - Pants (right column only)
        slot(g, x + s + gap, y + (s + gap) * 3, x + (s * 2) + gap, y + (s + gap) * 3 + s);       // Pants

        // ROW 5 - Knees (right column only)
        slot(g, x + s + gap, y + (s + gap) * 4, x + (s * 2) + gap, y + (s + gap) * 4 + s);       // Knees

        // ROW 6 - Boots (right column only)
        slot(g, x + s + gap, y + (s + gap) * 5, x + (s * 2) + gap, y + (s + gap) * 5 + s);       // Boots

        // ROW 7 - Backpack (left column only)
        slot(g, x, y + (s + gap) * 6, x + s, y + (s + gap) * 6 + s);                             // Backpack

        // WEAPONS
        int wY = y + (s + gap) * 7 + gap;
        slot(g, x, wY, x + panelWidth, wY + s);                                                    // Primary
        slot(g, x, wY + s + gap, x + panelWidth, wY + (s * 2) + gap);                             // Secondary
    }

    public void renderWithBackground(GuiGraphics g) {

        int x = left;
        int y = top;

        int s = 36;
        int gap = 10;

        int panelWidth = (s * 2) + gap;
        int panelHeight = (s + gap) * 9 + s;

        // background panel
        g.fill(x - 10, y - 10, x + panelWidth + 10, y + panelHeight + 10, 0xFF101010);

        // border frame
        g.fill(x - 10, y - 10, x + panelWidth + 10, y - 8, 0xFF2B2B2B);
        g.fill(x - 10, y + panelHeight - 2, x + panelWidth + 10, y + panelHeight + 10, 0xFF2B2B2B);
        g.fill(x - 10, y - 10, x - 8, y + panelHeight + 10, 0xFF2B2B2B);
        g.fill(x + panelWidth + 8, y - 10, x + panelWidth + 10, y + panelHeight + 10, 0xFF2B2B2B);

        render(g);
    }
}
