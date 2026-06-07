package com.tarkovinventory.inventory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public class RigInventory {

    private ItemStack[] items;

    public RigInventory(int size) {
        this.items = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            items[i] = ItemStack.EMPTY;
        }
    }

    // ─────────────────────────────
    // CORE API (LOCKED)
    // ─────────────────────────────

    public int size() {
        return items.length;
    }

    public ItemStack get(int slot) {
        return valid(slot) ? items[slot] : ItemStack.EMPTY;
    }

    public ItemStack set(int slot, ItemStack stack) {
        if (!valid(slot)) return stack;

        ItemStack old = items[slot];
        items[slot] = stack;

        return old;
    }

    public ItemStack extract(int slot, int amount) {
        if (!valid(slot)) return ItemStack.EMPTY;

        ItemStack stack = items[slot];
        if (stack.isEmpty()) return ItemStack.EMPTY;

        int take = Math.min(amount, stack.getCount());

        ItemStack result = stack.copy();
        result.setCount(take);

        stack.shrink(take);

        if (stack.getCount() <= 0) {
            items[slot] = ItemStack.EMPTY;
        }

        return result;
    }

    private boolean valid(int slot) {
        return slot >= 0 && slot < items.length;
    }

    // ─────────────────────────────
    // SERIALIZATION API
    // ─────────────────────────────

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();

        for (int i = 0; i < items.length; i++) {
            if (!items[i].isEmpty()) {
                CompoundTag c = new CompoundTag();
                c.putByte("Slot", (byte) i);
                items[i].save(c);
                list.add(c);
            }
        }

        tag.put("Items", list);
        tag.putInt("Size", items.length);

        return tag;
    }

    public static RigInventory load(CompoundTag tag) {
        int size = tag.getInt("Size");
        if (size <= 0) size = 9;

        RigInventory inv = new RigInventory(size);

        ListTag list = tag.getList("Items", 10);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag c = list.getCompound(i);
            int slot = c.getByte("Slot") & 255;

            if (slot >= 0 && slot < size) {
                inv.items[slot] = ItemStack.of(c);
            }
        }

        return inv;
    }
}
