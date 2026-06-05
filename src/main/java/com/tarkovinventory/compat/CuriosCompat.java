package com.tarkovinventory.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

/**
 * Soft compatibility with Curios API (top.theillusivec4.curios).
 *
 * All access is guarded by {@link #isLoaded()}.  Import nothing from the
 * curios package at the top level so this class loads cleanly when Curios
 * is absent.
 */
public final class CuriosCompat {

    private CuriosCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded("curios");
    }

    /**
     * Returns a flat list of all stacks currently equipped in any Curios slot.
     * Returns an empty list if Curios is not installed.
     */
    public static List<CuriosSlotEntry> getEquippedSlots(Player player) {
        List<CuriosSlotEntry> result = new ArrayList<>();
        if (!isLoaded()) return result;
        try {
            var api = top.theillusivec4.curios.api.CuriosApi.getCuriosHelper();
            var optional = api.getCuriosHandler(player);
            optional.ifPresent(handler -> {
                handler.getCurios().forEach((id, stacksHandler) -> {
                    var stacks = stacksHandler.getStacks();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        ItemStack stack = stacks.getStackInSlot(i);
                        result.add(new CuriosSlotEntry(id, i, stack));
                    }
                });
            });
        } catch (Throwable ignored) {
            // Curios version mismatch or other issue — degrade gracefully
        }
        return result;
    }

    /**
     * Set an item in a specific Curios slot.
     * No-op if Curios is not installed.
     */
    public static void setSlot(Player player, String slotId, int index, ItemStack stack) {
        if (!isLoaded()) return;
        try {
            var api = top.theillusivec4.curios.api.CuriosApi.getCuriosHelper();
            api.getCuriosHandler(player).ifPresent(handler -> {
                var slotHandler = handler.getCurios().get(slotId);
                if (slotHandler != null) {
                    slotHandler.getStacks().setStackInSlot(index, stack);
                }
            });
        } catch (Throwable ignored) {}
    }

    /** Returns a human-readable label for a Curios slot id. */
    public static String labelFor(String slotId) {
        return switch (slotId) {
            case "head"      -> "HEADWEAR";
            case "face"      -> "FACE COVER";
            case "necklace"  -> "NECKLACE";
            case "ring"      -> "RING";
            case "hands"     -> "GLOVES";
            case "back"      -> "ON BACK";
            case "belt"      -> "BELT";
            case "charm"     -> "CHARM";
            case "body"      -> "BODY ARMOR";
            default          -> slotId.toUpperCase();
        };
    }

    public record CuriosSlotEntry(String slotId, int index, ItemStack stack) {}
}
