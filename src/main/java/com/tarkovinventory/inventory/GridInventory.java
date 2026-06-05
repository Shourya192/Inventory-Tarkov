package com.tarkovinventory.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A grid-based inventory that tracks the position of each ItemStack on a 2-D grid.
 *
 * Grid coordinates:
 *   - origin is top-left (0, 0)
 *   - x increases to the right, y increases downward
 *
 * Each placed stack occupies cells from (gridX, gridY) up to
 * (gridX + width - 1, gridY + height - 1).  The slot index used
 * internally is: slotIndex = gridY * COLS + gridX  (anchor cell only).
 * The {@code occupied} boolean array marks every covered cell so
 * collision detection is O(1).
 */
public class GridInventory extends SimpleContainer {

    public static final int COLS = 8;
    public static final int ROWS = 8;
    public static final int TOTAL_CELLS = COLS * ROWS;

    /** gridX stored per slot index (anchor column). */
    private final int[] slotX = new int[TOTAL_CELLS];
    /** gridY stored per slot index (anchor row). */
    private final int[] slotY = new int[TOTAL_CELLS];
    /** GridSize stored per slot index. */
    private final GridSize[] slotSize = new GridSize[TOTAL_CELLS];
    /** Fast lookup: is this cell occupied? */
    private final boolean[] occupied = new boolean[TOTAL_CELLS];

    public GridInventory() {
        super(TOTAL_CELLS);
        for (int i = 0; i < TOTAL_CELLS; i++) {
            slotSize[i] = GridSize.ONE_BY_ONE;
        }
    }

    // ---------------------------------------------------------------
    // Placement helpers
    // ---------------------------------------------------------------

    /**
     * Returns true if the given grid region is fully free.
     */
    public boolean canPlace(int gridX, int gridY, GridSize size) {
        if (gridX < 0 || gridY < 0
                || gridX + size.width() > COLS
                || gridY + size.height() > ROWS) {
            return false;
        }
        for (int dy = 0; dy < size.height(); dy++) {
            for (int dx = 0; dx < size.width(); dx++) {
                if (occupied[(gridY + dy) * COLS + (gridX + dx)]) return false;
            }
        }
        return true;
    }

    /**
     * Places the item at the given grid position (anchor = top-left).
     * Returns the slot index used, or -1 if placement failed.
     */
    public int placeItem(ItemStack stack, int gridX, int gridY, GridSize size) {
        if (!canPlace(gridX, gridY, size)) return -1;

        int slotIdx = gridY * COLS + gridX;
        setItem(slotIdx, stack);
        slotX[slotIdx] = gridX;
        slotY[slotIdx] = gridY;
        slotSize[slotIdx] = size;

        markOccupied(gridX, gridY, size, true);
        return slotIdx;
    }

    /**
     * Removes the item whose anchor is at the given slot index and frees occupied cells.
     */
    public ItemStack removeItem(int slotIdx) {
        ItemStack stack = getItem(slotIdx);
        if (!stack.isEmpty()) {
            markOccupied(slotX[slotIdx], slotY[slotIdx], slotSize[slotIdx], false);
            slotSize[slotIdx] = GridSize.ONE_BY_ONE;
            setItem(slotIdx, ItemStack.EMPTY);
        }
        return stack;
    }

    /**
     * Tries to auto-place an item by scanning for the first fitting free region.
     * Returns the slot index used, or -1 if there is no room.
     */
    public int autoPlace(ItemStack stack) {
        GridSize size = GridItemSizes.getSize(stack.getItem());
        for (int row = 0; row <= ROWS - size.height(); row++) {
            for (int col = 0; col <= COLS - size.width(); col++) {
                int idx = placeItem(stack, col, row, size);
                if (idx >= 0) return idx;
            }
        }
        return -1;
    }

    // ---------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------

    public int getSlotX(int slotIdx) { return slotX[slotIdx]; }
    public int getSlotY(int slotIdx) { return slotY[slotIdx]; }
    public GridSize getSlotSize(int slotIdx) { return slotSize[slotIdx]; }
    public boolean isCellOccupied(int col, int row) { return occupied[row * COLS + col]; }

    /**
     * Returns the anchor slot index for the cell at (col, row), or -1 if empty.
     */
    public int getAnchorSlot(int col, int row) {
        if (!occupied[row * COLS + col]) return -1;
        for (int i = 0; i < TOTAL_CELLS; i++) {
            if (getItem(i).isEmpty()) continue;
            int ax = slotX[i], ay = slotY[i];
            GridSize s = slotSize[i];
            if (col >= ax && col < ax + s.width() && row >= ay && row < ay + s.height()) {
                return i;
            }
        }
        return -1;
    }

    // ---------------------------------------------------------------
    // NBT persistence
    // ---------------------------------------------------------------

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (int i = 0; i < TOTAL_CELLS; i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("SlotIndex", i);
                entry.putInt("GridX", slotX[i]);
                entry.putInt("GridY", slotY[i]);
                entry.putInt("SizeW", slotSize[i].width());
                entry.putInt("SizeH", slotSize[i].height());
                entry.put("Item", stack.save(new CompoundTag()));
                list.add(entry);
            }
        }
        tag.put("Items", list);
        return tag;
    }

    public void load(CompoundTag tag) {
        // Clear first
        for (int i = 0; i < TOTAL_CELLS; i++) {
            setItem(i, ItemStack.EMPTY);
            occupied[i] = false;
            slotSize[i] = GridSize.ONE_BY_ONE;
        }

        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int idx = entry.getInt("SlotIndex");
            int gx  = entry.getInt("GridX");
            int gy  = entry.getInt("GridY");
            int sw  = entry.getInt("SizeW");
            int sh  = entry.getInt("SizeH");
            ItemStack stack = ItemStack.of(entry.getCompound("Item"));
            if (!stack.isEmpty() && idx >= 0 && idx < TOTAL_CELLS) {
                setItem(idx, stack);
                slotX[idx] = gx;
                slotY[idx] = gy;
                GridSize gs = new GridSize(sw, sh);
                slotSize[idx] = gs;
                markOccupied(gx, gy, gs, true);
            }
        }
    }

    // ---------------------------------------------------------------
    // Internal
    // ---------------------------------------------------------------

    private void markOccupied(int gx, int gy, GridSize size, boolean value) {
        for (int dy = 0; dy < size.height(); dy++) {
            for (int dx = 0; dx < size.width(); dx++) {
                int cell = (gy + dy) * COLS + (gx + dx);
                if (cell >= 0 && cell < TOTAL_CELLS) {
                    occupied[cell] = value;
                }
            }
        }
    }
}
