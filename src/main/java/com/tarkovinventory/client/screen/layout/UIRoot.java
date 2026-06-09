package com.tarkovinventory.client.screen.layout;

/**
 * Single anchor for ALL UI rendering.
 */
public class UIRoot {

    public final int x;
    public final int y;

    public UIRoot(int screenWidth, int screenHeight) {
        this.x = (screenWidth - 600) / 2;
        this.y = (screenHeight - 300) / 2;
    }
}
