package com.tarkovinventory.capability;

import com.tarkovinventory.inventory.GridInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public class PlayerEquipmentCapability implements IPlayerEquipment {

    private final ItemStack[]  slots       = new ItemStack[SLOT_COUNT];
    private final GridInventory gridInventory = new GridInventory();
    private final ItemStack[]  pocketSlots = new ItemStack[POCKETS_COUNT];
    private final ItemStack[]  pouchSlots  = new ItemStack[POUCH_COUNT];

    public PlayerEquipmentCapability() {
        for (int i = 0; i < SLOT_COUNT;    i++) slots[i]       = ItemStack.EMPTY;
        for (int i = 0; i < POCKETS_COUNT; i++) pocketSlots[i] = ItemStack.EMPTY;
        for (int i = 0; i < POUCH_COUNT;   i++) pouchSlots[i]  = ItemStack.EMPTY;
    }

    // ── Equipment slots ───────────────────────────────────────────────

    @Override
    public ItemStack getSlot(int index) {
        return (index >= 0 && index < SLOT_COUNT) ? slots[index] : ItemStack.EMPTY;
    }

    @Override
    public void setSlot(int index, ItemStack stack) {
        if (index >= 0 && index < SLOT_COUNT)
            slots[index] = stack == null ? ItemStack.EMPTY : stack;
    }

    // ── Grid ──────────────────────────────────────────────────────────

    @Override
    public GridInventory getGridInventory() { return gridInventory; }

    // ── Pockets ───────────────────────────────────────────────────────

    @Override
    public ItemStack getPocketSlot(int index) {
        return (index >= 0 && index < POCKETS_COUNT) ? pocketSlots[index] : ItemStack.EMPTY;
    }

    @Override
    public void setPocketSlot(int index, ItemStack stack) {
        if (index >= 0 && index < POCKETS_COUNT)
            pocketSlots[index] = stack == null ? ItemStack.EMPTY : stack;
    }

    // ── Pouch ─────────────────────────────────────────────────────────

    @Override
    public ItemStack getPouchSlot(int index) {
        return (index >= 0 && index < POUCH_COUNT) ? pouchSlots[index] : ItemStack.EMPTY;
    }

    @Override
    public void setPouchSlot(int index, ItemStack stack) {
        if (index >= 0 && index < POUCH_COUNT)
            pouchSlots[index] = stack == null ? ItemStack.EMPTY : stack;
    }

    // ── NBT ───────────────────────────────────────────────────────────

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        // Equipment slots
        ListTag eqList = new ListTag();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!slots[i].isEmpty()) {
                CompoundTag e = new CompoundTag();
                e.putInt("Slot", i);
                e.put("Item", slots[i].save(new CompoundTag()));
                eqList.add(e);
            }
        }
        tag.put("Slots", eqList);

        // Grid
        tag.put("Grid", gridInventory.save());

        // Pockets
        ListTag pList = new ListTag();
        for (ItemStack s : pocketSlots) pList.add(s.save(new CompoundTag()));
        tag.put("Pockets", pList);

        // Pouch
        ListTag ouList = new ListTag();
        for (ItemStack s : pouchSlots) ouList.add(s.save(new CompoundTag()));
        tag.put("Pouch", ouList);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        // Equipment slots
        for (int i = 0; i < SLOT_COUNT; i++) slots[i] = ItemStack.EMPTY;
        ListTag eqList = tag.getList("Slots", Tag.TAG_COMPOUND);
        for (int i = 0; i < eqList.size(); i++) {
            CompoundTag e = eqList.getCompound(i);
            int idx = e.getInt("Slot");
            if (idx >= 0 && idx < SLOT_COUNT)
                slots[idx] = ItemStack.of(e.getCompound("Item"));
        }

        // Grid
        if (tag.contains("Grid")) gridInventory.load(tag.getCompound("Grid"));

        // Pockets
        ListTag pList = tag.getList("Pockets", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(pList.size(), POCKETS_COUNT); i++)
            pocketSlots[i] = ItemStack.of(pList.getCompound(i));

        // Pouch
        ListTag ouList = tag.getList("Pouch", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(ouList.size(), POUCH_COUNT); i++)
            pouchSlots[i] = ItemStack.of(ouList.getCompound(i));
    }
}
