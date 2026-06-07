package com.tarkovinventory.service;

import com.tarkovinventory.inventory.RigInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class RigService {

    private RigService() {}

    // ─────────────────────────────
    // PUBLIC ENTRY POINT
    // ─────────────────────────────

    public static ItemStack extract(ServerPlayer player, ItemStack rig, int slot, int amount) {
        if (rig.isEmpty()) return ItemStack.EMPTY;

        RigInventory inv = load(rig);

        ItemStack taken = inv.extract(slot, amount);

        if (taken.isEmpty()) return ItemStack.EMPTY;

        save(rig, inv);
        syncRig(player, rig);

        return taken;
    }

    public static ItemStack insert(ServerPlayer player, ItemStack rig, int slot, ItemStack stack) {
        if (rig.isEmpty() || stack.isEmpty()) return stack;

        RigInventory inv = load(rig);

        ItemStack leftover = inv.set(slot, stack);

        save(rig, inv);
        syncRig(player, rig);

        return leftover;
    }

    // ─────────────────────────────
    // INTERNAL CORE
    // ─────────────────────────────

    private static RigInventory load(ItemStack rig) {
        CompoundTag tag = rig.getOrCreateTag();

        if (tag.contains("TarkovRigInventory")) {
            return RigInventory.load(tag.getCompound("TarkovRigInventory"));
        }

        return new RigInventory(9); // default fallback
    }

    private static void save(ItemStack rig, RigInventory inv) {
        CompoundTag tag = rig.getOrCreateTag();
        tag.put("TarkovRigInventory", inv.save());
    }

    // ─────────────────────────────
    // SYNC LAYER (IMPORTANT)
    // ─────────────────────────────

    private static void syncRig(ServerPlayer player, ItemStack rig) {

        // Curios support (safe fallback)
        if (com.tarkovinventory.compat.CuriosCompat.isLoaded()) {
            ItemStack cur = com.tarkovinventory.compat.CuriosCompat.getSlotItem(player, "body", 0);

            if (ItemStack.isSameItemSameTags(cur, rig)) {
                com.tarkovinventory.compat.CuriosCompat.setSlot(player, "body", 0, rig);
                return;
            }
        }

        // fallback armor slot
        player.setItemSlot(EquipmentSlot.CHEST, rig);
    }

    // ─────────────────────────────
    // HELPER
    // ─────────────────────────────

    public static ItemStack getRig(ServerPlayer player) {

        if (com.tarkovinventory.compat.CuriosCompat.isLoaded()) {
            ItemStack cur = com.tarkovinventory.compat.CuriosCompat.getSlotItem(player, "body", 0);
            if (!cur.isEmpty()) return cur;
        }

        return player.getItemBySlot(EquipmentSlot.CHEST);
    }
}
