package com.tarkovinventory.client.screen.layout;

/**
 * Central layout controller for Tarkov UI.
 * Fixes all hardcoded offsets.
 */
public class UILayout {

    private final int left;
    private final int top;

    // Panel sizes
    public static final int EQUIP_W = 180;
    public static final int GRID_W  = 220;
    public static final int LOOT_W  = 160;
    public static final int GAP     = 8;

    public UILayout(int left, int top) {
        this.left = left;
        this.top = top;
    }

    // ─────────────────────────────
    // LEFT PANEL (equipment)
    // ─────────────────────────────
    public int equipmentX() {
        return left + GAP;
    }

    public int equipmentY() {
        return top + GAP;
    }

    // ─────────────────────────────
    // CENTER PANEL (grid)
    // ─────────────────────────────
    public int gridX() {
        return equipmentX() + EQUIP_W + GAP;
    }

    public int gridY() {
        return top + GAP + 60;
    }

    // ─────────────────────────────
    // RIGHT PANEL (loot/vicinity)
    // ─────────────────────────────
    public int lootX() {
        return gridX() + GRID_W + GAP;
    }

    public int lootY() {
        return top + GAP;
    }

    public int vicinityX() {
        return lootX();
    }

    public int vicinityY() {
        return lootY() + 220;
    }
}
