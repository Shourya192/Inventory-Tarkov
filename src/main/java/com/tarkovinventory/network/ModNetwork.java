package com.tarkovinventory.network;

import com.tarkovinventory.TarkovInventoryMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {

    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TarkovInventoryMod.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int id = 0;

    public static void register() {
        CHANNEL.registerMessage(id++,
                C2SOpenTarkovPacket.class,
                C2SOpenTarkovPacket::encode,
                C2SOpenTarkovPacket::decode,
                C2SOpenTarkovPacket::handle);
    }
}
