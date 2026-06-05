package com.tarkovinventory.compat;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Soft compatibility with popular backpack mods:
 *  - Sophisticated Backpacks (sophisticatedbackpacks)
 *  - Traveler's Backpack (travelersbackpack)
 *  - Iron Backpacks (ironbackpacks)
 *
 * This class detects whether a given ItemStack is a backpack from another mod
 * and provides helpers to query or open its contents.
 */
public final class BackpackCompat {

    private BackpackCompat() {}

    public enum BackpackMod {
        SOPHISTICATED_BACKPACKS("sophisticatedbackpacks"),
        TRAVELERS_BACKPACK("travelersbackpack"),
        IRON_BACKPACKS("ironbackpacks"),
        NONE("");

        public final String modId;
        BackpackMod(String id) { this.modId = id; }

        public boolean isLoaded() {
            return !modId.isEmpty() && ModList.get().isLoaded(modId);
        }
    }

    /**
     * Returns which backpack mod the given item belongs to, or NONE.
     */
    public static BackpackMod detectMod(ItemStack stack) {
        if (stack.isEmpty()) return BackpackMod.NONE;
        String ns = getNamespace(stack.getItem());

        for (BackpackMod mod : BackpackMod.values()) {
            if (mod == BackpackMod.NONE) continue;
            if (ns.equals(mod.modId) && mod.isLoaded()) return mod;
        }
        return BackpackMod.NONE;
    }

    /**
     * Returns true if the item is a known backpack from another mod.
     * Used by the screen to decide whether to show a "Open in native UI" hint.
     */
    public static boolean isExternalBackpack(ItemStack stack) {
        return detectMod(stack) != BackpackMod.NONE;
    }

    /**
     * Returns a user-facing label for the external backpack type, or null.
     */
    public static String getExternalLabel(ItemStack stack) {
        BackpackMod mod = detectMod(stack);
        return switch (mod) {
            case SOPHISTICATED_BACKPACKS -> "Sophisticated Backpack";
            case TRAVELERS_BACKPACK      -> "Traveler's Backpack";
            case IRON_BACKPACKS          -> "Iron Backpack";
            default -> null;
        };
    }

    /** Whether ANY backpack mod is detected at runtime. */
    public static boolean anyLoaded() {
        for (BackpackMod mod : BackpackMod.values()) {
            if (mod.isLoaded()) return true;
        }
        return false;
    }

    private static String getNamespace(Item item) {
        var key = ForgeRegistries.ITEMS.getKey(item);
        return key != null ? key.getNamespace() : "";
    }
}
