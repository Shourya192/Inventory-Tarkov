package com.tarkovinventory.network;

import com.tarkovinventory.capability.ModCapabilities;
import com.tarkovinventory.compat.BackpackCompat;
import com.tarkovinventory.compat.CuriosCompat;
import com.tarkovinventory.inventory.GridInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → Server: place one item into a slot inside the player's equipped rig
 * custom inventory (curios "body" slot, or vanilla armor CHEST as fallback).
 *
 * The server inserts the item into the custom Tarkov RigInventory NBT and
 * removes a matching item from the player's containers (grid, main inventory).
 */
public class C2SRigPlacePacket {

    private final int slotIndex;
    private final byte rigSource;
    private final ItemStack itemToPlace;

    public C2SRigPlacePacket(int slotIndex, byte rigSource, ItemStack itemToPlace) {
        this.slotIndex = slotIndex;
        this.rigSource = rigSource;
        this.itemToPlace = itemToPlace;
    }

    public static void encode(C2SRigPlacePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slotIndex);
        buf.writeByte(msg.rigSource);
        buf.writeItem(msg.itemToPlace);
    }

    public static C2SRigPlacePacket decode(FriendlyByteBuf buf) {
        return new C2SRigPlacePacket(buf.readVarInt(), buf.readByte(), buf.readItem());
    }

    public static void handle(C2SRigPlacePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (msg.itemToPlace.isEmpty()) return;

            // Resolve rig item
            ItemStack rig = ItemStack.EMPTY;
            if (msg.rigSource == C2SRigSlotPacket.SRC_CURIOS && CuriosCompat.isLoaded()) {
                rig = CuriosCompat.getSlotItem(player, "body", 0);
            }
            if (rig.isEmpty()) rig = player.getItemBySlot(EquipmentSlot.CHEST);
            if (rig.isEmpty()) return;

            // Get custom RigInventory handler
            IItemHandler handler = BackpackCompat.getRigInventoryHandler(rig);
            if (handler == null || msg.slotIndex >= handler.getSlots()) return;

            // Verify target slot is empty
            if (!handler.getStackInSlot(msg.slotIndex).isEmpty()) return;

            // Remove one matching item from the player's containers
            ItemStack removed = removeMatchingItem(player, msg.itemToPlace);
            if (removed.isEmpty()) return;

            // Insert into rig custom inventory
            handler.insertItem(msg.slotIndex, removed, false);

            // Re-set rig in its parent slot so Forge/Curios detect the NBT change and sync
            if (msg.rigSource == C2SRigSlotPacket.SRC_CURIOS && CuriosCompat.isLoaded()) {
                CuriosCompat.setSlot(player, "body", 0, rig);
            } else {
                player.setItemSlot(EquipmentSlot.CHEST, rig);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    /**
     * Removes one matching item from the player's containers.
     * Search order: backpack grid → main inventory + hotbar → capability equipment.
     */
    private static ItemStack removeMatchingItem(ServerPlayer player, ItemStack target) {
        var cap = ModCapabilities.get(player).orElse(null);

        // 1. Try backpack grid (capability)
        if (cap != null) {
            GridInventory grid = cap.getGridInventory();
            for (int i = 0; i < grid.getContainerSize(); i++) {
                ItemStack s = grid.getItem(i);
                if (!s.isEmpty() && ItemStack.isSameItemSameTags(s, target)) {
                    ItemStack slotStack = grid.getItem(i);
if (!slotStack.isEmpty() && ItemStack.isSameItemSameTags(slotStack, target)) {
    ItemStack taken = slotStack.copyWithCount(1);

    slotStack.shrink(1);

    if (slotStack.isEmpty()) {
        grid.setItem(i, ItemStack.EMPTY);
    }

    return taken;
}
                }
            }
        }

        // 2. Try player main inventory + hotbar
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!s.isEmpty() && ItemStack.isSameItemSameTags(s, target)) {
                ItemStack taken = s.copyWithCount(1);
                s.shrink(1);
                if (s.isEmpty()) player.getInventory().setItem(i, ItemStack.EMPTY);
                return taken;
            }
        }

        // 3. Try capability equipment slots
        if (cap != null) {
            for (int i = 0; i < com.tarkovinventory.capability.IPlayerEquipment.SLOT_COUNT; i++) {
                ItemStack s = cap.getSlot(i);
                if (!s.isEmpty() && ItemStack.isSameItemSameTags(s, target)) {
                    cap.setSlot(i, ItemStack.EMPTY);
                    return s.copyWithCount(1);
                }
            }
        }

        return ItemStack.EMPTY;
    }
}
