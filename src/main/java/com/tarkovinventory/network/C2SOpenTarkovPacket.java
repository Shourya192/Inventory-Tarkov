package com.tarkovinventory.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class C2SOpenTarkovPacket {

    public static void encode(C2SOpenTarkovPacket msg, FriendlyByteBuf buf) {}

    public static C2SOpenTarkovPacket decode(FriendlyByteBuf buf) {
        return new C2SOpenTarkovPacket();
    }

    public static void handle(C2SOpenTarkovPacket msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {

            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new S2COpenTarkovPacket()
            );
        });

        ctx.get().setPacketHandled(true);
    }
}
