package com.tarkovinventory.client.screen.layout;

/**
 * Clean fixed layout for Tarkov UI panels.
 * No randomness, stable positions.
 */
public class UILayout {

    private final int left;
    private final int top;
    private final int width;
    private final int height;

    public static final int GAP = 8;

    public UILayout(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    // LEFT: Equipment panel
    public int equipmentX() {
        return left + GAP;
    }

    public int equipmentY() {
        return top + GAP;
    }

    // CENTER: Inventory grid
    public int gridX() {
        return left + 200;
    }

    public int gridY() {
        return top + GAP + 40;
    }

    // RIGHT: Loot panel
    public int lootX() {
        return left + 430;
    }

    public int lootY() {
        return top + GAP;
    }

    // RIGHT-BOTTOM: Vicinity
    public int vicinityX() {
        return lootX();
    }

    public int vicinityY() {
        return lootY() + 240;
    }
}
