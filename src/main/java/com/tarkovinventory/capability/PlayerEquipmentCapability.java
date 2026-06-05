package com.tarkovinventory.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class PlayerEquipmentCapability implements IPlayerEquipment {

    private final ItemStack[] slots = new ItemStack[SLOT_COUNT];

    public PlayerEquipmentCapability() {
        for (int i = 0; i < SLOT_COUNT; i++) slots[i] = ItemStack.EMPTY;
    }

    @Override
    public ItemStack getSlot(int index) {
        return (index >= 0 && index < SLOT_COUNT) ? slots[index] : ItemStack.EMPTY;
    }

    @Override
    public void setSlot(int index, ItemStack stack) {
        if (index >= 0 && index < SLOT_COUNT) slots[index] = stack == null ? ItemStack.EMPTY : stack;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!slots[i].isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putInt("Slot", i);
                entry.put("Item", slots[i].save(new CompoundTag()));
                list.add(entry);
            }
        }
        tag.put("Slots", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        for (int i = 0; i < SLOT_COUNT; i++) slots[i] = ItemStack.EMPTY;
        ListTag list = tag.getList("Slots", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int idx = entry.getInt("Slot");
            if (idx >= 0 && idx < SLOT_COUNT) {
                slots[idx] = ItemStack.of(entry.getCompound("Item"));
            }
        }
    }
}
