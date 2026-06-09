package com.tarkovinventory.client.screen.layout;

public class UILayout {

    private final UIRoot root;

    public UILayout(UIRoot root) {
        this.root = root;
    }

    public Panel equipment() {
        return new Panel(root.x, root.y, 180, 180);
    }

    public Panel grid() {
        return new Panel(root.x + 190, root.y + 30, 220, 220);
    }

    public Panel loot() {
        return new Panel(root.x + 420, root.y, 160, 240);
    }

    public Panel vicinity() {
        return new Panel(root.x + 420, root.y + 250, 160, 180);
    }
}
