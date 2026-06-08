package com.tarkovinventory.service;

import com.tarkovinventory.inventory.RigInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class RigController {

    private RigController() {}

    public static ItemStack extract(ServerPlayer player, int slot, int amount) {

        if (!RigLock.tryLock(player.getUUID())) return ItemStack.EMPTY;

        RigTransaction tx = new RigTransaction();

        ItemStack rig = RigSync.getRig(player);
        if (rig.isEmpty()) return ItemStack.EMPTY;

        RigInventory inv = RigService.load(rig);

        if (slot < 0 || slot >= inv.getSlots()) return ItemStack.EMPTY;

        ItemStack taken = inv.extractItem(slot, amount);
        if (taken.isEmpty()) return ItemStack.EMPTY;

        tx.add(() -> RigService.save(rig, inv));
        tx.add(() -> RigSync.syncRig(player, rig));

        tx.commit();

        return taken;
    }

    public static ItemStack insert(ServerPlayer player, int slot, ItemStack stack) {

        if (!RigLock.tryLock(player.getUUID())) return stack;

        RigTransaction tx = new RigTransaction();

        ItemStack rig = RigSync.getRig(player);
        if (rig.isEmpty() || stack.isEmpty()) return stack;

        RigInventory inv = RigService.load(rig);

        if (slot < 0 || slot >= inv.getSlots()) return stack;

        ItemStack leftover = inv.insertItem(slot, stack);

        tx.add(() -> RigService.save(rig, inv));
        tx.add(() -> RigSync.syncRig(player, rig));

        tx.commit();

        return leftover;
    }
}
