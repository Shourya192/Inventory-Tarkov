package com.tarkovinventory.network;

import com.tarkovinventory.service.RigController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SRigPlacePacket {

    private final int slotIndex;
    private final ItemStack itemToPlace;

    public C2SRigPlacePacket(int slotIndex, ItemStack itemToPlace) {
        this.slotIndex = slotIndex;
        this.itemToPlace = itemToPlace;
    }

    public static void encode(C2SRigPlacePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slotIndex);
        buf.writeItem(msg.itemToPlace);
    }

    public static C2SRigPlacePacket decode(FriendlyByteBuf buf) {
        return new C2SRigPlacePacket(buf.readVarInt(), buf.readItem());
    }

    public static void handle(C2SRigPlacePacket msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {

            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (msg.itemToPlace.isEmpty()) return;

            // SINGLE AUTHORITY ACTION
            ItemStack leftover = RigController.insert(player, msg.slotIndex, msg.itemToPlace.copy());

            // if something couldn't be inserted → return it to player
            if (!leftover.isEmpty()) {
                if (!player.getInventory().add(leftover)) {
                    player.drop(leftover, false);
                }
            }
        });

        ctx.get().setPacketHandled(true);
    }
}
