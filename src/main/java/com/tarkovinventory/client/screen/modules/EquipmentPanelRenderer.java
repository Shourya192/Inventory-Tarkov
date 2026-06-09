package com.tarkovinventory.client.screen.modules;

import com.tarkovinventory.client.screen.layout.Panel;
import net.minecraft.client.gui.GuiGraphics;

public class EquipmentPanelRenderer {

    public void render(GuiGraphics g, Panel p) {

        p.draw(g);

        int x = p.x + 10;
        int y = p.y + 25;

        g.fill(x, y, x + 26, y + 26, 0xFF2A2A2A);
        g.fill(x + 40, y, x + 70, y + 30, 0xFF2A2A2A);
        g.fill(x + 90, y, x + 116, y + 26, 0xFF2A2A2A);
        g.fill(x + 130, y, x + 156, y + 26, 0xFF2A2A2A);

        g.fill(x, y + 50, x + 40, y + 90, 0xFF2A2A2A);
        g.fill(x + 60, y + 50, x + 100, y + 90, 0xFF2A2A2A);
    }
}
