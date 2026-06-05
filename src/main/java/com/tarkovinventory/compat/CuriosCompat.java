package com.tarkovinventory.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Soft compatibility with Curios API.
 * Uses pure reflection — no Curios classes are imported or referenced at
 * compile time, so the mod compiles and runs without Curios on the classpath.
 *
 * Supports three generations of Curios API for Forge 1.20.1:
 *   A) CuriosApi.getCuriosInventory(LivingEntity)       — Curios 5.3+ (newest)
 *   B) getCuriosHelper().getCuriosHandler(LivingEntity) — Curios 5.1–5.2
 *   C) getCuriosHelper().getCuriosHandler(Player)       — Curios 4.x / early 5
 *
 * All code that needs Curios should go through this class.
 */
public final class CuriosCompat {

    private CuriosCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded("curios");
    }

    // ── Core: get the ICuriosItemHandler for a player ─────────────────

    /**
     * Returns the raw Curios item-handler object for the player,
     * or null if Curios is absent or the API call fails.
     *
     * Tries all known API patterns so it works across Curios versions.
     */
    public static Object getHandler(Player player) {
        if (!isLoaded()) return null;
        try {
            Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");

            // ── Pattern A: CuriosApi.getCuriosInventory(LivingEntity) ─────
            try {
                Method m = api.getMethod("getCuriosInventory", LivingEntity.class);
                Object lazyOpt = m.invoke(null, player);
                // LazyOptional<T>.resolve() → Optional<T>
                Method resolve = lazyOpt.getClass().getMethod("resolve");
                Optional<?> opt = (Optional<?>) resolve.invoke(lazyOpt);
                if (opt.isPresent()) return opt.get();
            } catch (NoSuchMethodException ignored) {}

            // ── Pattern B: getCuriosHelper().getCuriosHandler(LivingEntity)
            try {
                Object helper = api.getMethod("getCuriosHelper").invoke(null);
                Method hm = helper.getClass().getMethod("getCuriosHandler", LivingEntity.class);
                Optional<?> opt = (Optional<?>) hm.invoke(helper, player);
                if (opt.isPresent()) return opt.get();
            } catch (NoSuchMethodException ignored) {}

            // ── Pattern C: getCuriosHelper().getCuriosHandler(Player) ─────
            try {
                Object helper = api.getMethod("getCuriosHelper").invoke(null);
                Method hm = helper.getClass().getMethod("getCuriosHandler", Player.class);
                Optional<?> opt = (Optional<?>) hm.invoke(helper, player);
                if (opt.isPresent()) return opt.get();
            } catch (NoSuchMethodException ignored) {}

        } catch (Throwable ignored) {}
        return null;
    }

    // ── Public helpers ────────────────────────────────────────────────

    /**
     * Returns all stacks currently equipped in any Curios slot.
     * Returns an empty list if Curios is absent or an error occurs.
     */
    public static List<CuriosSlotEntry> getEquippedSlots(Player player) {
        List<CuriosSlotEntry> result = new ArrayList<>();
        Object handler = getHandler(player);
        if (handler == null) return result;
        try {
            Map<?, ?> curios = (Map<?, ?>) handler.getClass().getMethod("getCurios").invoke(handler);
            for (Map.Entry<?, ?> entry : curios.entrySet()) {
                String slotId = entry.getKey().toString();
                Object stacksHandler = entry.getValue();
                Object stacks = stacksHandler.getClass().getMethod("getStacks").invoke(stacksHandler);
                int slots = (int) stacks.getClass().getMethod("getSlots").invoke(stacks);
                Method getStack = stacks.getClass().getMethod("getStackInSlot", int.class);
                for (int i = 0; i < slots; i++) {
                    ItemStack stack = (ItemStack) getStack.invoke(stacks, i);
                    result.add(new CuriosSlotEntry(slotId, i, stack));
                }
            }
        } catch (Throwable ignored) {}
        return result;
    }

    /**
     * Returns the first non-empty item in a specific Curios slot, or EMPTY.
     */
    public static ItemStack getSlotItem(Player player, String slotId, int index) {
        Object handler = getHandler(player);
        if (handler == null) return ItemStack.EMPTY;
        try {
            Map<?, ?> curios = (Map<?, ?>) handler.getClass().getMethod("getCurios").invoke(handler);
            Object stacksHandler = curios.get(slotId);
            if (stacksHandler == null) return ItemStack.EMPTY;
            Object stacks = stacksHandler.getClass().getMethod("getStacks").invoke(stacksHandler);
            return (ItemStack) stacks.getClass()
                .getMethod("getStackInSlot", int.class).invoke(stacks, index);
        } catch (Throwable ignored) {}
        return ItemStack.EMPTY;
    }

    /**
     * Set an item in a specific Curios slot. No-op if Curios is absent.
     */
    public static void setSlot(Player player, String slotId, int index, ItemStack stack) {
        Object handler = getHandler(player);
        if (handler == null) return;
        try {
            Map<?, ?> curios = (Map<?, ?>) handler.getClass().getMethod("getCurios").invoke(handler);
            Object slotHandler = curios.get(slotId);
            if (slotHandler == null) return;
            Object stacks = slotHandler.getClass().getMethod("getStacks").invoke(slotHandler);
            stacks.getClass().getMethod("setStackInSlot", int.class, ItemStack.class)
                .invoke(stacks, index, stack);
        } catch (Throwable ignored) {}
    }

    /** Returns a human-readable label for a Curios slot id. */
    public static String labelFor(String slotId) {
        return switch (slotId) {
            case "head"     -> "HEADWEAR";
            case "back"     -> "ON BACK";
            case "body"     -> "BODY ARMOR";
            case "earwear"  -> "EARWEAR";
            case "facewear" -> "FACEWEAR";
            case "knees"    -> "KNEES";
            case "face"     -> "FACE COVER";
            case "necklace" -> "NECKLACE";
            case "ring"     -> "RING";
            case "hands"    -> "GLOVES";
            case "belt"     -> "BELT";
            case "charm"    -> "CHARM";
            default         -> slotId.toUpperCase();
        };
    }

    public record CuriosSlotEntry(String slotId, int index, ItemStack stack) {}
}
