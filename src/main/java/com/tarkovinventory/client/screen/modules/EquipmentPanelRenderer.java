package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Renders armor / rig / backpack UI.
 * Visual only. No logic.
 */
public class EquipmentPanelRenderer {

    private int left;
    private int top;

    // slot size standardization
    private static final int SLOT = 26;
    private static final int GAP = 6;

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    private int x() {
        return left + 7;
    }

    private int y() {
        return top + 7;
    }

    public void render(GuiGraphics g, int screenX, int screenY, int mouseX, int mouseY) {

        int x = x();
        int y = y();

        // panel background
        g.fill(x - 4, y - 4, x + 160, y + 170, 0xFF121212);

        // ── HEAD ─────────────────────────────
        g.fill(x, y, x + SLOT, y + SLOT, 0xFF1A1A1A);

        // ── CHEST ────────────────────────────
        g.fill(x + (SLOT + GAP), y, x + (SLOT + GAP) + SLOT + 4, y + SLOT + 4, 0xFF1A1A1A);

        // ── LEGS ─────────────────────────────
        g.fill(x + 2 * (SLOT + GAP), y, x + 2 * (SLOT + GAP) + SLOT, y + SLOT, 0xFF1A1A1A);

        // ── FEET ─────────────────────────────
        g.fill(x + 3 * (SLOT + GAP), y, x + 3 * (SLOT + GAP) + SLOT, y + SLOT, 0xFF1A1A1A);

        // ── RIG ──────────────────────────────
        int rigY = y + 55;
        g.fill(x, rigY, x + SLOT + 10, rigY + SLOT + 10, 0xFF1A1A1A);

        // ── BACKPACK ─────────────────────────
        g.fill(x + 70, rigY, x + 70 + SLOT + 10, rigY + SLOT + 10, 0xFF1A1A1A);

        // ── WEAPONS ──────────────────────────
        int wY = y + 110;
        g.fill(x, wY, x + SLOT + 10, wY + SLOT + 10, 0xFF1A1A1A);
        g.fill(x + 80, wY, x + 80 + SLOT + 10, wY + SLOT + 10, 0xFF1A1A1A);
    }
}
