package com.tarkovinventory.service;

import com.tarkovinventory.inventory.RigInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class RigService {

    private RigService() {}

    private static final String TAG = "TarkovRigInventory";

    public static RigInventory load(ItemStack rig) {
        CompoundTag tag = rig.getOrCreateTag();

        if (tag.contains(TAG)) {
            return RigInventory.unwrapFromNBT(tag.getCompound(TAG));
        }

        return new RigInventory(3, 3);
    }

    public static void save(ItemStack rig, RigInventory inv) {
        rig.getOrCreateTag().put(TAG, inv.serializeNBT());
    }
}
