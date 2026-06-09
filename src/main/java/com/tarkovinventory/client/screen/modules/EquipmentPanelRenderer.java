package com.tarkovinventory.client.screen.modules;

import com.tarkovinventory.client.screen.layout.Panel;
import net.minecraft.client.gui.GuiGraphics;

public class EquipmentPanelRenderer {

    public void render(GuiGraphics g, Panel p) {

        p.drawBase(g);

        int x = p.x + 8;
        int y = p.y + 25;

        g.fill(x, y, x + 26, y + 26, 0xFF1A1A1A); // head
        g.fill(x + 40, y, x + 70, y + 30, 0xFF1A1A1A); // chest
        g.fill(x + 90, y, x + 116, y + 26, 0xFF1A1A1A); // legs
        g.fill(x + 130, y, x + 156, y + 26, 0xFF1A1A1A); // feet
    }
}
