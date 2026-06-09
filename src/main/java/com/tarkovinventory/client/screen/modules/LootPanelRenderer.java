package com.tarkovinventory.client.screen.modules;

import com.tarkovinventory.client.screen.layout.Panel;
import net.minecraft.client.gui.GuiGraphics;

public class LootPanelRenderer {

    public void render(GuiGraphics g, Panel p) {

        p.drawBase(g);

        for (int i = 0; i < 12; i++) {
            g.fill(
                    p.x + 6,
                    p.y + 22 + i * 16,
                    p.x + p.w - 6,
                    p.y + 34 + i * 16,
                    0xFF2A2A2A
            );
        }
    }
}
