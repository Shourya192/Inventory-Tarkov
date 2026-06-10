package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.gui.GuiGraphics;

public class EquipmentSlot {

    public final String name;

    public int x1, y1, x2, y2;

    public boolean hovered;

    public EquipmentSlot(String name, int x1, int y1, int size) {
        this.name = name;

        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x1 + size;
        this.y2 = y1 + size;
    }

    public void render(GuiGraphics g) {

        int bg = hovered ? 0xFF1F1F1F : 0xFF161616;
        int b1 = hovered ? 0xFF555555 : 0xFF2A2A2A;
        int b2 = hovered ? 0xFF777777 : 0xFF3A3A3A;

        g.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF000000);
        g.fill(x1, y1, x2, y2, bg);

        g.fill(x1, y1, x1 + 1, y2, b1);
        g.fill(x1, y1, x2, y1 + 1, b2);

        g.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xFF101010);
    }

    public boolean isMouseOver(double mx, double my) {
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
    }
}
