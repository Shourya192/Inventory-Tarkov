package com.tarkovinventory.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/**
 * Custom NBT-serialized inventory for rig items.
 * Stores items independently of the rig's original mod inventory.
 * Each rig item has its own RigInventory instance stored in player capability.
 */
public class RigInventory {

    private ItemStack[] items;
    private int cols;
    private int rows;
    private static final String NBT_KEY = "TarkovRigInventory";
    private static final String NBT_ITEMS = "Items";
    private static final String NBT_COLS = "Cols";
    private static final String NBT_ROWS = "Rows";

    public RigInventory(int cols, int rows) {
        this.cols = Math.min(cols, GridInventory.MAX_COLS);
        this.rows = Math.min(rows, GridInventory.MAX_ROWS);
        this.items = new ItemStack[this.cols * this.rows];
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.EMPTY;
        }
    }

    public int getCols() { return cols; }
    public int getRows() { return rows; }
    public int getSlots() { return items.length; }

    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= items.length) return ItemStack.EMPTY;
        return items[slot];
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < items.length) {
            items[slot] = stack;
        }
    }

    public ItemStack extractItem(int slot, int amount) {
        if (slot < 0 || slot >= items.length) return ItemStack.EMPTY;
        ItemStack stack = items[slot];
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack extracted = stack.split(amount);
        if (stack.isEmpty()) {
            items[slot] = ItemStack.EMPTY;
        }
        return extracted;
    }

    public void insertItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= items.length) return;
        if (items[slot].isEmpty()) {
            items[slot] = stack.copy();
        } else if (ItemStack.isSameItemSameTags(items[slot], stack)) {
            items[slot].grow(stack.getCount());
        }
    }

    public void setSize(int newCols, int newRows) {
        newCols = Math.min(newCols, GridInventory.MAX_COLS);
        newRows = Math.min(newRows, GridInventory.MAX_ROWS);

        if (newCols == this.cols && newRows == this.rows) return;

        ItemStack[] oldItems = this.items;
        this.cols = newCols;
        this.rows = newRows;
        this.items = new ItemStack[newCols * newRows];
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.EMPTY;
        }

        // Transfer items from old array to new (in order, up to capacity)
        int transferCount = Math.min(oldItems.length, this.items.length);
        for (int i = 0; i < transferCount; i++) {
            if (!oldItems[i].isEmpty()) {
                items[i] = oldItems[i].copy();
            }
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(NBT_COLS, cols);
        tag.putInt(NBT_ROWS, rows);

        ListTag itemsList = new ListTag();
        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                items[i].save(itemTag);
                itemsList.add(itemTag);
            }
        }
        tag.put(NBT_ITEMS, itemsList);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(NBT_COLS)) this.cols = tag.getInt(NBT_COLS);
        if (tag.contains(NBT_ROWS)) this.rows = tag.getInt(NBT_ROWS);
        this.items = new ItemStack[cols * rows];
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.EMPTY;
        }

        ListTag itemsList = tag.getList(NBT_ITEMS, Tag.TAG_COMPOUND);
        for (int i = 0; i < itemsList.size(); i++) {
            CompoundTag itemTag = itemsList.getCompound(i);
            int slot = itemTag.getInt("Slot");
            if (slot >= 0 && slot < items.length) {
                items[slot] = ItemStack.of(itemTag);
            }
        }
    }

    public static CompoundTag wrapInNBT(RigInventory inv) {
        CompoundTag wrapper = new CompoundTag();
        wrapper.put(NBT_KEY, inv.serializeNBT());
        return wrapper;
    }

    public static RigInventory unwrapFromNBT(CompoundTag wrapper) {
        if (!wrapper.contains(NBT_KEY)) {
            return new RigInventory(RigSizes.DEFAULT_COLS, RigSizes.DEFAULT_ROWS);
        }
        RigInventory inv = new RigInventory(RigSizes.DEFAULT_COLS, RigSizes.DEFAULT_ROWS);
        inv.deserializeNBT(wrapper.getCompound(NBT_KEY));
        return inv;
    }
}
