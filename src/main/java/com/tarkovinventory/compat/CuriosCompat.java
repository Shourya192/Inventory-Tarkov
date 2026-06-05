package com.tarkovinventory.compat;

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
 */
public final class CuriosCompat {

    private CuriosCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded("curios");
    }

    /**
     * Returns all stacks currently equipped in any Curios slot.
     * Returns an empty list if Curios is absent or an error occurs.
     */
    public static List<CuriosSlotEntry> getEquippedSlots(Player player) {
        List<CuriosSlotEntry> result = new ArrayList<>();
        if (!isLoaded()) return result;
        try {
            Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method helperMethod = apiClass.getMethod("getCuriosHelper");
            Object helper = helperMethod.invoke(null);

            Method handlerMethod = helper.getClass().getMethod("getCuriosHandler", Player.class);
            Optional<?> optHandler = (Optional<?>) handlerMethod.invoke(helper, player);
            if (!optHandler.isPresent()) return result;

            Object handler = optHandler.get();
            Method getCurios = handler.getClass().getMethod("getCurios");
            Map<?, ?> curios = (Map<?, ?>) getCurios.invoke(handler);

            for (Map.Entry<?, ?> entry : curios.entrySet()) {
                String slotId = entry.getKey().toString();
                Object stacksHandler = entry.getValue();
                Method getStacks = stacksHandler.getClass().getMethod("getStacks");
                Object stacks = getStacks.invoke(stacksHandler);
                Method getSlots = stacks.getClass().getMethod("getSlots");
                int slots = (int) getSlots.invoke(stacks);
                Method getStack = stacks.getClass().getMethod("getStackInSlot", int.class);
                for (int i = 0; i < slots; i++) {
                    ItemStack stack = (ItemStack) getStack.invoke(stacks, i);
                    result.add(new CuriosSlotEntry(slotId, i, stack));
                }
            }
        } catch (Throwable ignored) {
            // Curios not present or version mismatch — degrade gracefully
        }
        return result;
    }

    /**
     * Set an item in a specific Curios slot. No-op if Curios is absent.
     */
    public static void setSlot(Player player, String slotId, int index, ItemStack stack) {
        if (!isLoaded()) return;
        try {
            Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method helperMethod = apiClass.getMethod("getCuriosHelper");
            Object helper = helperMethod.invoke(null);

            Method handlerMethod = helper.getClass().getMethod("getCuriosHandler", Player.class);
            Optional<?> optHandler = (Optional<?>) handlerMethod.invoke(helper, player);
            if (!optHandler.isPresent()) return;

            Object handler = optHandler.get();
            Method getCurios = handler.getClass().getMethod("getCurios");
            Map<?, ?> curios = (Map<?, ?>) getCurios.invoke(handler);
            Object slotHandler = curios.get(slotId);
            if (slotHandler == null) return;

            Method getStacks = slotHandler.getClass().getMethod("getStacks");
            Object stacks = getStacks.invoke(slotHandler);
            Method setStack = stacks.getClass().getMethod("setStackInSlot", int.class, ItemStack.class);
            setStack.invoke(stacks, index, stack);
        } catch (Throwable ignored) {}
    }

    /** Returns a human-readable label for a Curios slot id. */
    public static String labelFor(String slotId) {
        return switch (slotId) {
            case "head"     -> "HEADWEAR";
            case "face"     -> "FACE COVER";
            case "necklace" -> "NECKLACE";
            case "ring"     -> "RING";
            case "hands"    -> "GLOVES";
            case "back"     -> "ON BACK";
            case "belt"     -> "BELT";
            case "charm"    -> "CHARM";
            case "body"     -> "BODY ARMOR";
            default         -> slotId.toUpperCase();
        };
    }

    public record CuriosSlotEntry(String slotId, int index, ItemStack stack) {}
}
