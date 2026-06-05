package com.tarkovinventory.network;

import com.tarkovinventory.block.TarkovCorpseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Supplier;

/**
 * Client → Server: take one item (or all items) from a nearby corpse block.
 *
 * <p>{@code slot == -1} means "take everything".
 * The server validates range, modifies the block entity, and sends back an
 * updated {@link S2CCorpseContentsPacket} (or an empty one if fully looted).
 */
public class C2STakeFromCorpsePacket {

    private static final double MAX_RANGE_SQ = 10.0 * 10.0; // 10 block radius

    private final BlockPos pos;
    private final int      slot;

    public C2STakeFromCorpsePacket(BlockPos pos, int slot) {
        this.pos  = pos;
        this.slot = slot;
    }

    public static void encode(C2STakeFromCorpsePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.slot);
    }

    public static C2STakeFromCorpsePacket decode(FriendlyByteBuf buf) {
        return new C2STakeFromCorpsePacket(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(C2STakeFromCorpsePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!(player.level() instanceof ServerLevel level)) return;

            // Range check
            if (msg.pos.distSqr(player.blockPosition()) > MAX_RANGE_SQ) return;

            if (!(level.getBlockEntity(msg.pos) instanceof TarkovCorpseBlockEntity be)) return;

            if (msg.slot == -1) {
                // Take all
                List<ItemStack> taken = be.takeAll();
                for (ItemStack stack : taken) giveOrDrop(player, level, stack);
            } else {
                ItemStack taken = be.takeItem(msg.slot);
                if (!taken.isEmpty()) giveOrDrop(player, level, taken);
            }

            // Auto-remove corpse block when empty; always send updated contents back
            if (be.isEmpty()) {
                level.removeBlock(msg.pos, false);
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new S2CCorpseContentsPacket(msg.pos, "", List.of()));
            } else {
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new S2CCorpseContentsPacket(msg.pos, be.getOwnerName(), be.getItems()));
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static void giveOrDrop(ServerPlayer player, ServerLevel level, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            ItemEntity ie = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
            level.addFreshEntity(ie);
        }
    }
}
