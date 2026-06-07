package com.tarkovinventory.network;

import com.tarkovinventory.compat.BackpackCompat;
import com.tarkovinventory.compat.CuriosCompat;
import com.tarkovinventory.inventory.RigInventory;
import com.tarkovinventory.service.RigService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SRigSlotPacket {

    public static final byte SRC_CURIOS = 0;
    public static final byte SRC_ARMOR  = 1;

    private final int slot;
    private final byte source;

    public C2SRigSlotPacket(int slot, byte source) {
        this.slot = slot;
        this.source = source;
    }

    public static void encode(C2SRigSlotPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slot);
        buf.writeByte(msg.source);
    }

    public static C2SRigSlotPacket decode(FriendlyByteBuf buf) {
        return new C2SRigSlotPacket(buf.readVarInt(), buf.readByte());
    }

    public static void handle(C2SRigSlotPacket msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // ─────────────────────────────
            // GET RIG (single source of truth)
            // ─────────────────────────────
            ItemStack rig = RigService.getRig(player);
            if (rig.isEmpty()) return;

            // ─────────────────────────────
            // VALIDATE SLOT
            // ─────────────────────────────
            RigInventory inv = RigServiceTestLoad(rig); // temporary internal load
            if (msg.slot < 0 || msg.slot >= inv.size()) return;

            // ─────────────────────────────
            // SERVER AUTHORITATIVE ACTION
            // ─────────────────────────────
            ItemStack taken = RigService.extract(player, rig, msg.slot, 64);
            if (taken.isEmpty()) return;

            // ─────────────────────────────
            // GIVE ITEM TO PLAYER
            // ─────────────────────────────
            ItemStack carried = player.containerMenu.getCarried();

            if (carried.isEmpty()) {
                player.containerMenu.setCarried(taken);
            } else if (ItemStack.isSameItemSameTags(carried, taken)) {

                int space = carried.getMaxStackSize() - carried.getCount();
                int move = Math.min(space, taken.getCount());

                carried.grow(move);
                taken.shrink(move);

                if (!taken.isEmpty()) {
                    player.getInventory().add(taken);
                }

            } else {
                if (!player.getInventory().add(taken)) {
                    player.drop(taken, false);
                }
            }
        });

        ctx.get().setPacketHandled(true);
    }

    // ─────────────────────────────
    // TEMP FIX HELPER (we remove later)
    // ─────────────────────────────
    private static RigInventory RigServiceTestLoad(ItemStack rig) {
        return com.tarkovinventory.inventory.RigInventory.load(
                rig.getOrCreateTag().getCompound("TarkovRigInventory")
        );
    }
}
