package com.tarkovinventory.client.screen.modules;

import com.tarkovinventory.client.screen.layout.Panel;
import net.minecraft.client.gui.GuiGraphics;

public class LootPanelRenderer {

    public void render(GuiGraphics g, Panel p) {

        p.draw(g);

        for (int i = 0; i < 12; i++) {

            int y = p.y + 22 + i * 16;

            g.fill(p.x + 6, y, p.x + p.w - 6, y + 12, 0xFF222222);
        }
    }
}
