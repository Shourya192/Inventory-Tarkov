package com.tarkovinventory.network;

import com.tarkovinventory.container.TarkovInventoryMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

/**
 * Sent from client → server when the player presses the Tarkov inventory key.
 * The server responds by opening the TarkovInventoryMenu for that player.
 */
public class C2SOpenTarkovPacket {

    public C2SOpenTarkovPacket() {}

    public static void encode(C2SOpenTarkovPacket msg, FriendlyByteBuf buf) {}

    public static C2SOpenTarkovPacket decode(FriendlyByteBuf buf) {
        return new C2SOpenTarkovPacket();
    }

    public static void handle(C2SOpenTarkovPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            // Don't open if another screen is already open server-side
            NetworkHooks.openScreen(player,
                    new net.minecraft.world.MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.literal("Tarkov Inventory");
                        }

                        @Override
                        public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                                int windowId,
                                net.minecraft.world.entity.player.Inventory inv,
                                net.minecraft.world.entity.player.Player p) {
                            return new TarkovInventoryMenu(windowId, inv);
                        }
                    });
        });
        ctx.get().setPacketHandled(true);
    }
}
