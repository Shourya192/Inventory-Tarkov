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

        int s = 24;    // reduced from 36 to shrink total height
        int hGap = 12;
        int vGap = 6;

        int col1 = x;
        int col2 = x + s + hGap;
        int col3 = x + (s * 2) + (hGap * 2);

        int panelWidth = (s * 3) + (hGap * 2);

        // ROW 1
        slot(g, col1, y + (s + vGap) * 0, col1 + s, y + (s + vGap) * 0 + s);  // Balaclava
        slot(g, col3, y + (s + vGap) * 0, col3 + s, y + (s + vGap) * 0 + s);  // Head

        // ROW 2
        slot(g, col1, y + (s + vGap) * 1, col1 + s, y + (s + vGap) * 1 + s);  // Ear
        slot(g, col3, y + (s + vGap) * 1, col3 + s, y + (s + vGap) * 1 + s);  // Face

        // ROW 3
        slot(g, col1, y + (s + vGap) * 2, col1 + s, y + (s + vGap) * 2 + s);  // Rig
        slot(g, col3, y + (s + vGap) * 2, col3 + s, y + (s + vGap) * 2 + s);  // Chest

        // ROW 4 - Pants (center column)
        slot(g, col2, y + (s + vGap) * 3, col2 + s, y + (s + vGap) * 3 + s);  // Pants

        // ROW 5 - Knees (center column)
        slot(g, col2, y + (s + vGap) * 4, col2 + s, y + (s + vGap) * 4 + s);  // Knees

        // ROW 6 - Boots (center column)
        slot(g, col2, y + (s + vGap) * 5, col2 + s, y + (s + vGap) * 5 + s);  // Boots

        // ROW 7 - Backpack (left column)
        slot(g, col1, y + (s + vGap) * 6, col1 + s, y + (s + vGap) * 6 + s);  // Backpack

        // WEAPONS
        int wY = y + (s + vGap) * 7 + vGap;
        slot(g, x, wY, x + panelWidth, wY + s);                                 // Primary
        slot(g, x, wY + s + vGap, x + panelWidth, wY + (s * 2) + vGap);        // Secondary
    }

    public void renderWithBackground(GuiGraphics g) {

        int x = left;
        int y = top;

        int s = 24;
        int hGap = 12;
        int vGap = 6;

        int panelWidth = (s * 3) + (hGap * 2);
        int panelHeight = (s + vGap) * 9 + s;

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
