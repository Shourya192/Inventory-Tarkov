package com.tarkovinventory.network;

import com.tarkovinventory.compat.BackpackCompat;
import com.tarkovinventory.compat.CuriosCompat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → Server: take one item from a slot inside the player's equipped rig
 * (curios "body" slot, or vanilla armor CHEST as fallback).
 *
 * The rig item's IItemHandler capability is used so that any container-item
 * from any mod works automatically — no special-casing per mod required.
 */
public class C2SRigSlotPacket {

    /** Which slot holds the rig. */
    public static final byte SRC_CURIOS = 0;
    public static final byte SRC_ARMOR  = 1;

    private final int  slotIndex;
    private final byte rigSource;

    public C2SRigSlotPacket(int slotIndex, byte rigSource) {
        this.slotIndex = slotIndex;
        this.rigSource = rigSource;
    }

    public static void encode(C2SRigSlotPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slotIndex);
        buf.writeByte(msg.rigSource);
    }

    public static C2SRigSlotPacket decode(FriendlyByteBuf buf) {
        return new C2SRigSlotPacket(buf.readVarInt(), buf.readByte());
    }

    public static void handle(C2SRigSlotPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // Resolve rig item
            ItemStack rig = ItemStack.EMPTY;
            if (msg.rigSource == SRC_CURIOS && CuriosCompat.isLoaded()) {
                rig = CuriosCompat.getSlotItem(player, "body", 0);
            }
            if (rig.isEmpty()) rig = player.getItemBySlot(EquipmentSlot.CHEST);
            if (rig.isEmpty()) return;

            // BackpackCompat.extractFromRig: tries IItemHandler capability first,
            // then falls back to NBT read→extract→write for mods like Modern Mayhem
            // that store their inventory in item NBT without a Forge capability.
            ItemStack taken = BackpackCompat.extractFromRig(rig, msg.slotIndex);
            if (!taken.isEmpty()) {
                // Re-set rig in its parent slot so Forge/Curios detect the NBT change and sync
                if (msg.rigSource == SRC_CURIOS && CuriosCompat.isLoaded()) {
    CuriosCompat.setSlot(player, "body", 0, rig.copy());
} else {
    player.setItemSlot(EquipmentSlot.CHEST, rig.copy());
}

                // Give item to player inventory or drop at feet
                if (!player.getInventory().add(taken)) {
                    player.level().addFreshEntity(new ItemEntity(
                            player.level(), player.getX(), player.getY(), player.getZ(), taken));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
