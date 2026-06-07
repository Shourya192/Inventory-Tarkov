package com.tarkovinventory.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

/**
 * DUPE-SAFE RULES:
 * - No method may leave inventory in half-mutated state
 * - All operations must be atomic
 * - Slot access is always validated
 */
public class RigInventory {

    private ItemStack[] items;
    private int cols;
    private int rows;

    public RigInventory(int cols, int rows) {
        this.cols = Math.max(1, cols);
        this.rows = Math.max(1, rows);
        this.items = new ItemStack[getSlots()];
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.EMPTY;
        }
    }

    // ─────────────────────────────────────────────
    // BASIC INFO
    // ─────────────────────────────────────────────

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public int getSlots() {
        return cols * rows;
    }

    // ─────────────────────────────────────────────
    // SAFE SLOT ACCESS
    // ─────────────────────────────────────────────

    public ItemStack getItem(int slot) {
        if (!isValid(slot)) return ItemStack.EMPTY;
        return items[slot];
    }

    // ─────────────────────────────────────────────
    // ATOMIC INSERT
    // ─────────────────────────────────────────────

    public ItemStack insertItem(int slot, ItemStack stack) {
        if (stack.isEmpty() || !isValid(slot)) {
            return stack;
        }

        ItemStack existing = items[slot];

        // empty slot → full insert
        if (existing.isEmpty()) {
            items[slot] = stack.copy();
            return ItemStack.EMPTY;
        }

        // same item → merge safely
        if (ItemStack.isSameItemSameTags(existing, stack)) {

            int max = Math.min(existing.getMaxStackSize(), getSlotLimit(slot));
            int space = max - existing.getCount();

            if (space <= 0) return stack;

            int move = Math.min(space, stack.getCount());

            existing.grow(move);
            stack.shrink(move);

            return stack;
        }

        // cannot insert
        return stack;
    }

    // ─────────────────────────────────────────────
    // ATOMIC EXTRACT (CRITICAL FIX AREA)
    // ─────────────────────────────────────────────

    public ItemStack extractItem(int slot, int amount) {
        if (!isValid(slot) || amount <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = items[slot];
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int extracted = Math.min(amount, existing.getCount());

        ItemStack result = existing.copy();
        result.setCount(extracted);

        existing.shrink(extracted);

        if (existing.getCount() <= 0) {
            items[slot] = ItemStack.EMPTY;
        }

        return result;
    }

    // ─────────────────────────────────────────────
    // SLOT LIMIT
    // ─────────────────────────────────────────────

    public int getSlotLimit(int slot) {
        return 64;
    }

    // ─────────────────────────────────────────────
    // VALIDATION (IMPORTANT)
    // ─────────────────────────────────────────────

    public boolean isValid(int slot) {
        return slot >= 0 && slot < items.length;
    }

    // ─────────────────────────────────────────────
    // RESIZE (SAFE VERSION)
    // ─────────────────────────────────────────────

    public void setSize(int newCols, int newRows) {
        int newSize = Math.max(1, newCols) * Math.max(1, newRows);

        ItemStack[] newItems = new ItemStack[newSize];
        for (int i = 0; i < newSize; i++) {
            newItems[i] = ItemStack.EMPTY;
        }

        int copy = Math.min(items.length, newSize);

        for (int i = 0; i < copy; i++) {
            newItems[i] = items[i];
        }

        this.cols = newCols;
        this.rows = newRows;
        this.items = newItems;
    }

    // ─────────────────────────────────────────────
    // NBT SAVE
    // ─────────────────────────────────────────────

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();

        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putByte("Slot", (byte) i);
                items[i].save(entry);
                list.add(entry);
            }
        }

        tag.put("Items", list);
        tag.putInt("Cols", cols);
        tag.putInt("Rows", rows);

        return tag;
    }

    // ─────────────────────────────────────────────
    // NBT LOAD (SAFE)
    // ─────────────────────────────────────────────

    public void deserializeNBT(CompoundTag tag) {

        this.cols = tag.getInt("Cols");
        this.rows = tag.getInt("Rows");

        int size = Math.max(1, cols) * Math.max(1, rows);
        this.items = new ItemStack[size];

        for (int i = 0; i < size; i++) {
            items[i] = ItemStack.EMPTY;
        }

        ListTag list = tag.getList("Items", 10);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);

            int slot = entry.getByte("Slot") & 255;

            if (slot >= 0 && slot < items.length) {
                items[slot] = ItemStack.of(entry);
            }
        }
    }
}
