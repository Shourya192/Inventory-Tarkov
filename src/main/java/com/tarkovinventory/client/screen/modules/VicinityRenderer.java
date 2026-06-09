package com.tarkovinventory.client.screen.modules;

import com.tarkovinventory.client.screen.layout.Panel;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Ground items / nearby loot list.
 */
public class VicinityRenderer {

    public void render(GuiGraphics g, Panel p) {

        p.draw(g);

        for (int i = 0; i < 10; i++) {

            int yOff = p.y + 22 + i * 18;

            g.fill(
                    p.x + 6,
                    yOff,
                    p.x + p.w - 6,
                    yOff + 14,
                    0xFF1A1A1A
            );
        }
    }
}
