package com.tarkovinventory.client.screen;

import com.tarkovinventory.capability.IPlayerEquipment;
import com.tarkovinventory.capability.ModCapabilities;
import com.tarkovinventory.compat.BackpackCompat;
import com.tarkovinventory.compat.CuriosCompat;
import com.tarkovinventory.container.TarkovInventoryMenu;
import com.tarkovinventory.inventory.BackpackSizes;
import com.tarkovinventory.inventory.GridInventory;
import com.tarkovinventory.inventory.GridItemSizes;
import com.tarkovinventory.inventory.GridSize;
import com.tarkovinventory.client.CorpseClientCache;
import com.tarkovinventory.network.C2SLootAllPacket;
import com.tarkovinventory.network.C2SPickupItemPacket;
import com.tarkovinventory.network.C2SRigSlotPacket;
import com.tarkovinventory.network.C2STakeFromCorpsePacket;
import com.tarkovinventory.network.ModNetwork;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Full Tarkov-style character inventory screen.
 *
 * Left panel  : character equipment slots + stats bar
 * Right panel : POCKETS (7, hotbar 3-9) / BACKPACK grid (search) / no pouch
 *
 * PRIMARY weapon  → synced to hotbar slot 1 (press 1 to use)
 * SECONDARY weapon → synced to hotbar slot 2 (press 2 to use)
 * POCKET 1-7      → synced to hotbar slots 3-9
 */
public class TarkovInventoryScreen extends AbstractContainerScreen<TarkovInventoryMenu> {

    // ── Layout constants ──────────────────────────────────────────────
    private static final int PAD           = 7;
    private static final int CHAR_PANEL_W  = 190;
    private static final int CHAR_PANEL_H  = 260;
    private static final int CONT_PANEL_W  = 218;
    private static final int CONT_PANEL_H  = CHAR_PANEL_H;
    private static final int VICI_PANEL_W  = 160;
    private static final int VICI_PANEL_H  = CHAR_PANEL_H;
    private static final int VICI_HEADER_H = 18;
    private static final int VICI_ROW_H    = 20;
    private static final double VICINITY_RANGE = 6.0;
    private static final int TOTAL_W       = PAD + CHAR_PANEL_W + PAD + CONT_PANEL_W + PAD + VICI_PANEL_W + PAD;
    private static final int TOTAL_H       = PAD + CHAR_PANEL_H + PAD;

    // ── Grid ─────────────────────────────────────────────────────────
    private static final int CELL = 17;
    // Active grid dims — recomputed every render() based on the equipped backpack
    private int currentGridCols = BackpackSizes.DEFAULT_COLS;
    private int currentGridRows = BackpackSizes.DEFAULT_ROWS;
    private int currentGridW()  { return currentGridCols * CELL; }
    private int currentGridH()  { return currentGridRows * CELL; }

    // ── Small-slot size ───────────────────────────────────────────────
    private static final int SS = 26;

    // ── Colours ───────────────────────────────────────────────────────
    private static final int C_BG_DARK      = 0xFF111111;
    private static final int C_BG_PANEL     = 0xFF181818;
    private static final int C_BG_SECTION   = 0xFF1E1E1E;
    private static final int C_BORDER       = 0xFF3A3A3A;
    private static final int C_GRID_EMPTY   = 0xFF252525;
    private static final int C_GRID_LINE    = 0xFF303030;
    private static final int C_HOVER        = 0x40607060;
    private static final int C_DRAG_VALID   = 0x7044AA44;
    private static final int C_DRAG_INVALID = 0x70AA4444;
    private static final int C_ITEM_BG      = 0xFF1E3524;
    private static final int C_ITEM_BORDER  = 0xFF4CAF50;
    private static final int C_SLOT_EMPTY   = 0xFF242424;
    private static final int C_SLOT_BORDER  = 0xFF444444;
    private static final int C_TEXT_LABEL   = 0xFF8A8A7A;
    private static final int C_TEXT_TITLE   = 0xFFD4C89A;
    private static final int C_TEXT_WHITE   = 0xFFE0E0E0;
    private static final int C_SEARCH_BG    = 0xFF1A2A1A;
    private static final int C_HEALTH       = 0xFF4CAF50;
    private static final int C_HYDRATION    = 0xFF2196F3;
    private static final int C_ENERGY       = 0xFFFFC107;
    private static final int C_WEIGHT       = 0xFFBBBBBB;
    private static final int C_HOTBAR_BADGE  = 0xFF556B4A;  // tint on hotbar-synced slots
    // ── Vicinity / Ground panel colours ──────────────────────────────
    private static final int C_VICI_HEADER  = 0xFF0E1A12;
    private static final int C_VICI_ROW_ODD = 0xFF181E18;
    private static final int C_VICI_HOVER   = 0x5060A070;
    private static final int C_VICI_BTN     = 0xFF2A4A2A;
    private static final int C_VICI_BTN_HOV = 0xFF3A6A3A;
    private static final int C_VICI_DIST    = 0xFF607060;
    private static final int C_SCROLLBAR_BG = 0xFF1A1A1A;
    private static final int C_SCROLLBAR_FG = 0xFF4CAF50;
    // ── Loot (corpse) panel colours ───────────────────────────────────
    private static final int C_LOOT_HEADER  = 0xFF1A0808;
    private static final int C_LOOT_TITLE   = 0xFFDD6655;
    private static final int C_LOOT_SUBHDR  = 0xFF120606;
    private static final int C_LOOT_HOVER   = 0x50A06060;
    private static final int C_LOOT_BTN     = 0xFF4A1A1A;
    private static final int C_LOOT_BTN_HOV = 0xFF6A2A2A;
    private static final int C_LOOT_EXIT    = 0xFF3A1010;
    private static final int C_LOOT_EXIT_H  = 0xFF5A2020;
    private static final int C_LOOT_NAV     = 0xFF2A2A3A;
    private static final int C_LOOT_NAV_H   = 0xFF3A3A5A;
    // Loot-panel layout
    private static final int LOOT_TITLE_H  = 20;
    private static final int LOOT_SUBHDR_H = 12;
    private static final int LOOT_FOOTER_H = 22;

    // ── Equipment slot model ──────────────────────────────────────────
    /**
     * ARMOR  = vanilla EquipmentSlot (HEAD/CHEST/LEGS/FEET)
     * CAP    = our NBT capability slot
     * HOTBAR = playerInv.getItem(sourceIdx) — writes go directly to hotbar
     * CURIOS = Curios API slot (curiosSlotId / curiosSlotIndex), falls back to
     *          the companion ARMOR/CAP source when Curios is absent or that
     *          slot type doesn't exist on this player.
     */
    private record EqSlotDef(String label, int x, int y, int w, int h,
                              EqSource source, int sourceIdx,
                              String curiosSlotId, int curiosSlotIndex) {
        /** Convenience constructor for slots that have no Curios mapping. */
        EqSlotDef(String label, int x, int y, int w, int h, EqSource src, int idx) {
            this(label, x, y, w, h, src, idx, "", 0);
        }
    }
    private enum EqSource { ARMOR, CAP, HOTBAR }

    private List<EqSlotDef> eqSlots = new ArrayList<>();

    // ── Drag state ────────────────────────────────────────────────────
    private ItemStack dragging     = ItemStack.EMPTY;
    private GridSize  draggingSize = GridSize.ONE_BY_ONE;
    private int       dragOffX, dragOffY;

    // ── Search ────────────────────────────────────────────────────────
    private boolean searchActive = false;
    private String  searchText   = "";

    // ── Hover ─────────────────────────────────────────────────────────
    private int hoverGridCol = -1, hoverGridRow = -1;
    private int tooltipSlot  = -1;

    // ── Rig slots ─────────────────────────────────────────────────────
    /** Pixel height consumed by the rig section this frame (0 = no rig). */
    private int  rigSectionH     = 0;
    /** Which rig slot the cursor is over (-1 = none). */
    private int  rigHoveredSlot  = -1;
    /** Which source the hovered rig came from (SRC_CURIOS / SRC_ARMOR). */
    private byte rigHoveredSource = -1;
    private static final int  RIG_CELL   = 18; // px per rig-slot cell (incl. 1-px gap)

    // ── Curios ───────────────────────────────────────────────────────
    private List<CuriosCompat.CuriosSlotEntry> curiosSlots = new ArrayList<>();

    // ── Ground / Vicinity panel state ────────────────────────────────
    private int              vicinityScroll     = 0;
    private int              hoveredVicinityIdx = -1;
    private boolean          lootAllHovered     = false;
    private List<ItemEntity> vicinityItems      = new ArrayList<>();
    private List<ViciEntry>  viciRows           = List.of();

    // ── Loot panel state (corpse) ─────────────────────────────────────
    /** Which corpse is open in the loot panel; null = ground/vicinity mode. */
    private BlockPos         selectedCorpsePos  = null;
    private int              lootScroll         = 0;
    private int              lootHoveredRow     = -1;
    private boolean          lootExitHovered    = false;
    private boolean          lootTakeAllHovered = false;
    private boolean          lootNavLeftHov     = false;
    private boolean          lootNavRightHov    = false;
    /** Ordered list of nearby corpse positions (nearest first). Rebuilt each frame. */
    private List<BlockPos>   nearbyCorpseList   = List.of();

    // ── Loot-panel drag-and-drop state ────────────────────────────────
    /** True while the player is dragging an item out of the loot panel. */
    private boolean   lootDragging      = false;
    /** Copy of the item being dragged, for rendering. */
    private ItemStack lootDragStack     = ItemStack.EMPTY;
    /** Corpse the drag originated from. */
    private BlockPos  lootDragCorpsePos = null;
    /** Index into {@link #buildLootRows} of the row being dragged. */
    private int       lootDragSourceRow = -1;
    /** Mouse position at drag start (used to detect click vs drag). */
    private double    lootDragStartX    = 0, lootDragStartY = 0;

    // ── Loot-panel row model ──────────────────────────────────────────
    private static final int LOOT_ROW_H = 18;
    private static final int LOOT_SEP_H = 10;
    private sealed interface LootRow permits LootEquipRow, LootInvRow, LootSepRow {}
    /** An equipment/curios slot (filled). Click = take by named slot key. */
    private record LootEquipRow(String key, ItemStack stack) implements LootRow {}
    /** A main-inventory item. Click = take by inventory index. */
    private record LootInvRow(int idx, ItemStack stack) implements LootRow {}
    /** Visual separator between equipment and inventory sections. */
    private record LootSepRow(int invCount) implements LootRow {}

    /** Ordered slot keys used to drive display order in the loot panel. */
    private static final String[] EQUIP_SLOT_ORDER = {
        "armor.head", "armor.chest", "armor.legs", "armor.feet", "offhand",
        "curios.head", "curios.body", "curios.back",
        "curios.earwear", "curios.facewear", "curios.knees"
    };

    private static String equipSlotLabel(String key) {
        return switch (key) {
            case "armor.head"     -> "HELMET";
            case "armor.chest"    -> "ARMOR";
            case "armor.legs"     -> "LEGS";
            case "armor.feet"     -> "BOOTS";
            case "offhand"        -> "OFFHAND";
            case "curios.head"    -> "HEADGEAR";
            case "curios.body"    -> "BODY";
            case "curios.back"    -> "ON BACK";
            case "curios.earwear" -> "EARWEAR";
            case "curios.facewear"-> "FACEWEAR";
            case "curios.knees"   -> "KNEES";
            default -> key.toUpperCase();
        };
    }

    /** Builds the unified scrollable row list from a corpse entry. */
    private List<LootRow> buildLootRows(CorpseClientCache.CorpseEntry corpse) {
        List<LootRow> rows = new ArrayList<>();
        var slotted = corpse.slottedItems();
        for (String key : EQUIP_SLOT_ORDER) {
            ItemStack s = slotted.get(key);
            if (s != null && !s.isEmpty()) rows.add(new LootEquipRow(key, s));
        }
        // Any extra slotted keys not in the standard order
        slotted.forEach((k, v) -> {
            if (!List.of(EQUIP_SLOT_ORDER).contains(k) && !v.isEmpty())
                rows.add(new LootEquipRow(k, v));
        });
        rows.add(new LootSepRow(corpse.inventoryItems().size()));
        var inv = corpse.inventoryItems();
        for (int i = 0; i < inv.size(); i++) rows.add(new LootInvRow(i, inv.get(i)));
        return rows;
    }

    // ── Vicinity row types (ground mode only) ─────────────────────────
    private interface ViciEntry {}
    private record ViciGround(ItemEntity entity) implements ViciEntry {}
    private record ViciCorpseHeader(BlockPos pos, String ownerName,
                                    int itemCount, double dist) implements ViciEntry {}
    private record ViciCorpseItem(BlockPos pos, int slot, ItemStack stack) implements ViciEntry {}

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
    private int gridOriginX() { return contX() + 2; }
    private int gridOriginY() { return panelY() + 68 + rigSectionH; }
    private int viciX()       { return contX() + CONT_PANEL_W + PAD; }

    private int toGridCol(double px) {
        int rel = (int) px - gridOriginX();
        return (rel >= 0 && rel < currentGridW()) ? rel / CELL : -1;
    }
    private int toGridRow(double py) {
        int rel = (int) py - gridOriginY();
        return (rel >= 0 && rel < currentGridH()) ? rel / CELL : -1;
    }

    // ================================================================
    // Equipment slot layout
    // ================================================================

    private void buildEquipmentSlots() {
        eqSlots = new ArrayList<>();
        int ox = charX() + 4;
        int oy = panelY() + 14;

        // ── Row 1: Head ──────────────────────────────────────────────
        // EARPIECE: no standard Curios slot, stored in capability
        eqSlots.add(new EqSlotDef("EARPIECE",   ox,       oy,       SS,     SS,     EqSource.CAP,    IPlayerEquipment.SLOT_EARPIECE));
        // HEADWEAR: Curios "head" when loaded, vanilla HEAD armor otherwise
        eqSlots.add(new EqSlotDef("HEADWEAR",   ox + 38,  oy,       SS+8,   SS+8,   EqSource.ARMOR,  0,  "head", 0));
        // FACE COVER: Curios "face" when loaded, otherwise stored in capability (no vanilla slot)
        eqSlots.add(new EqSlotDef("FACE COVER", ox + 90,  oy,       SS,     SS,     EqSource.CAP,    -1, "face", 0));

        // ── Row 2: Torso ─────────────────────────────────────────────
        // ARMBAND: Curios "ring" or "hands", else capability
        eqSlots.add(new EqSlotDef("ARMBAND",    ox,       oy + 40,  SS,     SS,     EqSource.CAP,    IPlayerEquipment.SLOT_ARMBAND, "ring", 0));
        // BODY ARMOR: Curios "body" when loaded, vanilla CHEST otherwise
        eqSlots.add(new EqSlotDef("BODY ARMOR", ox + 38,  oy + 40,  SS+8,   SS+14,  EqSource.ARMOR,  1,  "body", 0));
        // EYEWEAR: Curios "charm"/"necklace", else capability
        eqSlots.add(new EqSlotDef("EYEWEAR",    ox + 90,  oy + 40,  SS,     SS,     EqSource.CAP,    -1, "charm", 0));

        // ── Row 3: Legs / Feet ────────────────────────────────────────
        // PANTS / BOOTS: vanilla armor slots (Curios typically doesn't have these)
        eqSlots.add(new EqSlotDef("PANTS",      ox + 5,   oy + 90,  SS+8,   SS+14,  EqSource.ARMOR,  2));
        eqSlots.add(new EqSlotDef("BOOTS",      ox + 65,  oy + 90,  SS+8,   SS,     EqSource.ARMOR,  3));

        // ── Row 4: Weapons (hotbar synced) ────────────────────────────
        // PRIMARY → hotbar slot 0 (press 1), SECONDARY → hotbar slot 1 (press 2)
        eqSlots.add(new EqSlotDef("PRIMARY",    ox,       oy + 130, SS+8,   SS+20,  EqSource.HOTBAR, 0));
        eqSlots.add(new EqSlotDef("SECONDARY",  ox + 90,  oy + 130, SS+8,   SS+8,   EqSource.HOTBAR, 1));

        // ── Row 5: Backpack ───────────────────────────────────────────
        // ON BACK: Curios "back" when loaded, capability otherwise
        eqSlots.add(new EqSlotDef("ON BACK",    ox,       oy + 182, SS+8,   SS+20,  EqSource.CAP,    IPlayerEquipment.SLOT_ON_BACK, "back", 0));
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
        // Refresh Curios every frame so live changes appear immediately
        refreshCuriosSlots();
        // Recompute active grid size from the currently equipped backpack
        ItemStack equippedBp = getEquippedBackpack();
        currentGridCols = BackpackSizes.getCols(equippedBp);
        currentGridRows = BackpackSizes.getRows(equippedBp);
        menu.getGridInventory().setActiveDimensions(currentGridCols, currentGridRows);

        // Rebuild vicinity item list
        if (minecraft != null && minecraft.player != null && minecraft.level != null) {
            AABB box = minecraft.player.getBoundingBox().inflate(VICINITY_RANGE);
            vicinityItems = minecraft.level.getEntitiesOfClass(ItemEntity.class, box,
                e -> e.isAlive() && e.distanceTo(minecraft.player) <= VICINITY_RANGE);
        } else {
            vicinityItems = List.of();
        }

        // ── Corpse auto-selection ────────────────────────────────────
        Map<BlockPos, CorpseClientCache.CorpseEntry> allCorpses = CorpseClientCache.all();
        if (minecraft != null && minecraft.player != null && !allCorpses.isEmpty()) {
            final var pPos = minecraft.player.blockPosition();
            nearbyCorpseList = allCorpses.keySet().stream()
                    .sorted(java.util.Comparator.comparingDouble(p -> p.distSqr(pPos)))
                    .collect(java.util.stream.Collectors.toList());
        } else {
            nearbyCorpseList = List.of();
        }
        // Clear selection if that corpse was looted / went out of range
        if (selectedCorpsePos != null && !allCorpses.containsKey(selectedCorpsePos)) {
            selectedCorpsePos = nearbyCorpseList.isEmpty() ? null : nearbyCorpseList.get(0);
            lootScroll = 0;
        }
        // Auto-select nearest corpse when one appears
        if (selectedCorpsePos == null && !nearbyCorpseList.isEmpty()) {
            selectedCorpsePos = nearbyCorpseList.get(0);
            lootScroll = 0;
        }

        renderBackground(gfx);
        renderBg(gfx, pt, mx, my);
        renderCharacterPanel(gfx, mx, my);
        renderContainersPanel(gfx, mx, my);
        renderVicinityPanel(gfx, mx, my);
        renderDragging(gfx, mx, my);
        renderHoveredTooltip(gfx, mx, my);
    }

    /** Returns the item currently in the ON BACK / Curios "back" slot, or EMPTY. */
    private ItemStack getEquippedBackpack() {
        if (minecraft == null || minecraft.player == null) return ItemStack.EMPTY;
        if (CuriosCompat.isLoaded()) {
            for (CuriosCompat.CuriosSlotEntry e : curiosSlots)
                if ("back".equals(e.slotId()) && e.index() == 0 && !e.stack().isEmpty())
                    return e.stack();
        }
        return ModCapabilities.get(minecraft.player)
                .map(cap -> cap.getSlot(IPlayerEquipment.SLOT_ON_BACK))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics gfx, float pt, int mx, int my) {
        gfx.fill(leftPos, topPos, leftPos + TOTAL_W, topPos + TOTAL_H, C_BG_DARK);
        gfx.fill(leftPos, topPos, leftPos + TOTAL_W, topPos + 12, 0xFF0E0E0E);
        gfx.drawString(font, "TARKOV INVENTORY", leftPos + 4, topPos + 2, C_TEXT_TITLE, false);
        String esc = "[ ESC ] Close";
        gfx.drawString(font, esc, leftPos + TOTAL_W - font.width(esc) - 4, topPos + 2, C_TEXT_LABEL, false);
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

        renderStatsBar(gfx, ox, oy + CHAR_PANEL_H - 18);
    }

    private void renderEquipmentSlot(@NotNull GuiGraphics gfx, int idx, Player player, int mx, int my) {
        EqSlotDef def   = eqSlots.get(idx);
        ItemStack stack = getEquipmentStack(player, def);
        boolean filled  = !stack.isEmpty();
        boolean isHotbar = def.source() == EqSource.HOTBAR;

        // Background: hotbar-synced slots get a subtle green-tinted bg
        int bgColor     = filled ? C_ITEM_BG : (isHotbar ? 0xFF1A2010 : C_SLOT_EMPTY);
        int borderColor = filled ? C_ITEM_BORDER : (isHotbar ? C_HOTBAR_BADGE : C_SLOT_BORDER);

        gfx.fill(def.x(), def.y(), def.x() + def.w(), def.y() + def.h(), bgColor);
        drawBorder(gfx, def.x(), def.y(), def.w(), def.h(), borderColor);

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
            // Empty slot — draw a "+" cross
            int cx = def.x() + def.w() / 2, cy = def.y() + def.h() / 2;
            gfx.fill(cx - 6, cy - 1, cx + 6, cy + 1, 0xFF303030);
            gfx.fill(cx - 1, cy - 6, cx + 1, cy + 6, 0xFF303030);

            // Hotbar badge: small number in corner showing hotbar key
            if (isHotbar) {
                String key = String.valueOf(def.sourceIdx() + 1);
                gfx.drawString(font, key, def.x() + 2, def.y() + 2, C_HOTBAR_BADGE, false);
            }
        }

        if (mx >= def.x() && mx < def.x() + def.w() && my >= def.y() && my < def.y() + def.h())
            gfx.fill(def.x(), def.y(), def.x() + def.w(), def.y() + def.h(), C_HOVER);
    }

    private void renderStatsBar(@NotNull GuiGraphics gfx, int ox, int oy) {
        Player p = minecraft.player;
        if (p == null) return;
        float hp   = p.getHealth(), maxHp = p.getMaxHealth();
        float food = p.getFoodData().getFoodLevel();
        float sat  = p.getFoodData().getSaturationLevel();
        int items  = 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++)
            if (!p.getInventory().getItem(i).isEmpty()) items++;

        gfx.drawString(font, items + " KG",          ox + 4,  oy,      C_WEIGHT,    false);
        gfx.drawString(font, (int)hp + "/" + (int)maxHp, ox + 4, oy + 10, C_HEALTH, false);
        gfx.drawString(font, (int)food + "/100",     ox + 70, oy,      C_ENERGY,    false);
        gfx.drawString(font, (int)(sat*10)/10f + "", ox + 70, oy + 10, C_HYDRATION, false);
    }

    // ── Containers panel ──────────────────────────────────────────────

    private void renderContainersPanel(@NotNull GuiGraphics gfx, int mx, int my) {
        int ox = contX(), oy = panelY();
        gfx.fill(ox, oy, ox + CONT_PANEL_W, oy + CONT_PANEL_H, C_BG_PANEL);
        drawBorder(gfx, ox, oy, CONT_PANEL_W, CONT_PANEL_H, C_BORDER);

        renderPocketsSection(gfx, ox + 2, oy + 2, mx, my);
        rigSectionH = renderRigSection(gfx, ox + 2, oy + 42, mx, my);
        renderBackpackSection(gfx, ox + 2, oy + 42 + rigSectionH, mx, my);
    }

    private void renderPocketsSection(@NotNull GuiGraphics gfx, int ox, int oy, int mx, int my) {
        gfx.drawString(font, "POCKETS  [hotbar 3-9]", ox, oy, C_TEXT_TITLE, false);
        int count = TarkovInventoryMenu.POCKETS_COUNT; // 7
        for (int i = 0; i < count; i++) {
            int sx = ox + i * (SS + 3);
            ItemStack s = menu.getPocketSlot(i);
            renderSmallSlot(gfx, sx, oy + 10, SS, SS, s, C_SLOT_EMPTY, C_SLOT_BORDER, mx, my);
            // Hotbar number badge
            if (s.isEmpty())
                gfx.drawString(font, String.valueOf(i + 3), sx + 2, oy + 11, C_HOTBAR_BADGE, false);
        }
    }

    private boolean hasBackpackEquipped() {
        if (minecraft.player == null) return false;
        // Check Curios "back" slot first, then fall back to capability
        if (CuriosCompat.isLoaded()) {
            for (CuriosCompat.CuriosSlotEntry e : curiosSlots) {
                if ("back".equals(e.slotId()) && e.index() == 0 && !e.stack().isEmpty())
                    return true;
            }
        }
        return ModCapabilities.get(minecraft.player)
                .map(cap -> !cap.getSlot(IPlayerEquipment.SLOT_ON_BACK).isEmpty())
                .orElse(false);
    }

    // ── Rig helpers ───────────────────────────────────────────────────

    /** Returns the item currently equipped as a rig (curios "body" slot). */
    private ItemStack getEquippedRigItem() {
        Player p = minecraft.player;
        if (p == null) return ItemStack.EMPTY;
        if (CuriosCompat.isLoaded()) {
            ItemStack s = CuriosCompat.getSlotItem(p, "body", 0);
            if (!s.isEmpty()) return s;
        }
        // Vanilla chest armor as fallback
        return p.getItemBySlot(EquipmentSlot.CHEST);
    }

    private byte getEquippedRigSource() {
        if (CuriosCompat.isLoaded()) {
            Player p = minecraft.player;
            if (p != null && !CuriosCompat.getSlotItem(p, "body", 0).isEmpty())
                return C2SRigSlotPacket.SRC_CURIOS;
        }
        return C2SRigSlotPacket.SRC_ARMOR;
    }

    private IItemHandler getRigItemHandler() {
        ItemStack rig = getEquippedRigItem();
        if (rig.isEmpty()) return null;
        return rig.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
    }

    /**
     * Renders the rig slots section and returns the pixel height consumed
     * (0 if no rig with an IItemHandler is equipped).
     * Also updates {@link #rigHoveredSlot} and {@link #rigHoveredSource}.
     */
    private int renderRigSection(@NotNull GuiGraphics gfx, int ox, int oy, int mx, int my) {
        rigHoveredSlot  = -1;
        rigHoveredSource = -1;

        IItemHandler handler = getRigItemHandler();
        if (handler == null || handler.getSlots() == 0) return 0;

        byte src  = getEquippedRigSource();
        int slots = handler.getSlots();
        int maxW  = CONT_PANEL_W - 4;
        int cols  = Math.min(slots, maxW / RIG_CELL);
        if (cols == 0) cols = 1;
        int rows = (int) Math.ceil((double) slots / cols);

        // Section label
        ItemStack rigItem = getEquippedRigItem();
        String label = "RIG  [" + rigItem.getHoverName().getString() + "]";
        gfx.drawString(font, label, ox, oy, C_TEXT_TITLE, false);
        // Thin separator under the label
        gfx.fill(ox, oy + 9, ox + maxW, oy + 10, C_BORDER);

        for (int i = 0; i < slots; i++) {
            int col = i % cols;
            int row = i / cols;
            int sx  = ox + col * RIG_CELL;
            int sy  = oy + 10 + row * RIG_CELL;
            int sw  = RIG_CELL - 1, sh = RIG_CELL - 1;

            ItemStack s = handler.getStackInSlot(i);
            boolean hov = !s.isEmpty()
                    && mx >= sx && mx < sx + sw && my >= sy && my < sy + sh;

            gfx.fill(sx, sy, sx + sw, sy + sh, s.isEmpty() ? C_GRID_EMPTY : C_ITEM_BG);
            drawBorder(gfx, sx, sy, sw, sh, hov ? C_ITEM_BORDER : C_GRID_LINE);

            if (!s.isEmpty()) {
                // Render 16×16 icon centred in the cell
                gfx.renderItem(s, sx + 1, sy + 1);
                if (s.getCount() > 1) {
                    String cnt = String.valueOf(s.getCount());
                    gfx.drawString(font, cnt,
                            sx + sw - font.width(cnt), sy + sh - 7, 0xFFFFFFFF, true);
                }
            }

            if (hov) { rigHoveredSlot = i; rigHoveredSource = src; }
        }

        return 10 + rows * RIG_CELL + 3; // title + grid + small gap below
    }

    private boolean handleRigSlotClick(int mx, int my, int button) {
        if (rigHoveredSlot < 0 || button != 0) return false;
        IItemHandler handler = getRigItemHandler();
        if (handler == null || rigHoveredSlot >= handler.getSlots()) return false;
        if (handler.getStackInSlot(rigHoveredSlot).isEmpty()) return true; // consumed, no-op
        ModNetwork.CHANNEL.sendToServer(
                new C2SRigSlotPacket(rigHoveredSlot, rigHoveredSource));
        return true;
    }

    private void renderBackpackSection(@NotNull GuiGraphics gfx, int ox, int oy, int mx, int my) {
        gfx.drawString(font, "BACKPACK", ox, oy, C_TEXT_TITLE, false);

        int areaX = ox, areaY = oy + 10;
        int areaW = CONT_PANEL_W - 4, areaH = CONT_PANEL_H - 42 - 10 - (areaY - panelY());

        if (!hasBackpackEquipped()) {
            int midX = areaX + areaW / 2, midY = areaY + areaH / 2;
            gfx.fill(areaX, areaY, areaX + areaW, areaY + areaH, 0xFF161616);
            drawBorder(gfx, areaX, areaY, areaW, areaH, 0xFF2A2A2A);
            gfx.fill(midX - 12, midY - 2, midX + 12, midY + 2, 0xFF2A2A2A);
            gfx.fill(midX - 2, midY - 12, midX + 2, midY + 12, 0xFF2A2A2A);
            String msg1 = "NO BACKPACK";
            String msg2 = "Equip one in ON BACK slot";
            gfx.drawString(font, msg1, midX - font.width(msg1) / 2, midY + 16, 0xFF555555, false);
            gfx.drawString(font, msg2, midX - font.width(msg2) / 2, midY + 26, 0xFF444444, false);
            hoverGridCol = -1; hoverGridRow = -1; tooltipSlot = -1;
            return;
        }

        // Search bar
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
        int gw = currentGridW(), gh = currentGridH();
        hoverGridCol = toGridCol(mx);
        hoverGridRow = toGridRow(my);
        tooltipSlot  = -1;

        gfx.fill(ox, oy, ox + gw, oy + gh, C_GRID_EMPTY);
        for (int col = 1; col < currentGridCols; col++) {
            int lx = ox + col * CELL - 1;
            gfx.fill(lx, oy, lx + 1, oy + gh, C_GRID_LINE);
        }
        for (int row = 1; row < currentGridRows; row++) {
            int ly = oy + row * CELL - 1;
            gfx.fill(ox, ly, ox + gw, ly + 1, C_GRID_LINE);
        }

        for (int i = 0; i < GridInventory.MAX_CELLS; i++) {
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
                gfx.drawString(font, shortenName(stack.getHoverName().getString(), sz.width()), px + 1, py + 1, C_TEXT_LABEL, false);
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

        if (hoverGridCol >= 0 && hoverGridRow >= 0 && dragging.isEmpty() && tooltipSlot < 0) {
            int px = ox + hoverGridCol * CELL, py = oy + hoverGridRow * CELL;
            gfx.fill(px, py, px + CELL - 1, py + CELL - 1, C_HOVER);
        }

        if (!dragging.isEmpty() && hoverGridCol >= 0 && hoverGridRow >= 0) {
            boolean fits = inv.canPlace(hoverGridCol, hoverGridRow, draggingSize);
            int pw = draggingSize.width() * CELL - 1, ph = draggingSize.height() * CELL - 1;
            gfx.fill(ox + hoverGridCol * CELL, oy + hoverGridRow * CELL,
                     ox + hoverGridCol * CELL + pw, oy + hoverGridRow * CELL + ph,
                     fits ? C_DRAG_VALID : C_DRAG_INVALID);
        }

        // Grid dimension label (e.g. "8×8") shown in top-right corner of grid
        String dimLabel = currentGridCols + "×" + currentGridRows;
        gfx.drawString(font, dimLabel, ox + gw - font.width(dimLabel), oy - 9, C_TEXT_LABEL, false);
    }

    // ── Drag overlay ──────────────────────────────────────────────────

    private void renderDragging(@NotNull GuiGraphics gfx, int mx, int my) {
        // Internal backpack-grid drag
        if (!dragging.isEmpty()) {
            int pw = draggingSize.width() * CELL - 1, ph = draggingSize.height() * CELL - 1;
            int px = mx - dragOffX, py = my - dragOffY;
            gfx.fill(px, py, px + pw, py + ph, C_ITEM_BG);
            drawBorder(gfx, px, py, pw, ph, C_ITEM_BORDER);
            gfx.renderItem(dragging, px + (pw - 16) / 2, py + (ph - 16) / 2);
            gfx.renderItemDecorations(font, dragging, px + (pw - 16) / 2, py + (ph - 16) / 2);
        }

        // Loot-panel drag: item follows cursor
        if (lootDragging && !lootDragStack.isEmpty()) {
            // Highlight the drop-target panel when hovering over it
            boolean overLoot = mx >= viciX() && mx < viciX() + VICI_PANEL_W;
            if (!overLoot) {
                // Faint green glow over left+middle panels to signal "drop here"
                gfx.fill(charX(), panelY(), viciX(), panelY() + VICI_PANEL_H, 0x1800FF00);
            }
            // Draw the item centred on the cursor
            int ix = mx - 8, iy = my - 8;
            gfx.fill(ix - 1, iy - 1, ix + 17, iy + 17, C_ITEM_BG);
            drawBorder(gfx, ix - 1, iy - 1, 18, 18, C_ITEM_BORDER);
            gfx.renderItem(lootDragStack, ix, iy);
            gfx.renderItemDecorations(font, lootDragStack, ix, iy);
        }
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
                if (!s.isEmpty()) {
                    gfx.renderTooltip(font, s, mx, my);
                } else {
                    String tip = def.label();
                    if (def.source() == EqSource.HOTBAR)
                        tip += " (hotbar " + (def.sourceIdx() + 1) + ")";
                    gfx.renderTooltip(font, Component.literal(tip), mx, my);
                }
                return;
            }
        }

        // Pockets tooltip
        int pox = contX() + 2, poy = panelY() + 2 + 10;
        for (int i = 0; i < TarkovInventoryMenu.POCKETS_COUNT; i++) {
            int sx = pox + i * (SS + 3);
            if (mx >= sx && mx < sx + SS && my >= poy && my < poy + SS) {
                ItemStack s = menu.getPocketSlot(i);
                if (!s.isEmpty()) gfx.renderTooltip(font, s, mx, my);
                else gfx.renderTooltip(font,
                    Component.literal("POCKET " + (i + 1) + " (hotbar " + (i + 3) + ")"), mx, my);
                return;
            }
        }

        // Rig slot tooltip
        if (rigHoveredSlot >= 0) {
            IItemHandler handler = getRigItemHandler();
            if (handler != null && rigHoveredSlot < handler.getSlots()) {
                ItemStack s = handler.getStackInSlot(rigHoveredSlot);
                if (!s.isEmpty()) {
                    gfx.renderTooltip(font, s, mx, my);
                } else {
                    gfx.renderTooltip(font,
                            Component.literal("RIG SLOT " + (rigHoveredSlot + 1)), mx, my);
                }
            }
        }
    }

    // ── Vicinity panel ────────────────────────────────────────────────

    // ================================================================
    // Right panel — switches between LOOT mode (corpse) and GROUND mode
    // ================================================================

    private void renderVicinityPanel(@NotNull GuiGraphics gfx, int mx, int my) {
        CorpseClientCache.CorpseEntry corpseEntry = selectedCorpsePos == null
                ? null : CorpseClientCache.all().get(selectedCorpsePos);

        if (corpseEntry != null) {
            renderLootPanel(gfx, mx, my, corpseEntry);
        } else {
            renderGroundPanel(gfx, mx, my);
        }
    }

    // ── LOOT MODE ─────────────────────────────────────────────────────

    /**
     * Character-screen style loot panel.
     *
     * Layout (top → bottom):
     *   Title bar  (LOOT_TITLE_H=20px)  — "☠ Name's Corpse" + EXIT + nav arrows
     *   Sub-header (LOOT_SUBHDR_H=12px) — item count + "< 1/2 >" if multiple corpses
     *   Scrollable row list:
     *     Equipment rows (LOOT_ROW_H=18px) — maroon bg, icon + name + slot label
     *     Separator     (LOOT_SEP_H=10px)  — "─── INVENTORY (N) ───"
     *     Inventory rows (LOOT_ROW_H=18px) — dark bg, icon + name + count
     *   Footer (LOOT_FOOTER_H=22px) — TAKE ALL button
     */
    private void renderLootPanel(@NotNull GuiGraphics gfx, int mx, int my,
                                  CorpseClientCache.CorpseEntry corpse) {
        int ox = viciX(), oy = panelY();
        int pw = VICI_PANEL_W, ph = VICI_PANEL_H;

        // Panel shell
        gfx.fill(ox, oy, ox + pw, oy + ph, C_BG_PANEL);
        drawBorder(gfx, ox, oy, pw, ph, C_BORDER);

        // ── Title bar ──────────────────────────────────────────────────
        gfx.fill(ox, oy, ox + pw, oy + LOOT_TITLE_H, C_LOOT_HEADER);
        String title = "\u2620 " + corpse.ownerName() + "'s Corpse";
        gfx.drawString(font, title, ox + 4, oy + 6, C_LOOT_TITLE, false);

        // EXIT button (top-right)
        int exitW = 26, exitH = 12;
        int exitX = ox + pw - exitW - 3, exitY = oy + 4;
        lootExitHovered = mx >= exitX && mx < exitX + exitW && my >= exitY && my < exitY + exitH;
        gfx.fill(exitX, exitY, exitX + exitW, exitY + exitH,
                 lootExitHovered ? C_LOOT_EXIT_H : C_LOOT_EXIT);
        drawBorder(gfx, exitX, exitY, exitW, exitH, C_BORDER);
        gfx.drawString(font, "EXIT", exitX + 4, exitY + 2, C_TEXT_WHITE, false);

        // ── Sub-header ─────────────────────────────────────────────────
        int subY = oy + LOOT_TITLE_H;
        gfx.fill(ox, subY, ox + pw, subY + LOOT_SUBHDR_H, C_LOOT_SUBHDR);
        int total = corpse.totalCount();
        gfx.drawString(font, total + " item" + (total == 1 ? "" : "s"), ox + 4, subY + 2, C_TEXT_LABEL, false);

        // Corpse navigation arrows (multiple corpses nearby)
        lootNavLeftHov = false; lootNavRightHov = false;
        if (nearbyCorpseList.size() > 1) {
            int idx = nearbyCorpseList.indexOf(selectedCorpsePos);
            String nav = "< " + (idx + 1) + "/" + nearbyCorpseList.size() + " >";
            int navW = font.width(nav) + 8, navX = ox + pw / 2 - navW / 2;
            int arW = 10, arH = LOOT_SUBHDR_H;
            int lX = navX, rX = navX + navW - arW;
            lootNavLeftHov  = mx >= lX && mx < lX + arW && my >= subY && my < subY + arH;
            lootNavRightHov = mx >= rX && mx < rX + arW && my >= subY && my < subY + arH;
            gfx.fill(lX, subY, lX + arW, subY + arH, lootNavLeftHov  ? C_LOOT_NAV_H : C_LOOT_NAV);
            gfx.fill(rX, subY, rX + arW, subY + arH, lootNavRightHov ? C_LOOT_NAV_H : C_LOOT_NAV);
            gfx.drawString(font, nav, navX + 1, subY + 2, C_TEXT_LABEL, false);
        }

        // ── Scrollable row list ────────────────────────────────────────
        List<LootRow> rows = buildLootRows(corpse);

        int listTop = subY + LOOT_SUBHDR_H;
        int listBot = oy + ph - LOOT_FOOTER_H;
        int listH   = listBot - listTop;

        // Compute total pixel height of all rows
        int totalH = 0;
        for (LootRow r : rows) totalH += (r instanceof LootSepRow) ? LOOT_SEP_H : LOOT_ROW_H;

        boolean needsBar = totalH > listH;
        int sbW = 4, sbX = ox + pw - sbW - 1;
        if (needsBar) {
            int maxSc = Math.max(0, totalH - listH);
            lootScroll = Math.min(lootScroll, maxSc);
            gfx.fill(sbX, listTop, sbX + sbW, listBot, C_SCROLLBAR_BG);
            int thumbH = Math.max(10, listH * listH / totalH);
            int thumbY = listTop + (int)((long) lootScroll * (listH - thumbH) / maxSc);
            gfx.fill(sbX, thumbY, sbX + sbW, thumbY + thumbH, C_SCROLLBAR_FG);
        } else { lootScroll = 0; }

        int rowW = pw - (needsBar ? sbW + 1 : 1);
        gfx.enableScissor(ox, listTop, ox + rowW, listBot);
        lootHoveredRow = -1;
        ItemStack tooltipStack = ItemStack.EMPTY;
        int cursor = listTop - lootScroll;
        // Suppress hover highlights while the player is actively dragging an item out
        boolean suppressHover = lootDragging;

        for (int ri = 0; ri < rows.size(); ri++) {
            LootRow row = rows.get(ri);
            int rowH = (row instanceof LootSepRow) ? LOOT_SEP_H : LOOT_ROW_H;
            int rowY = cursor; cursor += rowH;
            if (rowY + rowH <= listTop) continue;
            if (rowY >= listBot) break;

            if (row instanceof LootSepRow sep) {
                // ── Separator ────────────────────────────────────────
                gfx.fill(ox, rowY, ox + rowW, rowY + rowH, 0xFF0E0808);
                String sepLabel = "\u2500\u2500 INVENTORY (" + sep.invCount() + ") \u2500\u2500";
                int sx = ox + (rowW - font.width(sepLabel)) / 2;
                gfx.drawString(font, sepLabel, sx, rowY + 1, 0xFF604848, false);

            } else if (row instanceof LootEquipRow equip) {
                // ── Equipment row (maroon tint) ───────────────────────
                int bg = (ri & 1) == 0 ? 0xFF180A0A : 0xFF200D0D;
                gfx.fill(ox, rowY, ox + rowW, rowY + rowH, bg);
                boolean hov = !suppressHover && mx >= ox && mx < ox + rowW && my >= rowY && my < rowY + rowH;
                if (hov) { gfx.fill(ox, rowY, ox + rowW, rowY + rowH, C_LOOT_HOVER);
                           lootHoveredRow = ri; tooltipStack = equip.stack(); }

                gfx.renderItem(equip.stack(), ox + 1, rowY + 1);
                gfx.renderItemDecorations(font, equip.stack(), ox + 1, rowY + 1);

                // Slot label (right, dim)
                String slotLabel = equipSlotLabel(equip.key());
                int slW = font.width(slotLabel);
                gfx.drawString(font, slotLabel, ox + rowW - slW - 2, rowY + 5, 0xFF604848, false);

                // Item name (truncated, left of label)
                int nameMaxW = rowW - 20 - slW - 6;
                String name = equip.stack().getHoverName().getString();
                while (name.length() > 1 && font.width(name) > nameMaxW)
                    name = name.substring(0, name.length() - 1);
                if (font.width(equip.stack().getHoverName().getString()) > nameMaxW) name += "…";
                gfx.drawString(font, name, ox + 19, rowY + 5, C_TEXT_WHITE, false);

            } else if (row instanceof LootInvRow inv) {
                // ── Inventory row ─────────────────────────────────────
                if ((ri & 1) == 0) gfx.fill(ox, rowY, ox + rowW, rowY + rowH, C_VICI_ROW_ODD);
                boolean hov = !suppressHover && mx >= ox && mx < ox + rowW && my >= rowY && my < rowY + rowH;
                if (hov) { gfx.fill(ox, rowY, ox + rowW, rowY + rowH, C_LOOT_HOVER);
                           lootHoveredRow = ri; tooltipStack = inv.stack(); }

                gfx.renderItem(inv.stack(), ox + 1, rowY + 1);
                gfx.renderItemDecorations(font, inv.stack(), ox + 1, rowY + 1);

                String countStr = inv.stack().getCount() > 1 ? "\u00d7" + inv.stack().getCount() : "";
                int cntX = ox + rowW - font.width(countStr) - 3;
                if (!countStr.isEmpty())
                    gfx.drawString(font, countStr, cntX, rowY + 5, C_VICI_DIST, false);

                int nameMaxW = (countStr.isEmpty() ? rowW - 22 : cntX - ox - 22) - 2;
                String name = inv.stack().getHoverName().getString();
                while (name.length() > 1 && font.width(name) > nameMaxW)
                    name = name.substring(0, name.length() - 1);
                if (font.width(inv.stack().getHoverName().getString()) > nameMaxW) name += "…";
                gfx.drawString(font, name, ox + 19, rowY + 5, C_TEXT_WHITE, false);
            }
        }

        gfx.disableScissor();

        if (corpse.isEmpty())
            gfx.drawString(font, "Corpse is empty", ox + 10, listTop + 10, C_TEXT_LABEL, false);

        // ── TAKE ALL button ────────────────────────────────────────────
        int taY = listBot + 3, taH = LOOT_FOOTER_H - 6;
        int taX = ox + 4,     taW = pw - 8;
        lootTakeAllHovered = mx >= taX && mx < taX + taW && my >= taY && my < taY + taH;
        gfx.fill(taX, taY, taX + taW, taY + taH,
                 lootTakeAllHovered ? C_LOOT_BTN_HOV : C_LOOT_BTN);
        drawBorder(gfx, taX, taY, taW, taH, C_BORDER);
        String taLabel = "\u2193 TAKE ALL (" + total + ")";
        gfx.drawString(font, taLabel, taX + (taW - font.width(taLabel)) / 2, taY + 3, C_TEXT_WHITE, false);

        if (!tooltipStack.isEmpty()) gfx.renderTooltip(font, tooltipStack, mx, my);
    }

    // ── GROUND MODE ───────────────────────────────────────────────────

    /** Builds the ground-only row list (no corpses — loot panel handles those). */
    private List<ViciEntry> buildViciRows() {
        List<ViciEntry> rows = new ArrayList<>();
        for (ItemEntity e : vicinityItems) rows.add(new ViciGround(e));
        return rows;
    }

    /** Standard ground-items vicinity panel (shown when no corpse is selected). */
    private void renderGroundPanel(@NotNull GuiGraphics gfx, int mx, int my) {
        int ox = viciX(), oy = panelY();

        viciRows = buildViciRows();

        // Panel shell
        gfx.fill(ox, oy, ox + VICI_PANEL_W, oy + VICI_PANEL_H, C_BG_PANEL);
        drawBorder(gfx, ox, oy, VICI_PANEL_W, VICI_PANEL_H, C_BORDER);

        // Header
        gfx.fill(ox, oy, ox + VICI_PANEL_W, oy + VICI_HEADER_H, C_VICI_HEADER);
        gfx.drawString(font, "VICINITY", ox + 4, oy + 4, C_TEXT_TITLE, false);

        String badge = vicinityItems.size() + " on ground";
        gfx.drawString(font, badge, ox + 4, oy + VICI_HEADER_H + 2, C_TEXT_LABEL, false);

        // LOOT ALL button
        int btnW = 44, btnH = 10;
        int btnX = ox + VICI_PANEL_W - btnW - 3, btnY = oy + 4;
        lootAllHovered = mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + btnH;
        gfx.fill(btnX, btnY, btnX + btnW, btnY + btnH,
                 lootAllHovered ? C_VICI_BTN_HOV : C_VICI_BTN);
        drawBorder(gfx, btnX, btnY, btnW, btnH, C_BORDER);
        gfx.drawString(font, "LOOT ALL", btnX + 2, btnY + 1, C_TEXT_WHITE, false);

        // Scrollable list
        int contentYTop = oy + VICI_HEADER_H + 12;
        int contentH    = VICI_PANEL_H - VICI_HEADER_H - 12;
        int totalH      = viciRows.size() * VICI_ROW_H;

        boolean needsScroll = totalH > contentH;
        int sbX = ox + VICI_PANEL_W - 5, sbW = 4;
        if (needsScroll) {
            int maxSc = Math.max(0, totalH - contentH);
            vicinityScroll = Math.min(vicinityScroll, maxSc);
            gfx.fill(sbX, contentYTop, sbX + sbW, contentYTop + contentH, C_SCROLLBAR_BG);
            int thumbH = Math.max(12, contentH * contentH / totalH);
            int thumbY = contentYTop + (int)((long) vicinityScroll * (contentH - thumbH) / maxSc);
            gfx.fill(sbX, thumbY, sbX + sbW, thumbY + thumbH, C_SCROLLBAR_FG);
        } else { vicinityScroll = 0; }

        int listW = VICI_PANEL_W - (needsScroll ? sbW + 1 : 1);
        gfx.enableScissor(ox, contentYTop, ox + listW, contentYTop + contentH);

        hoveredVicinityIdx = -1;
        ItemStack tooltipStack = ItemStack.EMPTY;

        for (int i = 0; i < viciRows.size(); i++) {
            if (!(viciRows.get(i) instanceof ViciGround g)) continue;
            ItemStack stack = g.entity().getItem();
            int rowY = contentYTop + i * VICI_ROW_H - vicinityScroll;
            if (rowY + VICI_ROW_H <= contentYTop) continue;
            if (rowY >= contentYTop + contentH)   break;

            if ((i & 1) == 1) gfx.fill(ox, rowY, ox + listW, rowY + VICI_ROW_H, C_VICI_ROW_ODD);
            boolean rowHov = mx >= ox && mx < ox + listW && my >= rowY && my < rowY + VICI_ROW_H;
            if (rowHov) { gfx.fill(ox, rowY, ox + listW, rowY + VICI_ROW_H, C_VICI_HOVER);
                          hoveredVicinityIdx = i; tooltipStack = stack; }

            gfx.renderItem(stack, ox + 2, rowY + 2);
            gfx.renderItemDecorations(font, stack, ox + 2, rowY + 2);

            double dist = minecraft.player == null ? 0 : g.entity().distanceTo(minecraft.player);
            String distStr = String.format("%.1fm", dist);
            int distX = ox + listW - font.width(distStr) - 3;
            gfx.drawString(font, distStr, distX, rowY + 6, C_VICI_DIST, false);

            int nameMaxW = distX - (ox + 20) - 2;
            String name = stack.getHoverName().getString();
            while (name.length() > 1 && font.width(name) > nameMaxW)
                name = name.substring(0, name.length() - 1);
            if (font.width(stack.getHoverName().getString()) > nameMaxW) name += "…";
            gfx.drawString(font, name, ox + 20, rowY + 6, C_TEXT_WHITE, false);
        }

        gfx.disableScissor();

        if (viciRows.isEmpty())
            gfx.drawString(font, "Nothing nearby", ox + 14, contentYTop + 10, C_TEXT_LABEL, false);
        if (!tooltipStack.isEmpty()) gfx.renderTooltip(font, tooltipStack, mx, my);
    }

    // ================================================================
    // Mouse input
    // ================================================================

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // ── Right panel clicks (loot mode or ground mode) ─────────────
        if (button == 0 && mx >= viciX() && mx < viciX() + VICI_PANEL_W) {
            // ── LOOT MODE ────────────────────────────────────────────
            if (selectedCorpsePos != null && CorpseClientCache.all().containsKey(selectedCorpsePos)) {
                CorpseClientCache.CorpseEntry corpse = CorpseClientCache.all().get(selectedCorpsePos);
                if (lootExitHovered) {
                    selectedCorpsePos = null; lootScroll = 0;
                } else if (lootTakeAllHovered) {
                    ModNetwork.CHANNEL.sendToServer(C2STakeFromCorpsePacket.takeAll(selectedCorpsePos));
                } else if (lootNavLeftHov && nearbyCorpseList.size() > 1) {
                    int idx = nearbyCorpseList.indexOf(selectedCorpsePos);
                    selectedCorpsePos = nearbyCorpseList.get(
                            (idx - 1 + nearbyCorpseList.size()) % nearbyCorpseList.size());
                    lootScroll = 0;
                } else if (lootNavRightHov && nearbyCorpseList.size() > 1) {
                    int idx = nearbyCorpseList.indexOf(selectedCorpsePos);
                    selectedCorpsePos = nearbyCorpseList.get((idx + 1) % nearbyCorpseList.size());
                    lootScroll = 0;
                } else if (lootHoveredRow >= 0 && corpse != null) {
                    List<LootRow> rows = buildLootRows(corpse);
                    if (lootHoveredRow < rows.size()) {
                        LootRow row = rows.get(lootHoveredRow);
                        ItemStack dragStack = ItemStack.EMPTY;
                        if (row instanceof LootEquipRow equip) dragStack = equip.stack();
                        else if (row instanceof LootInvRow inv)  dragStack = inv.stack();
                        if (!dragStack.isEmpty()) {
                            lootDragging      = true;
                            lootDragStack     = dragStack.copy();
                            lootDragCorpsePos = selectedCorpsePos;
                            lootDragSourceRow = lootHoveredRow;
                            lootDragStartX    = mx;
                            lootDragStartY    = my;
                        }
                    }
                }
                return true;
            }
            // ── GROUND MODE ──────────────────────────────────────────
            if (lootAllHovered && !vicinityItems.isEmpty()) {
                ModNetwork.CHANNEL.sendToServer(new C2SLootAllPacket());
                return true;
            }
            if (hoveredVicinityIdx >= 0 && hoveredVicinityIdx < viciRows.size()
                    && viciRows.get(hoveredVicinityIdx) instanceof ViciGround g) {
                ModNetwork.CHANNEL.sendToServer(new C2SPickupItemPacket(g.entity().getId()));
            }
            return true;
        }

        if (handleRigSlotClick((int) mx, (int) my, button)) return true;

        if (hasBackpackEquipped()) {
            int sbBaseX = contX() + 2 + font.width("BACKPACK") + 6;
            int sbBaseY = panelY() + 42 + rigSectionH;
            int sbW     = CONT_PANEL_W - (sbBaseX - contX()) - 4;
            if (mx >= sbBaseX && mx < sbBaseX + sbW && my >= sbBaseY && my < sbBaseY + 10) {
                searchActive = !searchActive;
                if (!searchActive) searchText = "";
                return true;
            }
            int col = toGridCol(mx), row = toGridRow(my);
            if (col >= 0 && row >= 0)
                return handleGridClick(col, row, (int) mx, (int) my, button);
        }

        if (handleEqSlotClick((int) mx, (int) my, button))  return true;
        if (handlePocketsClick((int) mx, (int) my, button)) return true;

        if (button == 1 && !dragging.isEmpty()) {
            draggingSize = draggingSize.rotated();
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (mx >= viciX() && mx < viciX() + VICI_PANEL_W) {
            // Loot mode — scroll the corpse item list
            if (selectedCorpsePos != null && CorpseClientCache.all().containsKey(selectedCorpsePos)) {
                lootScroll = Math.max(0, (int)(lootScroll - delta * VICI_ROW_H));
            } else {
                vicinityScroll = Math.max(0, (int)(vicinityScroll - delta * VICI_ROW_H));
            }
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (button == 0 && lootDragging) {
            // Snapshot and clear drag state immediately
            boolean wasDragging   = lootDragging;
            BlockPos  corpsePos   = lootDragCorpsePos;
            int       sourceRow   = lootDragSourceRow;
            double    startX      = lootDragStartX;
            double    startY      = lootDragStartY;
            lootDragging      = false;
            lootDragStack     = ItemStack.EMPTY;
            lootDragCorpsePos = null;
            lootDragSourceRow = -1;

            if (!wasDragging || corpsePos == null) return true;
            CorpseClientCache.CorpseEntry corpse = CorpseClientCache.all().get(corpsePos);
            if (corpse == null) return true;
            List<LootRow> rows = buildLootRows(corpse);
            if (sourceRow >= rows.size()) return true;
            LootRow row = rows.get(sourceRow);

            boolean isSmallMove = Math.abs(mx - startX) < 5 && Math.abs(my - startY) < 5;
            boolean overLootPanel = mx >= viciX() && mx < viciX() + VICI_PANEL_W;

            // Send the packet if: tiny click OR dragged onto the character/container panels
            if (isSmallMove || !overLootPanel) {
                if (row instanceof LootEquipRow equip)
                    ModNetwork.CHANNEL.sendToServer(
                            C2STakeFromCorpsePacket.namedSlot(corpsePos, equip.key()));
                else if (row instanceof LootInvRow inv)
                    ModNetwork.CHANNEL.sendToServer(
                            C2STakeFromCorpsePacket.inventorySlot(corpsePos, inv.idx()));
            }
            // Released back in loot panel → drag cancelled, item stays in corpse
            return true;
        }
        return super.mouseReleased(mx, my, button);
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
        if (searchActive) return true;
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
    // Equipment stack access (ARMOR / CAP / HOTBAR)
    // ================================================================

    /**
     * Returns the ItemStack for an equipment slot.
     * Priority: Curios slot (if mapped + loaded + slot type exists) → vanilla ARMOR / CAP / HOTBAR.
     */
    private ItemStack getEquipmentStack(Player player, EqSlotDef def) {
        // Curios override
        if (!def.curiosSlotId().isEmpty() && CuriosCompat.isLoaded()) {
            for (CuriosCompat.CuriosSlotEntry e : curiosSlots) {
                if (e.slotId().equals(def.curiosSlotId()) && e.index() == def.curiosSlotIndex())
                    return e.stack(); // may be EMPTY — that's fine, slot exists
            }
            // Slot type not present on this player — fall through to vanilla
        }
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
            case HOTBAR -> player.getInventory().getItem(def.sourceIdx());
        };
    }

    /**
     * Writes a stack to an equipment slot.
     * Writes to Curios when mapped + loaded, otherwise writes to vanilla ARMOR / CAP / HOTBAR.
     */
    private void setEquipmentStack(Player player, EqSlotDef def, ItemStack stack) {
        // Curios override
        if (!def.curiosSlotId().isEmpty() && CuriosCompat.isLoaded()) {
            boolean slotExists = curiosSlots.stream()
                    .anyMatch(e -> e.slotId().equals(def.curiosSlotId()) && e.index() == def.curiosSlotIndex());
            if (slotExists) {
                CuriosCompat.setSlot(player, def.curiosSlotId(), def.curiosSlotIndex(), stack);
                return;
            }
        }
        // Fall back to vanilla source
        switch (def.source()) {
            case ARMOR -> {
                if (def.sourceIdx() >= 0) {
                    player.setItemSlot(switch (def.sourceIdx()) {
                        case 0  -> EquipmentSlot.HEAD;
                        case 1  -> EquipmentSlot.CHEST;
                        case 2  -> EquipmentSlot.LEGS;
                        case 3  -> EquipmentSlot.FEET;
                        default -> EquipmentSlot.MAINHAND;
                    }, stack);
                }
            }
            case CAP -> {
                if (def.sourceIdx() >= 0)
                    ModCapabilities.get(player).ifPresent(c -> c.setSlot(def.sourceIdx(), stack));
            }
            case HOTBAR -> player.getInventory().setItem(def.sourceIdx(), stack);
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

    @Override
    protected void renderLabels(@NotNull GuiGraphics gfx, int mx, int my) { /* handled above */ }
}
