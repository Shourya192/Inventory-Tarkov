package com.tarkovinventory.network;

import com.tarkovinventory.compat.BackpackCompat;
import com.tarkovinventory.compat.CuriosCompat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → Server: take one item from a slot inside the player's equipped rig
 * (curios "body" slot, or vanilla armor CHEST as fallback).
 *
 * Uses the custom Tarkov RigInventory stored in the rig item's NBT,
 * independent of the rig's original mod inventory.
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

        // Extract from custom Tarkov RigInventory
        IItemHandler handler = BackpackCompat.getRigInventoryHandler(rig);
        if (handler == null || msg.slotIndex >= handler.getSlots()) return;

        ItemStack taken = handler.extractItem(msg.slotIndex, 64, false);
        if (!taken.isEmpty()) {

            // Sync rig changes
            if (msg.rigSource == SRC_CURIOS && CuriosCompat.isLoaded()) {
                CuriosCompat.setSlot(player, "body", 0, rig);
            } else {
                player.setItemSlot(EquipmentSlot.CHEST, rig);
            }

            ItemStack carried = player.containerMenu.getCarried();

            if (carried.isEmpty()) {
                player.containerMenu.setCarried(taken);
            } else if (ItemStack.isSameItemSameTags(carried, taken)
                    && carried.getCount() < carried.getMaxStackSize()) {

                int space = carried.getMaxStackSize() - carried.getCount();
                int amount = Math.min(space, taken.getCount());

                carried.grow(amount);
                taken.shrink(amount);

                if (!taken.isEmpty() && !player.getInventory().add(taken)) {
                    ItemEntity entity = new ItemEntity(
                            player.level(),
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            taken
                    );
                    player.level().addFreshEntity(entity);
                }

            } else if (!player.getInventory().add(taken)) {

                ItemEntity entity = new ItemEntity(
                        player.level(),
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        taken
                );
                player.level().addFreshEntity(entity);
            }
        }
    });

    ctx.get().setPacketHandled(true);
}
}
