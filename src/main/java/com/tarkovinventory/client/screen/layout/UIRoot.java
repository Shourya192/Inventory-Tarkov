package com.tarkovinventory.client.screen.layout;

public class UIRoot {

    public final int x;
    public final int y;

    public final int width;
    public final int height;

    public UIRoot(int screenWidth, int screenHeight) {
        this.width = screenWidth;
        this.height = screenHeight;

        this.x = (screenWidth - 650) / 2;
        this.y = (screenHeight - 350) / 2;
    }
}
