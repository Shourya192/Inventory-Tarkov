package com.tarkovinventory.client.screen.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.*;

public class EquipmentPanelRenderer {

    private final Minecraft mc = Minecraft.getInstance();

    private int left;
    private int top;

    private static final ResourceLocation SILHOUETTE =
            new ResourceLocation("tarkovinventory", "textures/ui/silhouette.png");

    private static final int SIL_W = 128;
    private static final int SIL_H = 256;

    private final List<EquipmentSlot> slots = new ArrayList<>();
    private final Map<String, EquipmentSlot> slotMap = new HashMap<>();

    private ItemStack carriedItem = ItemStack.EMPTY;

    public void init(int left, int top) {

        this.left = left;
        this.top = top;

        slots.clear();
        slotMap.clear();

        int x = left;
        int y = top;

        int s = 24;
        int vGap = 4;

        int col1 = x;
        int col2 = x + 70;
        int col3 = x + 150;

        int panelWidth = 190;

        add("HEAD", EquipmentSlotType.HEAD, col3, y, s);
        add("BALACLAVA", EquipmentSlotType.FACE, col1, y, s);

        add("EAR", EquipmentSlotType.EAR, col1, y + (s + vGap), s);
        add("FACE", EquipmentSlotType.FACE, col3, y + (s + vGap), s);

        add("RIG", EquipmentSlotType.RIG, col1, y + (s + vGap) * 2, s);
        add("CHEST", EquipmentSlotType.ARMOR, col3, y + (s + vGap) * 2, s);

        add("PANTS", EquipmentSlotType.PANTS, col2, y + (s + vGap) * 3, s);
        add("KNEES", EquipmentSlotType.KNEE, col2, y + (s + vGap) * 4, s);
        add("BOOTS", EquipmentSlotType.BOOTS, col2, y + (s + vGap) * 5, s);

        add("BACKPACK", EquipmentSlotType.BACKPACK, col1, y + (s + vGap) * 6, s);

        int weaponHeight = s + 6;
        int wY = y + (s + vGap) * 7 + 2;

        add("PRIMARY", EquipmentSlotType.WEAPON, x + 4, wY, panelWidth - 8, weaponHeight);
        add("SECONDARY", EquipmentSlotType.WEAPON, x + 4, wY + weaponHeight + vGap, panelWidth - 8, weaponHeight);

    }

    private void add(String id, EquipmentSlotType type, int x, int y, int size) {
        add(id, type, x, y, size, size);
    }

    private void add(String id, EquipmentSlotType type, int x, int y, int w, int h) {
        EquipmentSlot slot = new EquipmentSlot(id, type, x, y, w, h);
        slots.add(slot);
        slotMap.put(id, slot);
    }

    private void giveTestItems() {

        if (slotMap.get("HEAD") != null)
            slotMap.get("HEAD").item = new ItemStack(Items.IRON_HELMET);

        if (slotMap.get("CHEST") != null)
            slotMap.get("CHEST").item = new ItemStack(Items.IRON_CHESTPLATE);

        if (slotMap.get("PRIMARY") != null)
            slotMap.get("PRIMARY").item = new ItemStack(Items.DIAMOND_SWORD);

        if (slotMap.get("SECONDARY") != null)
            slotMap.get("SECONDARY").item = new ItemStack(Items.BOW);
    }

    // ======================
    // CLICK
    // ======================
    public void updateHover(double mouseX, double mouseY) {
        for (EquipmentSlot slot : slots) slot.hovered = slot.isMouseOver(mouseX, mouseY);
    }

    public EquipmentSlot getSlot(String id) {
        return slotMap.get(id);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        if (button != 0) return false;

        double mx = mouseX;
        double my = mouseY;

        for (EquipmentSlot slot : slots) {

            if (slot.isMouseOver(mx, my)) {

                if (carriedItem.isEmpty() && !slot.item.isEmpty()) {
                    carriedItem = slot.item;
                    slot.item = ItemStack.EMPTY;
                    return true;
                }

                if (!carriedItem.isEmpty()) {
                    ItemStack temp = slot.item;
                    slot.item = carriedItem;
                    carriedItem = temp;
                    return true;
                }
            }
        }

        return false;
    }

    // 🔥 FIXED BUILD ERROR (ADDED THIS)
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    // ======================
    // RENDER
    // ======================
    public void render(GuiGraphics g) {
        render(g, true);
    }

    public void render(GuiGraphics g, boolean renderLocalItems) {

        int x = left;
        int y = top;

        int panelWidth = 190;
        int panelHeight = 16 * 18;

        g.fill(x - 10, y - 10, x + panelWidth + 10, y + panelHeight + 10, 0x66000000);
        g.fill(x - 6, y - 6, x + panelWidth + 6, y + panelHeight + 6, 0xFF101010);
        g.fill(x - 4, y - 4, x + panelWidth + 4, y + panelHeight + 4, 0xFF1A1A1A);

        int silX = x + (panelWidth / 2) - (SIL_W / 2);
        int silY = y + 10;

        g.blit(SILHOUETTE, silX, silY, 0, 0, SIL_W, SIL_H, SIL_W, SIL_H);

        for (EquipmentSlot slot : slots) {
            slot.render(g, renderLocalItems);
        }

        for (EquipmentSlot slot : slots) {
            slot.renderLabel(g, mc);
        }

        if (renderLocalItems && !carriedItem.isEmpty()) {
            int cx = (int) (mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth());
            int cy = (int) (mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / mc.getWindow().getScreenHeight());

            g.renderItem(carriedItem, cx + 8, cy + 8);
            g.renderItemDecorations(mc.font, carriedItem, cx + 8, cy + 8);
        }
    }
}
