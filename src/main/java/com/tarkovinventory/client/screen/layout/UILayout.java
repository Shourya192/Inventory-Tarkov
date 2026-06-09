package com.tarkovinventory.client.screen.layout;

/**
 * Returns fixed UI panels.
 * NO logic, NO rendering, ONLY layout.
 */
public class UILayout {

    private final int left;
    private final int top;

    public UILayout(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
    }

    public Panel equipment() {
        return new Panel(left + 10, top + 10, 180, 180);
    }

    public Panel grid() {
        return new Panel(left + 200, top + 40, 220, 220);
    }

    public Panel loot() {
        return new Panel(left + 430, top + 10, 160, 240);
    }

    public Panel vicinity() {
        return new Panel(left + 430, top + 260, 160, 180);
    }
}
