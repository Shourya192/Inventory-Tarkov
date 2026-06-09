package com.tarkovinventory.client.screen.modules;

import com.tarkovinventory.client.screen.layout.Panel;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Loot container UI.
 */
public class LootPanelRenderer {

    public void render(GuiGraphics g, Panel p) {

        p.draw(g);

        for (int i = 0; i < 12; i++) {

            int yOff = p.y + 22 + i * 16;

            g.fill(
                    p.x + 6,
                    yOff,
                    p.x + p.w - 6,
                    yOff + 12,
                    0xFF2A2A2A
            );
        }
    }
}
