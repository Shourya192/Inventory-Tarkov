package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class EquipmentPanelRenderer {

    private final Minecraft mc = Minecraft.getInstance();

    private int left;
    private int top;

    private static final ResourceLocation SILHOUETTE =
            new ResourceLocation("tarkovinventory", "textures/ui/silhouette.png");

    private static final int SIL_W = 128;
    private static final int SIL_H = 256;

    // SYSTEMS
    private final List<EquipmentSlot> slots = new ArrayList<>();
    private final Map<String, EquipmentSlot> slotMap = new HashMap<>();
    private final Map<Integer, EquipmentSlot> hotbar = new HashMap<>();

    private EquipmentSlot activeWeapon = null;

    // ======================
    // INIT
    // ======================
    public void init(int left, int top) {

        this.left = left;
        this.top = top;

        slots.clear();
        slotMap.clear();
        hotbar.clear();

        int x = left;
        int y = top;

        int s = 24;
        int vGap = 4;

        int col1 = x;
        int col2 = x + 70;
        int col3 = x + 150;

        int panelWidth = 190;

        // ======================
        // SLOTS (LINKED + TYPED)
        // ======================
        addSlot("BALACLAVA", EquipmentSlotType.FACE, col1, y, s);
        addSlot("HEAD", EquipmentSlotType.HEAD, col3, y, s);

        addSlot("EAR", EquipmentSlotType.EAR, col1, y + (s + vGap), s);
        addSlot("FACE", EquipmentSlotType.EYES, col3, y + (s + vGap), s);

        addSlot("RIG", EquipmentSlotType.RIG, col1, y + (s + vGap) * 2, s);
        addSlot("CHEST", EquipmentSlotType.ARMOR, col3, y + (s + vGap) * 2, s);

        addSlot("PANTS", EquipmentSlotType.PANTS, col2, y + (s + vGap) * 3, s);
        addSlot("KNEES", EquipmentSlotType.KNEE, col2, y + (s + vGap) * 4, s);
        addSlot("BOOTS", EquipmentSlotType.BOOTS, col2, y + (s + vGap) * 5, s);

        addSlot("BACKPACK", EquipmentSlotType.BACKPACK, col1, y + (s + vGap) * 6, s);

        // ======================
        // WEAPONS
        // ======================
        int weaponHeight = s + 6;
        int wY = y + (s + vGap) * 7 + 2;

        int weaponWidth = panelWidth - 8;

        addSlot("PRIMARY", EquipmentSlotType.WEAPON, x + 4, wY, weaponWidth, weaponHeight);
        addSlot("SECONDARY", EquipmentSlotType.WEAPON, x + 4, wY + weaponHeight + vGap, weaponWidth, weaponHeight);

        // ======================
        // HOTBAR LINKING
        // ======================
        hotbar.put(1, slotMap.get("PRIMARY"));
        hotbar.put(2, slotMap.get("SECONDARY"));
    }

    // helper
    private void addSlot(String id, EquipmentSlotType type, int x, int y, int size) {
        addSlot(id, type, x, y, size, size);
    }

    private void addSlot(String id, EquipmentSlotType type, int x, int y, int w, int h) {
        EquipmentSlot slot = new EquipmentSlot(id, type, x, y, w, h);
        slots.add(slot);
        slotMap.put(id, slot);
    }

    // ======================
    // HOTBAR INPUT
    // ======================
    public void handleKey(int key) {

        int slot = key - 48; // 1-9 keys

        if (hotbar.containsKey(slot)) {

            EquipmentSlot target = hotbar.get(slot);

            if (target != null) {
                activeWeapon = target;
            }
        }
    }

    // ======================
    // RENDER
    // ======================
    public void render(GuiGraphics g) {

        int x = left;
        int y = top;

        int panelWidth = 190;
        int panelHeight = 16 * 18;

        // BACK PANEL
        g.fill(x - 10, y - 10, x + panelWidth + 10, y + panelHeight + 10, 0x66000000);
        g.fill(x - 6, y - 6, x + panelWidth + 6, y + panelHeight + 6, 0xFF101010);
        g.fill(x - 4, y - 4, x + panelWidth + 4, y + panelHeight + 4, 0xFF1A1A1A);

        // SILHOUETTE
        int silX = x + (panelWidth / 2) - (SIL_W / 2);
        int silY = y + 10;

        g.blit(SILHOUETTE, silX, silY, 0, 0, SIL_W, SIL_H, SIL_W, SIL_H);

        // MOUSE
        double mx = mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        double my = mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight();

        for (EquipmentSlot slot : slots) {
            slot.hovered = slot.isMouseOver(mx, my);
        }

        // ACTIVE WEAPON HIGHLIGHT
        for (EquipmentSlot slot : slots) {

            if (slot == activeWeapon) {
                g.fill(slot.x1 - 2, slot.y1 - 2, slot.x2 + 2, slot.y2 + 2, 0x55FFD700);
            }

            slot.render(g);
        }

        for (EquipmentSlot slot : slots) {
            slot.renderLabel(g, mc);
        }
    }
}
