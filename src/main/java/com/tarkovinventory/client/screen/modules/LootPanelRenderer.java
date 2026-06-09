package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class LootPanelRenderer implements IModule {

    private int left;
    private int top;

    public void init(int left, int top) {
        this.left = left;
        this.top = top;
    }

    @Override
    public void render(GuiGraphics g, int left, int top, int mouseX, int mouseY) {

        int x = this.left + 300;
        int y = this.top + 10;

        g.fill(x, y, x + 150, y + 220, 0xFF2A1010);

        for (int i = 0; i < 12; i++) {
            g.fill(
                    x + 5,
                    y + 5 + i * 16,
                    x + 145,
                    y + 18 + i * 16,
                    0xFF3A1A1A
            );
        }
    }
}
