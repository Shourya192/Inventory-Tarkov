package com.tarkovinventory.inventory;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry mapping backpack item IDs to their grid dimensions (cols × rows).
 *
 * Unknown backpacks default to 6×6 so they still work without being registered.
 * Add entries here whenever you want a specific backpack to have a precise size.
 */
public final class BackpackSizes {

    private BackpackSizes() {}

    /** Default size for any backpack not in the registry. */
    public static final int DEFAULT_COLS = 6;
    public static final int DEFAULT_ROWS = 6;

    /** id → [cols, rows] */
    private static final Map<String, int[]> REGISTRY = new HashMap<>();

    static {
        // ── Our own backpack ──────────────────────────────────────────
        register("tarkovinventory:tactical_backpack", 8, 8);

        // ── Sophisticated Backpacks ───────────────────────────────────
        register("sophisticatedbackpacks:backpack",          4, 9);
        register("sophisticatedbackpacks:iron_backpack",     5, 9);
        register("sophisticatedbackpacks:gold_backpack",     6, 9);
        register("sophisticatedbackpacks:diamond_backpack",  7, 9);
        register("sophisticatedbackpacks:netherite_backpack",8, 9);

        // ── Traveler's Backpack ───────────────────────────────────────
        register("travelersbackpack:standard_backpack",      6, 4);
        register("travelersbackpack:leather_backpack",       5, 4);
        register("travelersbackpack:wither_backpack",        9, 6);

        // ── Iron Backpacks ────────────────────────────────────────────
        register("ironbackpacks:basic_backpack",   3, 5);
        register("ironbackpacks:iron_backpack",    4, 6);
        register("ironbackpacks:gold_backpack",    5, 7);
        register("ironbackpacks:diamond_backpack", 6, 8);
        register("ironbackpacks:crystal_backpack", 7, 9);

        // ── Quark ─────────────────────────────────────────────────────
        register("quark:backpack", 4, 4);
    }

    /**
     * Register a custom backpack size. Call this from your mod's setup if you
     * want to add new entries at runtime without editing this file.
     */
    public static void register(String itemId, int cols, int rows) {
        cols = Math.min(cols, GridInventory.MAX_COLS);
        rows = Math.min(rows, GridInventory.MAX_ROWS);
        REGISTRY.put(itemId, new int[]{cols, rows});
    }

    /** Returns the grid cols for the given backpack, or DEFAULT_COLS. */
    public static int getCols(ItemStack stack) {
        return getSize(stack)[0];
    }

    /** Returns the grid rows for the given backpack, or DEFAULT_ROWS. */
    public static int getRows(ItemStack stack) {
        return getSize(stack)[1];
    }

    private static int[] getSize(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return new int[]{DEFAULT_COLS, DEFAULT_ROWS};
        var key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (key == null) return new int[]{DEFAULT_COLS, DEFAULT_ROWS};
        return REGISTRY.getOrDefault(key.toString(), new int[]{DEFAULT_COLS, DEFAULT_ROWS});
    }
}
