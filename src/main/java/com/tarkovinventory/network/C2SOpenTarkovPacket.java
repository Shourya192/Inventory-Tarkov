package com.tarkovinventory.network;

import com.tarkovinventory.block.TarkovCorpseBlockEntity;
import com.tarkovinventory.container.TarkovInventoryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Sent client → server when the player presses the Tarkov inventory key.
 *
 * <p>Opens the Tarkov inventory screen and immediately delivers the contents
 * of every nearby corpse block so the client-side vicinity panel can render them.
 */
public class C2SOpenTarkovPacket {

    /** How many blocks in each direction to scan for corpse blocks. */
    private static final int CORPSE_SCAN_RADIUS = 10;

    public C2SOpenTarkovPacket() {}

    public static void encode(C2SOpenTarkovPacket msg, FriendlyByteBuf buf) {}

    public static C2SOpenTarkovPacket decode(FriendlyByteBuf buf) {
        return new C2SOpenTarkovPacket();
    }

    public static void handle(C2SOpenTarkovPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            // ── Open inventory screen ───────────────────────────────────
            NetworkHooks.openScreen(player, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return Component.literal("Tarkov Inventory");
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(
                        int windowId, @NotNull Inventory inv, @NotNull Player p) {
                    return new TarkovInventoryMenu(windowId, inv, 0);
                }
            }, buf -> buf.writeInt(0));

            // ── Sync nearby corpse blocks to the client ─────────────────
            if (!(player.level() instanceof ServerLevel level)) return;
            BlockPos origin = player.blockPosition();
            int r = CORPSE_SCAN_RADIUS;

            for (BlockPos pos : BlockPos.betweenClosed(
                    origin.offset(-r, -3, -r),
                    origin.offset( r,  3,  r))) {

                if (level.getBlockEntity(pos) instanceof TarkovCorpseBlockEntity be) {
                    ModNetwork.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new S2CCorpseContentsPacket(
                                    pos.immutable(), be.getOwnerName(),
                                    be.getSlottedItems(), be.getInventoryItems()));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
