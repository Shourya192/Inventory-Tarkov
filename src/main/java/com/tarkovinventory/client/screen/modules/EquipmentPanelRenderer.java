package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class EquipmentPanelRenderer {

    private final Minecraft mc = Minecraft.getInstance();

    private int left;
    private int top;

    private static final ResourceLocation SILHOUETTE =
            new ResourceLocation("tarkovinventory", "textures/ui/silhouette.png");

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    // ======================
    // SLOT BOX
    // ======================
    private void slot(GuiGraphics g, int x1, int y1, int x2, int y2) {

        g.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF000000);
        g.fill(x1, y1, x2, y2, 0xFF161616);

        g.fill(x1, y1, x1 + 1, y2, 0xFF2A2A2A);
        g.fill(x1, y1, x2, y1 + 1, 0xFF3A3A3A);

        g.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xFF101010);
    }

    // ======================
    // LABEL
    // ======================
    private void label(GuiGraphics g, String text, int x, int y) {
        g.drawString(mc.font, text, x, y, 0xFFB0B0B0, false);
    }

    // ======================
    // MAIN RENDER
    // ======================
    public void render(GuiGraphics g) {

        int x = left;
        int y = top;

        int s = 24;
        int vGap = 6;

        // ======================
        // REAL EFT-STYLE LAYOUT ZONES
        // ======================

        int leftCol = x;
        int centerCol = x + 90;     // body / pants / boots column
        int rightCol = x + 180;     // weapons / chest / head side

        int panelWidth = 260;

        // ======================
        // SILHOUETTE (CENTER FOCUS)
        // ======================
        g.blit(
                SILHOUETTE,
                x + (panelWidth / 2) - 40,
                y + 10,
                0, 0,
                80, 200,
                80, 200
        );

        // ======================
        // ROW 1
        // ======================
        label(g, "BALACLAVA", leftCol, y - 8);
        label(g, "HEAD", rightCol, y - 8);

        slot(g, leftCol, y, leftCol + s, y + s);
        slot(g, rightCol, y, rightCol + s, y + s);

        // ======================
        // ROW 2
        // ======================
        label(g, "EAR", leftCol, y + (s + vGap) - 8);
        label(g, "FACE", rightCol, y + (s + vGap) - 8);

        slot(g, leftCol, y + (s + vGap), leftCol + s, y + (s + vGap) + s);
        slot(g, rightCol, y + (s + vGap), rightCol + s, y + (s + vGap) + s);

        // ======================
        // ROW 3 (ARMOR)
        // ======================
        label(g, "RIG", leftCol, y + (s + vGap) * 2 - 8);
        label(g, "CHEST", rightCol, y + (s + vGap) * 2 - 8);

        slot(g, leftCol, y + (s + vGap) * 2, leftCol + s, y + (s + vGap) * 2 + s);
        slot(g, rightCol, y + (s + vGap) * 2, rightCol + s, y + (s + vGap) * 2 + s);

        // ======================
        // CENTER BODY COLUMN
        // ======================
        label(g, "PANTS", centerCol, y + (s + vGap) * 3 - 8);
        slot(g, centerCol, y + (s + vGap) * 3, centerCol + s, y + (s + vGap) * 3 + s);

        label(g, "KNEES", centerCol, y + (s + vGap) * 4 - 8);
        slot(g, centerCol, y + (s + vGap) * 4, centerCol + s, y + (s + vGap) * 4 + s);

        label(g, "BOOTS", centerCol, y + (s + vGap) * 5 - 8);
        slot(g, centerCol, y + (s + vGap) * 5, centerCol + s, y + (s + vGap) * 5 + s);

        // ======================
        // BACKPACK (LEFT SIDE)
        // ======================
        label(g, "BACKPACK", leftCol, y + (s + vGap) * 6 - 8);
        slot(g, leftCol, y + (s + vGap) * 6, leftCol + s, y + (s + vGap) * 6 + s);

        // ======================
        // WEAPONS (RIGHT SIDE WIDE)
        // ======================
        int wY = y + (s + vGap) * 7 + vGap;

        label(g, "PRIMARY", rightCol, wY - 8);
        slot(g, rightCol, wY, rightCol + s, wY + s);

        label(g, "SECONDARY", rightCol, wY + s + vGap - 8);
        slot(g, rightCol, wY + s + vGap, rightCol + s, wY + (s * 2) + vGap);
    }
}
