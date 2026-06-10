package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class EquipmentPanelRenderer {

    private final Minecraft mc = Minecraft.getInstance();

    private int left;
    private int top;

    private static final ResourceLocation SILHOUETTE =
            new ResourceLocation("tarkovinventory", "textures/ui/silhouette.png");

    private static final int SIL_W = 128;
    private static final int SIL_H = 256;

    private final List<EquipmentSlot> slots = new ArrayList<>();

    public void init(int left, int top) {
        this.left = left;
        this.top = top;

        slots.clear();

        int x = left;
        int y = top;

        int s = 24;
        int vGap = 6;

        int col1 = x;
        int col2 = x + 70;
        int col3 = x + 150;

        // LEFT + RIGHT
        slots.add(new EquipmentSlot("BALACLAVA", col1, y, s));
        slots.add(new EquipmentSlot("HEAD", col3, y, s));

        slots.add(new EquipmentSlot("EAR", col1, y + (s + vGap), s));
        slots.add(new EquipmentSlot("FACE", col3, y + (s + vGap), s));

        slots.add(new EquipmentSlot("RIG", col1, y + (s + vGap) * 2, s));
        slots.add(new EquipmentSlot("CHEST", col3, y + (s + vGap) * 2, s));

        // CENTER COLUMN
        slots.add(new EquipmentSlot("PANTS", col2, y + (s + vGap) * 3, s));
        slots.add(new EquipmentSlot("KNEES", col2, y + (s + vGap) * 4, s));
        slots.add(new EquipmentSlot("BOOTS", col2, y + (s + vGap) * 5, s));

        // BACKPACK
        slots.add(new EquipmentSlot("BACKPACK", col1, y + (s + vGap) * 6, s));

        // ======================
        // WEAPONS FIXED (IMPORTANT)
        // ======================
        int wY = y + (s + vGap) * 7 + vGap;

        int weaponPadding = 6;
        int weaponWidth = (x + 260) - x - (weaponPadding * 2); // safe panel width
        int wx = x + weaponPadding;

        slots.add(new EquipmentSlot("PRIMARY", wx, wY, weaponWidth, s));
        slots.add(new EquipmentSlot("SECONDARY", wx, wY + s + vGap, weaponWidth, s));
    }

    public void render(GuiGraphics g) {

        int x = left;
        int y = top;

        int s = 24;

        int col1 = x;
        int col3 = x + 150;

        int panelWidth = (col3 + s) - col1;

        // ======================
        // SILHOUETTE CENTER FIXED
        // ======================
        int silX = col1 + (panelWidth / 2) - (SIL_W / 2);
        int silY = y + 10;

        g.blit(
                SILHOUETTE,
                silX,
                silY,
                0, 0,
                SIL_W,
                SIL_H,
                SIL_W,
                SIL_H
        );

        // ======================
        // MOUSE HOVER
        // ======================
        double mx = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        double my = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();

        for (EquipmentSlot slot : slots) {
            slot.hovered = slot.isMouseOver(mx, my);
        }

        // ======================
        // RENDER SLOTS
        // ======================
        for (EquipmentSlot slot : slots) {
            slot.render(g);
        }

        // ======================
        // LABELS
        // ======================
        for (EquipmentSlot slot : slots) {
            slot.renderLabel(g, mc);
        }
    }
}
