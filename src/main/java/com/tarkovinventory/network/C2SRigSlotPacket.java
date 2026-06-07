package com.tarkovinventory.network;

import com.tarkovinventory.compat.BackpackCompat;
import com.tarkovinventory.compat.BackpackCompat.RigTransaction;
import com.tarkovinventory.compat.CuriosCompat;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SRigSlotPacket {

    public static final byte SRC_CURIOS = 0;
    public static final byte SRC_ARMOR = 1;

    private final int slotIndex;
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

            ItemStack rig = ItemStack.EMPTY;

            if (msg.rigSource == SRC_CURIOS && CuriosCompat.isLoaded()) {
                rig = CuriosCompat.getSlotItem(player, "body", 0);
            }

            if (rig.isEmpty()) {
                rig = player.getItemBySlot(EquipmentSlot.CHEST);
            }

            if (rig.isEmpty()) return;

            RigTransaction tx = BackpackCompat.openRig(rig);

            if (!tx.isValidSlot(msg.slotIndex)) return;

            ItemStack taken = tx.inv.extractItem(msg.slotIndex, 64, false);
            if (taken.isEmpty()) return;

            ItemStack carried = player.containerMenu.getCarried();

            ItemStack leftover = ItemStack.EMPTY;

            if (carried.isEmpty()) {
                player.containerMenu.setCarried(taken);
            } else if (ItemStack.isSameItemSameTags(carried, taken)) {

                int space = carried.getMaxStackSize() - carried.getCount();
                int move = Math.min(space, taken.getCount());

                carried.grow(move);
                taken.shrink(move);

                leftover = taken;

            } else {
                leftover = taken;
            }

            if (!leftover.isEmpty()) {
                if (!player.getInventory().add(leftover)) {
                    drop(player, leftover);
                }
            }

            tx.commit();

            if (msg.rigSource == SRC_CURIOS && CuriosCompat.isLoaded()) {
                CuriosCompat.setSlot(player, "body", 0, rig);
            } else {
                player.setItemSlot(EquipmentSlot.CHEST, rig);
            }
        });

        ctx.get().setPacketHandled(true);
    }

    private static void drop(ServerPlayer player, ItemStack stack) {
        ItemEntity entity = new ItemEntity(
                player.level(),
                player.getX(),
                player.getY(),
                player.getZ(),
                stack
        );
        player.level().addFreshEntity(entity);
    }
}
