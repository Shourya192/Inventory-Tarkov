package com.tarkovinventory.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side cache of nearby corpse block inventories.
 *
 * <p>Populated / cleared by {@link com.tarkovinventory.network.S2CCorpseContentsPacket}.
 * Read by {@link com.tarkovinventory.client.screen.TarkovInventoryScreen}.
 *
 * <p>Uses {@link ConcurrentHashMap} because the network thread writes and the
 * render thread reads without explicit synchronization.
 */
@OnlyIn(Dist.CLIENT)
public final class CorpseClientCache {

    private CorpseClientCache() {}

    /** Immutable snapshot stored per block position. */
    public record CorpseEntry(String ownerName, List<ItemStack> items) {}

    private static final Map<BlockPos, CorpseEntry> CORPSES = new ConcurrentHashMap<>();

    public static void put(BlockPos pos, String ownerName, List<ItemStack> items) {
        CORPSES.put(pos, new CorpseEntry(ownerName, List.copyOf(items)));
    }

    public static void remove(BlockPos pos) {
        CORPSES.remove(pos);
    }

    /** Returns an unmodifiable snapshot of all cached corpse entries. */
    public static Map<BlockPos, CorpseEntry> all() {
        return Map.copyOf(CORPSES);
    }

    public static void clear() {
        CORPSES.clear();
    }
}
