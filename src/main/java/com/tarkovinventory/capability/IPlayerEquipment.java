package com.tarkovinventory.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Stores the custom Tarkov equipment slots that don't exist in vanilla:
 * ON_SLING, ON_BACK, HOLSTER, SCABBARD, EARPIECE, ARMBAND.
 *
 * Vanilla armor (helmet/chestplate/leggings/boots) is read directly from
 * the player's armor inventory.
 */
public interface IPlayerEquipment {

    int SLOT_EARPIECE  = 0;
    int SLOT_ARMBAND   = 1;
    int SLOT_ON_SLING  = 2;
    int SLOT_HOLSTER   = 3;
    int SLOT_ON_BACK   = 4;
    int SLOT_SCABBARD  = 5;
    int SLOT_COUNT     = 6;

    ItemStack getSlot(int index);
    void setSlot(int index, ItemStack stack);

    CompoundTag serializeNBT();
    void deserializeNBT(CompoundTag tag);
}
