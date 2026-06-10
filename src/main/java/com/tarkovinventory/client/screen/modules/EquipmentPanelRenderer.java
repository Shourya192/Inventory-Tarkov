package com.tarkovinventory.client.screen.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class EquipmentPanelRenderer {

    private final Minecraft mc = Minecraft.getInstance();

    private int left;
    private int top;

    // 🧍 silhouette texture
    private static final ResourceLocation SILHOUETTE =
            new ResourceLocation("tarkovinventory", "textures/ui/silhouette.png");

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    // ======================
    // SLOT RENDER
    // ======================
    private void slot(GuiGraphics g, int x1, int y1, int x2, int y2) {

        g.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF000000);
        g.fill(x1, y1, x2, y2, 0xFF161616);

        g.fill(x1, y1, x1 + 1, y2, 0xFF2A2A2A);
        g.fill(x1, y1, x2, y1 + 1, 0xFF3A3A3A);

        g.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xFF101010);
    }

    // ======================
    // LABELS
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
        int hGap = 12;
        int vGap = 6;

        int col1 = x;
        int col2 = x + s + hGap;
        int col3 = x + (s * 2) + (hGap * 2);

        int panelWidth = (s * 3) + (hGap * 2);

        // ======================
        // 1. SILHOUETTE (BACKGROUND LAYER)
        // ======================
        g.blit(
                SILHOUETTE,
                x + (panelWidth / 2) - 32,
                y + 10,
                0, 0,
                64, 200,
                64, 200
        );

        // ======================
        // ROW 1
        // ======================
        label(g, "BALACLAVA", col1, y - 8);
        label(g, "HEAD", col3, y - 8);

        slot(g, col1, y, col1 + s, y + s);
        slot(g, col3, y, col3 + s, y + s);

        // ======================
        // ROW 2
        // ======================
        label(g, "EAR", col1, y + (s + vGap) - 8);
        label(g, "FACE", col3, y + (s + vGap) - 8);

        slot(g, col1, y + (s + vGap), col1 + s, y + (s + vGap) + s);
        slot(g, col3, y + (s + vGap), col3 + s, y + (s + vGap) + s);

        // ======================
        // ROW 3
        // ======================
        label(g, "RIG", col1, y + (s + vGap) * 2 - 8);
        label(g, "CHEST", col3, y + (s + vGap) * 2 - 8);

        slot(g, col1, y + (s + vGap) * 2, col1 + s, y + (s + vGap) * 2 + s);
        slot(g, col3, y + (s + vGap) * 2, col3 + s, y + (s + vGap) * 2 + s);

        // ======================
        // CENTER BODY
        // ======================
        label(g, "PANTS", col2, y + (s + vGap) * 3 - 8);
        slot(g, col2, y + (s + vGap) * 3, col2 + s, y + (s + vGap) * 3 + s);

        label(g, "KNEES", col2, y + (s + vGap) * 4 - 8);
        slot(g, col2, y + (s + vGap) * 4, col2 + s, y + (s + vGap) * 4 + s);

        label(g, "BOOTS", col2, y + (s + vGap) * 5 - 8);
        slot(g, col2, y + (s + vGap) * 5, col2 + s, y + (s + vGap) * 5 + s);

        // ======================
        // BACKPACK
        // ======================
        label(g, "BACKPACK", col1, y + (s + vGap) * 6 - 8);
        slot(g, col1, y + (s + vGap) * 6, col1 + s, y + (s + vGap) * 6 + s);

        // ======================
        // WEAPONS
        // ======================
        int wY = y + (s + vGap) * 7 + vGap;

        label(g, "PRIMARY", x, wY - 8);
        slot(g, x, wY, x + panelWidth, wY + s);

        label(g, "SECONDARY", x, wY + s + vGap - 8);
        slot(g, x, wY + s + vGap, x + panelWidth, wY + (s * 2) + vGap);
    }
}
