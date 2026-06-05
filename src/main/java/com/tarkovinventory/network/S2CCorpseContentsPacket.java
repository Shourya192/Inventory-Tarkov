package com.tarkovinventory.network;

import com.tarkovinventory.client.CorpseClientCache;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server → Client: delivers the current contents of a nearby corpse block.
 *
 * <p>Sent when the Tarkov screen opens and after each item-take operation.
 * An empty {@code items} list signals that the corpse has been fully looted
 * and should be removed from the client cache.
 */
public class S2CCorpseContentsPacket {

    private final BlockPos      pos;
    private final String        ownerName;
    private final List<ItemStack> items;

    public S2CCorpseContentsPacket(BlockPos pos, String ownerName, List<ItemStack> items) {
        this.pos       = pos;
        this.ownerName = ownerName;
        this.items     = items;
    }

    public static void encode(S2CCorpseContentsPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.ownerName, 64);
        buf.writeVarInt(msg.items.size());
        for (ItemStack s : msg.items) buf.writeItem(s);
    }

    public static S2CCorpseContentsPacket decode(FriendlyByteBuf buf) {
        BlockPos pos       = buf.readBlockPos();
        String ownerName   = buf.readUtf(64);
        int count          = buf.readVarInt();
        List<ItemStack> items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) items.add(buf.readItem());
        return new S2CCorpseContentsPacket(pos, ownerName, items);
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(S2CCorpseContentsPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.items.isEmpty()) {
                CorpseClientCache.remove(msg.pos);
            } else {
                CorpseClientCache.put(msg.pos, msg.ownerName, msg.items);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
