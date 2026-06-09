package com.tarkovinventory.client.screen.layout;

/**
 * Panel-based layout system (NO raw coordinates in modules).
 */
public class UILayout {

    private final int left;
    private final int top;

    public UILayout(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
    }

    // LEFT: Equipment panel
    public Panel equipment() {
        return new Panel(left + 10, top + 10, 180, 180);
    }

    // CENTER: Inventory grid
    public Panel grid() {
        return new Panel(left + 200, top + 40, 220, 220);
    }

    // RIGHT: Loot panel
    public Panel loot() {
        return new Panel(left + 430, top + 10, 160, 240);
    }

    // RIGHT BOTTOM: Vicinity
    public Panel vicinity() {
        return new Panel(left + 430, top + 260, 160, 180);
    }
}
