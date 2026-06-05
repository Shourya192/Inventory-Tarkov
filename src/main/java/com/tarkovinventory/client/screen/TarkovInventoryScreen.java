package com.tarkovinventory.client.screen;

import com.tarkovinventory.capability.IPlayerEquipment;
import com.tarkovinventory.capability.ModCapabilities;
import com.tarkovinventory.compat.BackpackCompat;
import com.tarkovinventory.compat.CuriosCompat;
import com.tarkovinventory.container.TarkovInventoryMenu;
import com.tarkovinventory.inventory.GridInventory;
import com.tarkovinventory.inventory.GridItemSizes;
import com.tarkovinventory.inventory.GridSize;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Full Tarkov-style character inventory screen.
 *
 * Left panel  : character silhouette + labeled equipment slots + stats bar
 * Right panel : POCKETS / BACKPACK grid (with search) / POUCH
 * Bottom strip: player inventory + hotbar
 */
public class TarkovInventoryScreen extends AbstractContainerScreen<TarkovInventoryMenu> {

    // ── Layout constants ──────────────────────────────────────────────
    private static final int PAD           = 7;
    private static final int CHAR_PANEL_W  = 190;
    private static final int CHAR_PANEL_H  = 260;
    private static final int CONT_PANEL_W  = 218;
    private static final int CONT_PANEL_H  = CHAR_PANEL_H;
    private static final int PLAYER_INV_H  = 3 * 18 + 4 + 18;
    private static final int TOTAL_W       = PAD + CHAR_PANEL_W + PAD + CONT_PANEL_W + PAD;
    private static final int TOTAL_H       = PAD + CHAR_PANEL_H + PAD + PLAYER_INV_H + PAD;

    // ── Grid ─────────────────────────────────────────────────────────
    private static final int CELL      = 17;
    private static final int GRID_COLS = GridInventory.COLS;
    private static final int GRID_ROWS = GridInventory.ROWS;
    private static final int GRID_W    = GRID_COLS * CELL;
    private static final int GRID_H    = GRID_ROWS * CELL;

    // ── Small-slot size ───────────────────────────────────────────────
    private static final int SS = 26;

    // ── Colours ───────────────────────────────────────────────────────
    private static final int C_BG_DARK       = 0xFF111111;
    private static final int C_BG_PANEL      = 0xFF181818;
    private static final int C_BG_SECTION    = 0xFF1E1E1E;
    private static final int C_BORDER        = 0xFF3A3A3A;
    private static final int C_GRID_EMPTY    = 0xFF252525;
    private static final int C_GRID_LINE     = 0xFF303030;
    private static final int C_HOVER         = 0x40607060;
    private static final int C_DRAG_VALID    = 0x7044AA44;
    private static final int C_DRAG_INVALID  = 0x70AA4444;
    private static final int C_ITEM_BG       = 0xFF1E3524;
    private static final int C_ITEM_BORDER   = 0xFF4CAF50;
    private static final int C_SLOT_EMPTY    = 0xFF242424;
    private static final int C_SLOT_BORDER   = 0xFF444444;
    private static final int C_TEXT_LABEL    = 0xFF8A8A7A;
    private static final int C_TEXT_TITLE    = 0xFFD4C89A;
    private static final int C_TEXT_WHITE    = 0xFFE0E0E0;
    private static final int C_SEARCH_BG     = 0xFF1A2A1A;
    private static final int C_HEALTH        = 0xFF4CAF50;
    private static final int C_HYDRATION     = 0xFF2196F3;
    private static final int C_ENERGY        = 0xFFFFC107;
    private static final int C_WEIGHT        = 0xFFBBBBBB;
    private static final int C_CURIOS_BDR    = 0xFF9C6BD6;
    private static final int C_CURIOS_BG     = 0xFF2A1A3A;
    private static final int C_HOTBAR_SEP    = 0xFF3A3A3A;
    private static final int C_HIGHLIGHT     = 0x60FFFFFF;

    // ── Equipment slot model ──────────────────────────────────────────
    private record EqSlotDef(String label, int x, int y, int w, int h,
                              EqSource source, int sourceIdx) {}
    private enum EqSource { ARMOR, CAP }

    private List<EqSlotDef> eqSlots = new ArrayList<>();

    // ── Drag state ────────────────────────────────────────────────────
    private ItemStack dragging     = ItemStack.EMPTY;
    private GridSize  draggingSize = GridSize.ONE_BY_ONE;
    private int       dragOffX, dragOffY;
    private static final int POCKET_TAG = 0x1000;
    private static final int POUCH_TAG  = 0x2000;
    private static final int EQ_TAG     = 0x4000;

    // ── Search ────────────────────────────────────────────────────────
    private boolean searchActive = false;
    private String  searchText   = "";

    // ── Hover ─────────────────────────────────────────────────────────
    private int hoverGridCol = -1, hoverGridRow = -1;
    private int tooltipSlot  = -1;

    // ── Curios ───────────────────────────────────────────────────────
    private List<CuriosCompat.CuriosSlotEntry> curiosSlots = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────

    public TarkovInventoryScreen(TarkovInventoryMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth  = TOTAL_W;
        this.imageHeight = TOTAL_H;
    }

    @Override
    protected void init() {
        super.init();
        buildEquipmentSlots();
        refreshCuriosSlots();
    }

    // ================================================================
    // Coordinate helpers
    // ================================================================

    private int charX()       { return leftPos + PAD; }
    private int panelY()      { return topPos  + PAD; }
    private int contX()       { return leftPos + PAD + CHAR_PANEL_W + PAD; }
    private int playerY()     { return topPos  + PAD + CHAR_PANEL_H + PAD; }
    private int gridOriginX() { return contX() + 2; }
    private int gridOriginY() { return panelY() + 68; }

    private int toGridCol(double px) {
        int rel = (int) px - gridOriginX();
        return (rel >= 0 && rel < GRID_W) ? rel / CELL : -1;
    }
    private int toGridRow(double py) {
        int rel = (int) py - gridOriginY();
        return (rel >= 0 && rel < GRID_H) ? rel / CELL : -1;
    }

    // ================================================================
    // Equipment slot layout
    // ================================================================

    private void buildEquipmentSlots() {
        eqSlots = new ArrayList<>();
        int ox = charX() + 4;
        int oy = panelY() + 14;

        // Row 1
        eqSlots.add(new EqSlotDef("EARPIECE",   ox,       oy,       SS,    SS,    EqSource.CAP,   IPlayerEquipment.SLOT_EARPIECE));
        eqSlots.add(new EqSlotDef("HEADWEAR",   ox + 38,  oy,       SS+8,  SS+8,  EqSource.ARMOR, 0));
        eqSlots.add(new EqSlotDef("FACE COVER", ox + 90,  oy,       SS,    SS,    EqSource.CAP,   -1));

        // Row 2
        eqSlots.add(new EqSlotDef("ARMBAND",    ox,       oy + 55,  SS,    SS,    EqSource.CAP,   IPlayerEquipment.SLOT_ARMBAND));
        eqSlots.add(new EqSlotDef("BODY ARMOR", ox + 38,  oy + 55,  SS+8,  SS+14, EqSource.ARMOR, 1));
        eqSlots.add(new EqSlotDef("EYEWEAR",    ox + 90,  oy + 55,  SS,    SS,    EqSource.CAP,   -1));

        // Row 3
        eqSlots.add(new EqSlotDef("ON SLING",   ox,       oy + 120, SS+8,  SS+20, EqSource.CAP,   IPlayerEquipment.SLOT_ON_SLING));
        eqSlots.add(new EqSlotDef("HOLSTER",    ox + 90,  oy + 120, SS+8,  SS+8,  EqSource.CAP,   IPlayerEquipment.SLOT_HOLSTER));

        // Row 4
        eqSlots.add(new EqSlotDef("ON BACK",    ox,       oy + 175, SS+8,  SS+20, EqSource.CAP,   IPlayerEquipment.SLOT_ON_BACK));
        eqSlots.add(new EqSlotDef("SCABBARD",   ox + 90,  oy + 175, SS+8,  SS,    EqSource.CAP,   IPlayerEquipment.SLOT_SCABBARD));
    }

    private void refreshCuriosSlots() {
        if (CuriosCompat.isLoaded() && minecraft != null && minecraft.player != null) {
            curiosSlots = CuriosCompat.getEquippedSlots(minecraft.player);
        }
    }

    // ================================================================
    // Render
    // ================================================================

    @Override
    public void render(@NotNull GuiGraphics gfx, int mx, int my, float pt) {
        renderBackground(gfx);
        renderBg(gfx, pt, mx, my);
        renderCharacterPanel(gfx, mx, my);
        renderContainersPanel(gfx, mx, my);
        renderPlayerInv(gfx, mx, my);
        renderDragging(gfx, mx, my);
        renderHoveredTooltip(gfx, mx, my);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics gfx, float pt, int mx, int my) {
        gfx.fill(leftPos, topPos, leftPos + TOTAL_W, topPos + TOTAL_H, C_BG_DARK);
        // Title bar
        gfx.fill(leftPos, topPos, leftPos + TOTAL_W, topPos + 12, 0xFF0E0E0E);
        gfx.drawString(font, "TARKOV INVENTORY", leftPos + 4, topPos + 2, C_TEXT_TITLE, false);
        String esc = "[ ESC ] Close";
        gfx.drawString(font, esc, leftPos + TOTAL_W - font.width(esc) - 4, topPos + 2, C_TEXT_LABEL, false);
        // Panel divider
        gfx.fill(contX() - PAD / 2, panelY(), contX() - PAD / 2 + 1, panelY() + CHAR_PANEL_H, C_BORDER);
    }

    // ── Character panel ───────────────────────────────────────────────

    private void renderCharacterPanel(@NotNull GuiGraphics gfx, int mx, int my) {
        int ox = charX(), oy = panelY();
        gfx.fill(ox, oy, ox + CHAR_PANEL_W, oy + CHAR_PANEL_H, C_BG_PANEL);
        drawBorder(gfx, ox, oy, CHAR_PANEL_W, CHAR_PANEL_H, C_BORDER);

        Player player = minecraft.player;
        for (int i = 0; i < eqSlots.size(); i++)
            renderEquipmentSlot(gfx, i, player, mx, my);

        // Curios row
        if (CuriosCompat.isLoaded() && !curiosSlots.isEmpty()) {
            int cy = oy + CHAR_PANEL_H - 42;
            gfx.drawString(font, "CURIOS", ox + 4, cy - 10, C_CURIOS_BDR, false);
            int shown = 0;
            for (CuriosCompat.CuriosSlotEntry e : curiosSlots) {
                if (shown >= 6) break;
                renderSmallSlot(gfx, ox + 4 + shown * (SS + 2), cy, SS, SS, e.stack(), C_CURIOS_BG, C_CURIOS_BDR, mx, my);
                shown++;
            }
        }

        renderStatsBar(gfx, ox, oy + CHAR_PANEL_H - (CuriosCompat.isLoaded() && !curiosSlots.isEmpty() ? 62 : 18));
    }

    private void renderEquipmentSlot(@NotNull GuiGraphics gfx, int idx, Player player, int mx, int my) {
        EqSlotDef def   = eqSlots.get(idx);
        ItemStack stack = getEquipmentStack(player, def);
        boolean filled  = !stack.isEmpty();

        gfx.fill(def.x(), def.y(), def.x() + def.w(), def.y() + def.h(), filled ? C_ITEM_BG : C_SLOT_EMPTY);
        drawBorder(gfx, def.x(), def.y(), def.w(), def.h(), filled ? C_ITEM_BORDER : C_SLOT_BORDER);
        gfx.drawString(font, def.label(), def.x(), def.y() - 8, C_TEXT_LABEL, false);

        if (filled) {
            int ix = def.x() + (def.w() - 16) / 2;
            int iy = def.y() + (def.h() - 16) / 2;
            gfx.renderItem(stack, ix, iy);
            gfx.renderItemDecorations(font, stack, ix, iy);

            if (stack.isDamageableItem()) {
                float dur = 1f - (float) stack.getDamageValue() / stack.getMaxDamage();
                int bw = def.w() - 4, fill = (int)(bw * dur), by = def.y() + def.h() - 3;
                gfx.fill(def.x() + 2, by, def.x() + 2 + bw, by + 2, 0xFF333333);
                int bc = dur > 0.6f ? 0xFF4CAF50 : dur > 0.3f ? 0xFFFFC107 : 0xFFFF4444;
                gfx.fill(def.x() + 2, by, def.x() + 2 + fill, by + 2, bc);
            }

            if (BackpackCompat.isExternalBackpack(stack)) {
                String lbl = BackpackCompat.getExternalLabel(stack);
                if (lbl != null)
                    gfx.drawString(font, "§6" + lbl, def.x() + 2, def.y() + def.h() + 1, 0xFFFFAA00, false);
            }
        } else {
            int cx = def.x() + def.w() / 2, cy = def.y() + def.h() / 2;
            gfx.fill(cx - 6, cy - 1, cx + 6, cy + 1, 0xFF303030);
            gfx.fill(cx - 1, cy - 6, cx + 1, cy + 6, 0xFF303030);
        }

        if (mx >= def.x() && mx < def.x() + def.w() && my >= def.y() && my < def.y() + def.h())
            gfx.fill(def.x(), def.y(), def.x() + def.w(), def.y() + def.h(), C_HOVER);
    }

    private void renderStatsBar(@NotNull GuiGraphics gfx, int ox, int oy) {
        Player p = minecraft.player;
        if (p == null) return;
        float hp  = p.getHealth(), maxHp = p.getMaxHealth();
        float food = p.getFoodData().getFoodLevel();
        float sat  = p.getFoodData().getSaturationLevel();
        int items = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++)
            if (!p.getInventory().getItem(i).isEmpty()) items++;

        gfx.drawString(font, items + " KG", ox + 4,  oy,      C_WEIGHT,    false);
        gfx.drawString(font, (int)hp + "/" + (int)maxHp, ox + 4, oy + 10, C_HEALTH, false);
        gfx.drawString(font, (int)food + "/100",  ox + 70, oy,      C_ENERGY,    false);
        gfx.drawString(font, (int)(sat*10)/10f + "", ox + 70, oy + 10, C_HYDRATION, false);
    }

    // ── Containers panel ──────────────────────────────────────────────

    private void renderContainersPanel(@NotNull GuiGraphics gfx, int mx, int my) {
        int ox = contX(), oy = panelY();
        gfx.fill(ox, oy, ox + CONT_PANEL_W, oy + CONT_PANEL_H, C_BG_PANEL);
        drawBorder(gfx, ox, oy, CONT_PANEL_W, CONT_PANEL_H, C_BORDER);

        renderPocketsSection(gfx, ox + 2, oy + 2,  mx, my);
        renderBackpackSection(gfx, ox + 2, oy + 42, mx, my);
        renderPouchSection(gfx,    ox + 2, oy + CONT_PANEL_H - 44, mx, my);
    }

    private void renderPocketsSection(@NotNull GuiGraphics gfx, int ox, int oy, int mx, int my) {
        gfx.drawString(font, "POCKETS", ox, oy, C_TEXT_TITLE, false);
        for (int i = 0; i < TarkovInventoryMenu.POCKETS_COUNT; i++)
            renderSmallSlot(gfx, ox + i * (SS + 3), oy + 10, SS, SS, menu.getPocketSlot(i), C_SLOT_EMPTY, C_SLOT_BORDER, mx, my);
    }

    private boolean hasBackpackEquipped() {
        if (minecraft.player == null) return false;
        return ModCapabilities.get(minecraft.player)
                .map(cap -> cap.getSlot(IPlayerEquipment.SLOT_ON_BACK).getItem()
                            instanceof com.tarkovinventory.item.TarkovBackpackItem)
                .orElse(false);
    }

    private void renderBackpackSection(@NotNull GuiGraphics gfx, int ox, int oy, int mx, int my) {
        gfx.drawString(font, "BACKPACK", ox, oy, C_TEXT_TITLE, false);

        int areaX = ox, areaY = oy + 10;
        int areaW = CONT_PANEL_W - 4, areaH = CONT_PANEL_H - 44 - 10 - areaY + panelY() + 42;

        if (!hasBackpackEquipped()) {
            // No backpack in ON BACK slot — show locked placeholder
            int midX = areaX + areaW / 2, midY = areaY + areaH / 2;
            gfx.fill(areaX, areaY, areaX + areaW, areaY + areaH, 0xFF161616);
            drawBorder(gfx, areaX, areaY, areaW, areaH, 0xFF2A2A2A);
            // Draw big cross
            gfx.fill(midX - 12, midY - 2, midX + 12, midY + 2, 0xFF2A2A2A);
            gfx.fill(midX - 2, midY - 12, midX + 2, midY + 12, 0xFF2A2A2A);
            String msg1 = "NO BACKPACK";
            String msg2 = "Equip one in ON BACK slot";
            gfx.drawString(font, msg1, midX - font.width(msg1) / 2, midY + 16, 0xFF555555, false);
            gfx.drawString(font, msg2, midX - font.width(msg2) / 2, midY + 26, 0xFF444444, false);
            hoverGridCol = -1; hoverGridRow = -1; tooltipSlot = -1;
            return;
        }

        // Backpack equipped — show search bar + grid
        int sbX = ox + font.width("BACKPACK") + 6;
        int sbW = CONT_PANEL_W - (sbX - contX()) - 4;
        gfx.fill(sbX, oy, sbX + sbW, oy + 10, searchActive ? C_SEARCH_BG : 0xFF1A1A1A);
        drawBorder(gfx, sbX, oy, sbW, 10, searchActive ? 0xFF4CAF50 : C_BORDER);
        String display = (searchText.isEmpty() && !searchActive) ? "SEARCH" : searchText;
        int tc = (searchText.isEmpty() && !searchActive) ? C_TEXT_LABEL : C_TEXT_WHITE;
        gfx.drawString(font, display + (searchActive ? "_" : ""), sbX + 3, oy + 1, tc, false);

        renderGrid(gfx, mx, my);
    }

    private void renderGrid(@NotNull GuiGraphics gfx, int mx, int my) {
        GridInventory inv = menu.getGridInventory();
        int ox = gridOriginX(), oy = gridOriginY();
        hoverGridCol = toGridCol(mx);
        hoverGridRow = toGridRow(my);
        tooltipSlot  = -1;

        // Grid background — 1 fill instead of 360 per-cell fills
        gfx.fill(ox, oy, ox + GRID_W, oy + GRID_H, C_GRID_EMPTY);
        // Vertical separator lines (GRID_COLS-1 calls)
        for (int col = 1; col < GRID_COLS; col++) {
            int lx = ox + col * CELL - 1;
            gfx.fill(lx, oy, lx + 1, oy + GRID_H, C_GRID_LINE);
        }
        // Horizontal separator lines (GRID_ROWS-1 calls)
        for (int row = 1; row < GRID_ROWS; row++) {
            int ly = oy + row * CELL - 1;
            gfx.fill(ox, ly, ox + GRID_W, ly + 1, C_GRID_LINE);
        }

        // Placed items
        for (int i = 0; i < GridInventory.TOTAL_CELLS; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            int gx = inv.getSlotX(i), gy = inv.getSlotY(i);
            GridSize sz = inv.getSlotSize(i);
            int pw = sz.width() * CELL - 1, ph = sz.height() * CELL - 1;
            int px = ox + gx * CELL,        py = oy + gy * CELL;
            boolean dimmed = isSearchDimmed(stack);

            gfx.fill(px, py, px + pw, py + ph, dimmed ? 0xFF1A1A1A : C_ITEM_BG);
            drawBorder(gfx, px, py, pw, ph, dimmed ? 0xFF2A2A2A : C_ITEM_BORDER);

            if (!dimmed) {
                int ix = px + (pw - 16) / 2, iy = py + (ph - 16) / 2;
                gfx.renderItem(stack, ix, iy);
                gfx.renderItemDecorations(font, stack, ix, iy);
                // Item name
                gfx.drawString(font, shortenName(stack.getHoverName().getString(), sz.width()), px + 1, py + 1, C_TEXT_LABEL, false);
                // Durability bar
                if (stack.isDamageableItem()) {
                    float dur = 1f - (float)stack.getDamageValue() / stack.getMaxDamage();
                    int bw = pw - 2, bFill = (int)(bw * dur), by = py + ph - 3;
                    gfx.fill(px + 1, by, px + 1 + bw, by + 2, 0xFF222222);
                    int bc = dur > 0.6f ? 0xFF4CAF50 : dur > 0.3f ? 0xFFFFC107 : 0xFFFF4444;
                    gfx.fill(px + 1, by, px + 1 + bFill, by + 2, bc);
                }
            }

            if (!dimmed && hoverGridCol >= gx && hoverGridCol < gx + sz.width()
                        && hoverGridRow >= gy && hoverGridRow < gy + sz.height())
                tooltipSlot = i;
        }

        // Hover tint on empty cell
        if (hoverGridCol >= 0 && hoverGridRow >= 0 && dragging.isEmpty() && tooltipSlot < 0) {
            int px = ox + hoverGridCol * CELL, py = oy + hoverGridRow * CELL;
            gfx.fill(px, py, px + CELL - 1, py + CELL - 1, C_HOVER);
        }

        // Drag placement preview
        if (!dragging.isEmpty() && hoverGridCol >= 0 && hoverGridRow >= 0) {
            boolean fits = inv.canPlace(hoverGridCol, hoverGridRow, draggingSize);
            int pw = draggingSize.width() * CELL - 1, ph = draggingSize.height() * CELL - 1;
            gfx.fill(ox + hoverGridCol * CELL, oy + hoverGridRow * CELL,
                     ox + hoverGridCol * CELL + pw, oy + hoverGridRow * CELL + ph,
                     fits ? C_DRAG_VALID : C_DRAG_INVALID);
        }
    }

    private void renderPouchSection(@NotNull GuiGraphics gfx, int ox, int oy, int mx, int my) {
        gfx.drawString(font, "POUCH", ox, oy, C_TEXT_TITLE, false);
        for (int i = 0; i < TarkovInventoryMenu.POUCH_COUNT; i++)
            renderSmallSlot(gfx, ox + i * (SS + 3), oy + 10, SS, SS, menu.getPouchSlot(i), C_SLOT_EMPTY, C_SLOT_BORDER, mx, my);
    }

    // ── Player inventory ──────────────────────────────────────────────

    private void renderPlayerInv(@NotNull GuiGraphics gfx, int mx, int my) {
        int ox = leftPos + PAD, oy = playerY();
        gfx.fill(ox - 2, oy - 2, ox + TOTAL_W - PAD * 2 + 2, oy + PLAYER_INV_H + 2, C_BG_SECTION);
        drawBorder(gfx, ox - 2, oy - 2, TOTAL_W - PAD * 2 + 4, PLAYER_INV_H + 4, C_BORDER);

        Inventory inv = minecraft.player.getInventory();
        int gridW = 9 * 18, gridH = 3 * 18;

        // Main inv — one background fill + grid lines instead of 27 × drawSlotBg
        gfx.fill(ox, oy, ox + gridW, oy + gridH, 0xFF2A2A2A);
        for (int col = 1; col < 9; col++) gfx.fill(ox + col * 18, oy, ox + col * 18 + 1, oy + gridH, C_SLOT_BORDER);
        for (int row = 1; row < 3; row++) gfx.fill(ox, oy + row * 18, ox + gridW, oy + row * 18 + 1, C_SLOT_BORDER);
        drawBorder(gfx, ox, oy, gridW, gridH, C_SLOT_BORDER);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int sx = ox + col * 18, sy = oy + row * 18;
                ItemStack s = inv.getItem(col + row * 9 + 9);
                if (!s.isEmpty()) { gfx.renderItem(s, sx + 1, sy + 1); gfx.renderItemDecorations(font, s, sx + 1, sy + 1); }
                if (mx >= sx && mx < sx + 18 && my >= sy && my < sy + 18)
                    gfx.fill(sx + 1, sy + 1, sx + 17, sy + 17, C_HIGHLIGHT);
            }
        }

        int sepY = oy + gridH + 3;
        gfx.fill(ox, sepY, ox + gridW, sepY + 1, C_HOTBAR_SEP);

        // Hotbar — one background fill + grid lines
        int hotbarY = sepY + 2;
        gfx.fill(ox, hotbarY, ox + gridW, hotbarY + 18, 0xFF2A2A2A);
        for (int col = 1; col < 9; col++) gfx.fill(ox + col * 18, hotbarY, ox + col * 18 + 1, hotbarY + 18, C_SLOT_BORDER);
        drawBorder(gfx, ox, hotbarY, gridW, 18, C_SLOT_BORDER);

        for (int col = 0; col < 9; col++) {
            int sx = ox + col * 18, sy = hotbarY;
            if (col == inv.selected) gfx.fill(sx, sy, sx + 18, sy + 18, 0x40FFFF00);
            ItemStack s = inv.getItem(col);
            if (!s.isEmpty()) { gfx.renderItem(s, sx + 1, sy + 1); gfx.renderItemDecorations(font, s, sx + 1, sy + 1); }
            gfx.drawString(font, String.valueOf(col + 1), sx + 1, sy + 1, C_TEXT_LABEL, false);
            if (mx >= sx && mx < sx + 18 && my >= sy && my < sy + 18)
                gfx.fill(sx + 1, sy + 1, sx + 17, sy + 17, C_HIGHLIGHT);
        }
    }

    // ── Drag overlay ──────────────────────────────────────────────────

    private void renderDragging(@NotNull GuiGraphics gfx, int mx, int my) {
        if (dragging.isEmpty()) return;
        int pw = draggingSize.width() * CELL - 1, ph = draggingSize.height() * CELL - 1;
        int px = mx - dragOffX, py = my - dragOffY;
        gfx.fill(px, py, px + pw, py + ph, C_ITEM_BG);
        drawBorder(gfx, px, py, pw, ph, C_ITEM_BORDER);
        gfx.renderItem(dragging, px + (pw - 16) / 2, py + (ph - 16) / 2);
        gfx.renderItemDecorations(font, dragging, px + (pw - 16) / 2, py + (ph - 16) / 2);
    }

    // ── Tooltip ───────────────────────────────────────────────────────

    private void renderHoveredTooltip(@NotNull GuiGraphics gfx, int mx, int my) {
        if (!dragging.isEmpty()) return;

        if (tooltipSlot >= 0) {
            ItemStack s = menu.getGridInventory().getItem(tooltipSlot);
            if (!s.isEmpty()) { gfx.renderTooltip(font, s, mx, my); return; }
        }

        Player player = minecraft.player;
        for (EqSlotDef def : eqSlots) {
            if (mx >= def.x() && mx < def.x() + def.w() && my >= def.y() && my < def.y() + def.h()) {
                ItemStack s = getEquipmentStack(player, def);
                if (!s.isEmpty()) gfx.renderTooltip(font, s, mx, my);
                return;
            }
        }

        // Pockets
        int pox = contX() + 2, poy = panelY() + 2 + 10;
        for (int i = 0; i < TarkovInventoryMenu.POCKETS_COUNT; i++) {
            int sx = pox + i * (SS + 3);
            if (mx >= sx && mx < sx + SS && my >= poy && my < poy + SS) {
                ItemStack s = menu.getPocketSlot(i);
                if (!s.isEmpty()) gfx.renderTooltip(font, s, mx, my);
                return;
            }
        }

        // Pouch
        int pouY = panelY() + CONT_PANEL_H - 44 + 10;
        for (int i = 0; i < TarkovInventoryMenu.POUCH_COUNT; i++) {
            int sx = contX() + 2 + i * (SS + 3);
            if (mx >= sx && mx < sx + SS && my >= pouY && my < pouY + SS) {
                ItemStack s = menu.getPouchSlot(i);
                if (!s.isEmpty()) gfx.renderTooltip(font, s, mx, my);
                return;
            }
        }

        // Player inventory
        int ox = leftPos + PAD, oy = playerY();
        int relX = mx - ox, relY = my - oy;
        if (relX >= 0 && relX < 9 * 18 && relY >= 0 && relY < 3 * 18) {
            ItemStack s = minecraft.player.getInventory().getItem(relX / 18 + (relY / 18) * 9 + 9);
            if (!s.isEmpty()) gfx.renderTooltip(font, s, mx, my);
        } else {
            int sepY = oy + 3 * 18 + 3;
            if (relX >= 0 && relX < 9 * 18 && my >= sepY + 2 && my < sepY + 20) {
                ItemStack s = minecraft.player.getInventory().getItem(relX / 18);
                if (!s.isEmpty()) gfx.renderTooltip(font, s, mx, my);
            }
        }
    }

    // ================================================================
    // Mouse input
    // ================================================================

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Grid + search bar — only active when backpack is equipped
        if (hasBackpackEquipped()) {
            int sbBaseX = contX() + 2 + font.width("BACKPACK") + 6;
            int sbBaseY = panelY() + 42;
            int sbW     = CONT_PANEL_W - (sbBaseX - contX()) - 4;
            if (mx >= sbBaseX && mx < sbBaseX + sbW && my >= sbBaseY && my < sbBaseY + 10) {
                searchActive = !searchActive;
                if (!searchActive) searchText = "";
                return true;
            }

            int col = toGridCol(mx), row = toGridRow(my);
            if (col >= 0 && row >= 0) {
                return handleGridClick(col, row, (int) mx, (int) my, button);
            }
        }

        if (handleEqSlotClick((int) mx, (int) my, button))   return true;
        if (handlePocketsClick((int) mx, (int) my, button))  return true;
        if (handlePouchClick((int) mx, (int) my, button))    return true;
        if (handlePlayerInvClick((int) mx, (int) my, button)) return true;

        // Right-click outside to rotate drag
        if (button == 1 && !dragging.isEmpty()) {
            draggingSize = draggingSize.rotated();
            return true;
        }

        return super.mouseClicked(mx, my, button);
    }

    private boolean handleGridClick(int col, int row, int mx, int my, int button) {
        GridInventory inv = menu.getGridInventory();
        if (button == 0) {
            if (!dragging.isEmpty()) {
                ItemStack leftover = menu.placeInGrid(dragging, col, row, draggingSize);
                if (leftover.isEmpty()) { dragging = ItemStack.EMPTY; }
            } else {
                int anchor = inv.getAnchorSlot(col, row);
                if (anchor >= 0) {
                    int gx = inv.getSlotX(anchor), gy = inv.getSlotY(anchor);
                    dragging     = menu.pickFromGrid(anchor);
                    draggingSize = GridItemSizes.getSize(dragging.getItem());
                    dragOffX     = Math.max(0, Math.min(mx - (gridOriginX() + gx * CELL), draggingSize.width()  * CELL - 1));
                    dragOffY     = Math.max(0, Math.min(my - (gridOriginY() + gy * CELL), draggingSize.height() * CELL - 1));
                }
            }
            return true;
        }
        if (button == 1 && !dragging.isEmpty()) { draggingSize = draggingSize.rotated(); return true; }
        return false;
    }

    private boolean handleEqSlotClick(int mx, int my, int button) {
        if (button != 0) return false;
        Player player = minecraft.player;
        for (int i = 0; i < eqSlots.size(); i++) {
            EqSlotDef def = eqSlots.get(i);
            if (mx < def.x() || mx >= def.x() + def.w() || my < def.y() || my >= def.y() + def.h()) continue;
            ItemStack cur = getEquipmentStack(player, def);
            if (!dragging.isEmpty()) {
                setEquipmentStack(player, def, dragging.copy());
                dragging = cur.isEmpty() ? ItemStack.EMPTY : cur.copy();
                if (!dragging.isEmpty()) { draggingSize = GridItemSizes.getSize(dragging.getItem()); dragOffX = dragOffY = 8; }
            } else if (!cur.isEmpty()) {
                dragging = cur.copy(); draggingSize = GridItemSizes.getSize(dragging.getItem()); dragOffX = dragOffY = 8;
                setEquipmentStack(player, def, ItemStack.EMPTY);
            }
            return true;
        }
        return false;
    }

    private boolean handlePocketsClick(int mx, int my, int button) {
        if (button != 0) return false;
        int ox = contX() + 2, oy = panelY() + 2 + 10;
        for (int i = 0; i < TarkovInventoryMenu.POCKETS_COUNT; i++) {
            int sx = ox + i * (SS + 3);
            if (mx < sx || mx >= sx + SS || my < oy || my >= oy + SS) continue;
            ItemStack cur = menu.getPocketSlot(i);
            if (!dragging.isEmpty()) {
                menu.setPocketSlot(i, dragging.copy());
                dragging = cur.isEmpty() ? ItemStack.EMPTY : cur.copy();
                if (!dragging.isEmpty()) { draggingSize = GridItemSizes.getSize(dragging.getItem()); dragOffX = dragOffY = 8; }
            } else if (!cur.isEmpty()) {
                dragging = cur.copy(); draggingSize = GridItemSizes.getSize(dragging.getItem()); dragOffX = dragOffY = 8;
                menu.setPocketSlot(i, ItemStack.EMPTY);
            }
            return true;
        }
        return false;
    }

    private boolean handlePouchClick(int mx, int my, int button) {
        if (button != 0) return false;
        int ox = contX() + 2, oy = panelY() + CONT_PANEL_H - 44 + 10;
        for (int i = 0; i < TarkovInventoryMenu.POUCH_COUNT; i++) {
            int sx = ox + i * (SS + 3);
            if (mx < sx || mx >= sx + SS || my < oy || my >= oy + SS) continue;
            ItemStack cur = menu.getPouchSlot(i);
            if (!dragging.isEmpty()) {
                menu.setPouchSlot(i, dragging.copy());
                dragging = cur.isEmpty() ? ItemStack.EMPTY : cur.copy();
                if (!dragging.isEmpty()) { draggingSize = GridItemSizes.getSize(dragging.getItem()); dragOffX = dragOffY = 8; }
            } else if (!cur.isEmpty()) {
                dragging = cur.copy(); draggingSize = GridItemSizes.getSize(dragging.getItem()); dragOffX = dragOffY = 8;
                menu.setPouchSlot(i, ItemStack.EMPTY);
            }
            return true;
        }
        return false;
    }

    private boolean handlePlayerInvClick(int mx, int my, int button) {
        int ox = leftPos + PAD, oy = playerY();
        int sepY = oy + 3 * 18 + 3;
        int relX = mx - ox, relY = my - oy;
        Inventory inv = minecraft.player.getInventory();

        int invSlot = -1;
        if (relX >= 0 && relX < 9 * 18 && relY >= 0 && relY < 3 * 18)
            invSlot = relX / 18 + (relY / 18) * 9 + 9;
        else if (relX >= 0 && relX < 9 * 18 && my >= sepY + 2 && my < sepY + 20)
            invSlot = relX / 18;

        if (invSlot < 0) return false;
        ItemStack cur = inv.getItem(invSlot);

        if (button == 0) {
            if (!dragging.isEmpty()) {
                inv.setItem(invSlot, dragging.copy());
                dragging = cur.isEmpty() ? ItemStack.EMPTY : cur.copy();
                if (!dragging.isEmpty()) { draggingSize = GridItemSizes.getSize(dragging.getItem()); dragOffX = dragOffY = 8; }
            } else if (!cur.isEmpty()) {
                dragging = cur.copy(); draggingSize = GridItemSizes.getSize(dragging.getItem()); dragOffX = dragOffY = 8;
                inv.setItem(invSlot, ItemStack.EMPTY);
            }
            return true;
        }
        if (button == 1 && !dragging.isEmpty() && cur.isEmpty()) {
            inv.setItem(invSlot, dragging.copyWithCount(1));
            dragging.shrink(1);
            if (dragging.isEmpty()) dragging = ItemStack.EMPTY;
            return true;
        }
        return false;
    }

    // ================================================================
    // Keyboard — search input
    // ================================================================

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (searchActive) { searchActive = false; searchText = ""; return true; }
            if (!dragging.isEmpty()) { returnDragging(); return true; }
        }
        if (searchActive && keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchText.isEmpty()) {
            searchText = searchText.substring(0, searchText.length() - 1);
            return true;
        }
        if (searchActive) return true; // consume while typing
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char c, int modifiers) {
        if (searchActive) { if (c >= 32 && searchText.length() < 24) searchText += c; return true; }
        return super.charTyped(c, modifiers);
    }

    @Override
    public void onClose() { returnDragging(); super.onClose(); }

    // ================================================================
    // Equipment stack access
    // ================================================================

    private ItemStack getEquipmentStack(Player player, EqSlotDef def) {
        return switch (def.source()) {
            case ARMOR -> def.sourceIdx() < 0 ? ItemStack.EMPTY : switch (def.sourceIdx()) {
                case 0  -> player.getItemBySlot(EquipmentSlot.HEAD);
                case 1  -> player.getItemBySlot(EquipmentSlot.CHEST);
                case 2  -> player.getItemBySlot(EquipmentSlot.LEGS);
                case 3  -> player.getItemBySlot(EquipmentSlot.FEET);
                default -> ItemStack.EMPTY;
            };
            case CAP -> def.sourceIdx() < 0 ? ItemStack.EMPTY
                      : ModCapabilities.get(player).map(c -> c.getSlot(def.sourceIdx())).orElse(ItemStack.EMPTY);
        };
    }

    private void setEquipmentStack(Player player, EqSlotDef def, ItemStack stack) {
        if (def.source() == EqSource.ARMOR && def.sourceIdx() >= 0) {
            player.setItemSlot(switch (def.sourceIdx()) {
                case 0  -> EquipmentSlot.HEAD;
                case 1  -> EquipmentSlot.CHEST;
                case 2  -> EquipmentSlot.LEGS;
                case 3  -> EquipmentSlot.FEET;
                default -> EquipmentSlot.MAINHAND;
            }, stack);
        } else if (def.source() == EqSource.CAP && def.sourceIdx() >= 0) {
            ModCapabilities.get(player).ifPresent(c -> c.setSlot(def.sourceIdx(), stack));
        }
    }

    // ================================================================
    // Utilities
    // ================================================================

    private void returnDragging() {
        if (dragging.isEmpty()) return;
        if (!menu.autoPlace(dragging)) {
            if (minecraft.player != null) minecraft.player.getInventory().add(dragging.copy());
        }
        dragging = ItemStack.EMPTY;
    }

    private boolean isSearchDimmed(ItemStack s) {
        return !searchText.isEmpty() && !s.getHoverName().getString().toLowerCase().contains(searchText.toLowerCase());
    }

    private String shortenName(String name, int cellWidth) {
        int max = cellWidth * 3 + 1;
        return name.length() <= max ? name : name.substring(0, max - 1) + "…";
    }

    private void renderSmallSlot(GuiGraphics gfx, int sx, int sy, int w, int h,
                                  ItemStack stack, int bg, int border, int mx, int my) {
        gfx.fill(sx, sy, sx + w, sy + h, bg);
        drawBorder(gfx, sx, sy, w, h, border);
        if (!stack.isEmpty()) {
            int ix = sx + (w - 16) / 2, iy = sy + (h - 16) / 2;
            gfx.renderItem(stack, ix, iy);
            gfx.renderItemDecorations(font, stack, ix, iy);
        } else {
            gfx.fill(sx + w/2 - 4, sy + h/2 - 1, sx + w/2 + 4, sy + h/2 + 1, 0xFF2A2A2A);
            gfx.fill(sx + w/2 - 1, sy + h/2 - 4, sx + w/2 + 1, sy + h/2 + 4, 0xFF2A2A2A);
        }
        if (mx >= sx && mx < sx + w && my >= sy && my < sy + h)
            gfx.fill(sx, sy, sx + w, sy + h, C_HOVER);
    }

    private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h, int c) {
        gfx.fill(x,         y,         x + w, y + 1,     c);
        gfx.fill(x,         y + h - 1, x + w, y + h,     c);
        gfx.fill(x,         y,         x + 1, y + h,     c);
        gfx.fill(x + w - 1, y,         x + w, y + h,     c);
    }

    private void drawSlotBg(GuiGraphics gfx, int x, int y) {
        gfx.fill(x + 1, y + 1, x + 17, y + 17, 0xFF2A2A2A);
        drawBorder(gfx, x, y, 18, 18, C_SLOT_BORDER);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics gfx, int mx, int my) { /* handled above */ }
}
