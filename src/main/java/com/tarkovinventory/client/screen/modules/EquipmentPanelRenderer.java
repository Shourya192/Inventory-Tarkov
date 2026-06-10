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
        int vGap = 4; // tightened to match grid style

        int col1 = x;
        int col2 = x + 70;
        int col3 = x + 150;

        int panelWidth = 190;

        // ======================
        // NORMAL SLOTS
        // ======================
        slots.add(new EquipmentSlot("BALACLAVA", col1, y, s));
        slots.add(new EquipmentSlot("HEAD", col3, y, s));

        slots.add(new EquipmentSlot("EAR", col1, y + (s + vGap), s));
        slots.add(new EquipmentSlot("FACE", col3, y + (s + vGap), s));

        slots.add(new EquipmentSlot("RIG", col1, y + (s + vGap) * 2, s));
        slots.add(new EquipmentSlot("CHEST", col3, y + (s + vGap) * 2, s));

        slots.add(new EquipmentSlot("PANTS", col2, y + (s + vGap) * 3, s));
        slots.add(new EquipmentSlot("KNEES", col2, y + (s + vGap) * 4, s));
        slots.add(new EquipmentSlot("BOOTS", col2, y + (s + vGap) * 5, s));

        slots.add(new EquipmentSlot("BACKPACK", col1, y + (s + vGap) * 6, s));

        // ======================
        // WEAPON SLOTS (FIXED PROPERLY)
        // ======================
        int weaponHeight = s + 6;

        int wY = y + (s + vGap) * 7 + 2;

        int weaponPadding = 4;
        int weaponWidth = panelWidth - (weaponPadding * 2);
        int wx = x + weaponPadding;

        slots.add(new EquipmentSlot("PRIMARY", wx, wY, weaponWidth, weaponHeight));
        slots.add(new EquipmentSlot("SECONDARY", wx, wY + weaponHeight + vGap, weaponWidth, weaponHeight));
    }

    public void render(GuiGraphics g) {

        int x = left;
        int y = top;

        int s = 24;

        int panelWidth = 190;

        // ======================
        // MATCH MIDDLE GRID HEIGHT EXACTLY
        // ======================
        int panelHeight = 16 * 18; // 288px (from your InventoryGridRenderer)

        // ======================
        // BACK PANEL (CLEAN EFT STYLE)
        // ======================
        g.fill(
                x - 10,
                y - 10,
                x + panelWidth + 10,
                y + panelHeight + 10,
                0x66000000
        );

        g.fill(
                x - 6,
                y - 6,
                x + panelWidth + 6,
                y + panelHeight + 6,
                0xFF101010
        );

        g.fill(
                x - 4,
                y - 4,
                x + panelWidth + 4,
                y + panelHeight + 4,
                0xFF1A1A1A
        );

        // subtle border
        g.fill(x - 4, y - 4, x + panelWidth + 4, y - 3, 0xFF2B2B2B);
        g.fill(x - 4, y - 4, x - 3, y + panelHeight + 4, 0xFF2B2B2B);
        g.fill(x + panelWidth + 3, y - 4, x + panelWidth + 4, y + panelHeight + 4, 0xFF2B2B2B);
        g.fill(x - 4, y + panelHeight + 3, x + panelWidth + 4, y + panelHeight + 4, 0xFF2B2B2B);

        // ======================
        // SILHOUETTE CENTERED
        // ======================
        int silX = x + (panelWidth / 2) - (SIL_W / 2);
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
        // HOVER CHECK
        // ======================
        double mx = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        double my = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();

        for (EquipmentSlot slot : slots) {
            slot.hovered = slot.isMouseOver(mx, my);
        }

        // ======================
        // RENDER
        // ======================
        for (EquipmentSlot slot : slots) {
            slot.render(g);
        }

        for (EquipmentSlot slot : slots) {
            slot.renderLabel(g, mc);
        }
    }
}
