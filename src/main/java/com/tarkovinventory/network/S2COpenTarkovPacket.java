package com.tarkovinventory.network;

import com.tarkovinventory.client.screen.TarkovInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2COpenTarkovPacket {

    public S2COpenTarkovPacket() {}

    public S2COpenTarkovPacket(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new TarkovInventoryScreen());
        });

        ctx.get().setPacketHandled(true);
        return true;
    }
}
