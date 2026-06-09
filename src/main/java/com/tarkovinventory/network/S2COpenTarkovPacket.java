package com.tarkovinventory.network;

import com.tarkovinventory.client.screen.TarkovInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2COpenTarkovPacket {

    public static void encode(S2COpenTarkovPacket msg, FriendlyByteBuf buf) {}

    public static S2COpenTarkovPacket decode(FriendlyByteBuf buf) {
        return new S2COpenTarkovPacket();
    }

    public static void handle(S2COpenTarkovPacket msg, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new TarkovInventoryScreen());
        });

        ctx.get().setPacketHandled(true);
    }
}
