package com.tarkovinventory.client.screen.layout;

/**
 * FINAL CENTERED LAYOUT SYSTEM
 * Fixes all positioning drift issues.
 */
public class UILayout {

    private final int left;
    private final int top;
    private final int width;
    private final int height;

    private static final int EQUIP_W = 180;
    private static final int GRID_W  = 220;
    private static final int LOOT_W  = 160;
    private static final int GAP     = 10;

    public UILayout(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    private int centerX() {
        return left + width / 2;
    }

    // ─────────────────────────────
    // EQUIPMENT (LEFT)
    // ─────────────────────────────
    public int equipmentX() {
        return centerX() - (EQUIP_W + GRID_W + LOOT_W) / 2;
    }

    public int equipmentY() {
        return top + GAP;
    }

    // ─────────────────────────────
    // GRID (CENTER)
    // ─────────────────────────────
    public int gridX() {
        return equipmentX() + EQUIP_W + GAP;
    }

    public int gridY() {
        return top + GAP + 30;
    }

    // ─────────────────────────────
    // LOOT (RIGHT)
    // ─────────────────────────────
    public int lootX() {
        return gridX() + GRID_W + GAP;
    }

    public int lootY() {
        return top + GAP;
    }

    // ─────────────────────────────
    // VICINITY (BOTTOM RIGHT)
    // ─────────────────────────────
    public int vicinityX() {
        return lootX();
    }

    public int vicinityY() {
        return lootY() + 250;
    }
}
