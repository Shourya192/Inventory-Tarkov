package com.tarkovinventory.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Arrays;

/**
 * Soft compatibility with popular backpack / rig mods:
 *  - Sophisticated Backpacks (sophisticatedbackpacks)
 *  - Traveler's Backpack (travelersbackpack)
 *  - Iron Backpacks (ironbackpacks)
 *  - Modern Mayhem (mm) — backpacks + curios rigs
 *  - Survivor's Arsenal (survivorsarsenal) — backpacks
 *
 * Provides:
 *  - {@link #detectMod(ItemStack)} — which mod owns this item
 *  - {@link #asItemHandler(ItemStack)} — IItemHandler view of the inventory,
 *    falling back to NBT reading when the item does not expose the capability
 *  - {@link #getRigInventoryHandler(ItemStack)} — Custom rig inventory (independent of mod)
 */
public final class BackpackCompat {

    private BackpackCompat() {}

    public enum BackpackMod {
        SOPHISTICATED_BACKPACKS("sophisticatedbackpacks"),
        TRAVELERS_BACKPACK("travelersbackpack"),
        IRON_BACKPACKS("ironbackpacks"),
        MODERN_MAYHEM("mm"),
        SURVIVORS_ARSENAL("survivorsarsenal"),
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
     */
    public static boolean isExternalBackpack(ItemStack stack) {
        return detectMod(stack) != BackpackMod.NONE;
    }

    /**
     * Returns a user-facing label for the external backpack type, or null.
     */
    @Nullable
    public static String getExternalLabel(ItemStack stack) {
        BackpackMod mod = detectMod(stack);
        return switch (mod) {
            case SOPHISTICATED_BACKPACKS -> "Sophisticated Backpack";
            case TRAVELERS_BACKPACK      -> "Traveler's Backpack";
            case IRON_BACKPACKS          -> "Iron Backpack";
            case MODERN_MAYHEM          -> "Modern Mayhem";
            case SURVIVORS_ARSENAL      -> "Survivor's Arsenal";
            default -> null;
        };
    }

    /** Whether ANY backpack / rig mod is detected at runtime. */
    public static boolean anyLoaded() {
        for (BackpackMod mod : BackpackMod.values()) {
            if (mod.isLoaded()) return true;
        }
        return false;
    }

    // ── IItemHandler resolution ───────────────────────────────────────────

    /**
     * Returns an {@link IItemHandler} for the given item stack.
     *
     * Resolution order:
     * <ol>
     *   <li>IItemHandler capability (works for most mods)</li>
     *   <li>Modern Mayhem NBT fallback — stores {@code ItemStackHandler} data
     *       under the {@code "inventory"} compound in the item tag</li>
     *   <li>Survivor's Arsenal NBT fallback — stores a vanilla {@code SimpleContainer}
     *       list under {@code "Inventory"} directly in the item tag</li>
     * </ol>
     *
     * Returns {@code null} if no handler can be resolved.
     */
    @Nullable
    public static IItemHandler asItemHandler(ItemStack stack) {
        if (stack.isEmpty()) return null;

        // 1. Try the IItemHandler capability first
        var cap = stack.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (cap.isPresent()) return cap.orElse(null);

        String ns = getNamespace(stack.getItem());

        // 2. Modern Mayhem: item.tag["inventory"] → ItemStackHandler compound
        //    (GenericBackpackItem#InitInventory stores it this way).
        //    Checked BEFORE the tag-null guard: a freshly crafted rig has no item
        //    tag at all, so we must not exit early — we still want to return an
        //    empty handler sized from BackpackSizes so the rig section renders.
        if ("mm".equals(ns)) {
            int slots = com.tarkovinventory.inventory.BackpackSizes.getCols(stack)
                      * com.tarkovinventory.inventory.BackpackSizes.getRows(stack);
            if (slots <= 0) slots = 9; // fallback for unregistered MM items
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("inventory", 10)) {
                try {
                    ItemStackHandler h = new ItemStackHandler(slots);
                    h.deserializeNBT(tag.getCompound("inventory"));
                    return h;
                } catch (Exception ignored) {}
            }
            // No NBT yet (fresh rig) — correctly-sized empty handler
            return new ItemStackHandler(slots);
        }

        // 3. Survivor's Arsenal: item.tag["Inventory"] → vanilla ListTag
        //    (BackpackMenu#loadFromNBT reads getList("Inventory", 10))
        CompoundTag tag = stack.getTag();
        if (tag == null) return null;
        if ("survivorsarsenal".equals(ns) && tag.contains("Inventory", 9)) {
            int slots = getSaSlotCount(stack);
            return buildSaHandler(tag.getList("Inventory", 10), slots);
        }

        return null;
    }

    /**
     * Returns an IItemHandler for a rig that uses custom Tarkov rig inventory storage.
     * This is independent of the mod's original inventory system.
     */
    @Nullable
    public static IItemHandler getRigInventoryHandler(ItemStack rigItem) {
        if (rigItem.isEmpty()) return null;

        // Get rig dimensions from RigSizes registry
        int cols = com.tarkovinventory.inventory.RigSizes.getCols(rigItem);
        int rows = com.tarkovinventory.inventory.RigSizes.getRows(rigItem);

        // Get or create the custom RigInventory from the rig's NBT
        CompoundTag tag = rigItem.getOrCreateTag();
        com.tarkovinventory.inventory.RigInventory rigInv;

        if (tag.contains("TarkovRigInventory")) {
            rigInv = com.tarkovinventory.inventory.RigInventory.unwrapFromNBT(tag);
        } else {
            rigInv = new com.tarkovinventory.inventory.RigInventory(cols, rows);
        }

        // If size changed, update it
        if (rigInv.getCols() != cols || rigInv.getRows() != rows) {
            rigInv.setSize(cols, rows);
        }

        // Wrap RigInventory as IItemHandler
        return new RigInventoryItemHandler(rigInv, tag);
    }

    // ── Write operations (server-side) ───────────────────────────────────

    /**
     * Extracts one full stack from {@code slotIndex} inside a rig/backpack item
     * and writes the change back to the item's NBT when needed.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>IItemHandler capability — write-through automatically handled by Forge</li>
     *   <li>Modern Mayhem NBT path — reads {@code ItemStackHandler}, extracts,
     *       then serializes the modified handler back into {@code item.tag["inventory"]}</li>
     * </ol>
     *
     * Returns {@link ItemStack#EMPTY} if nothing could be extracted.
     */
    public static ItemStack extractFromRig(ItemStack rig, int slotIndex) {
        if (rig.isEmpty() || slotIndex < 0) return ItemStack.EMPTY;

        // 1. IItemHandler capability (Forge-managed write-through)
        var cap = rig.getCapability(ForgeCapabilities.ITEM_HANDLER);
        if (cap.isPresent()) {
            return cap.map(h -> {
                if (slotIndex >= h.getSlots()) return ItemStack.EMPTY;
                return h.extractItem(slotIndex, h.getSlotLimit(slotIndex), false);
            }).orElse(ItemStack.EMPTY);
        }

        // 2. Modern Mayhem: deserialize, extract, serialize back
        if ("mm".equals(getNamespace(rig.getItem()))) {
            CompoundTag tag = rig.getOrCreateTag();
            if (tag.contains("inventory", 10)) {
                try {
                    ItemStackHandler h = new ItemStackHandler();
                    h.deserializeNBT(tag.getCompound("inventory"));
                    if (slotIndex < h.getSlots()) {
                        ItemStack taken = h.extractItem(slotIndex, h.getSlotLimit(slotIndex), false);
                        if (!taken.isEmpty()) {
                            tag.put("inventory", h.serializeNBT()); // write back
                            return taken;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        return ItemStack.EMPTY;
    }

    // ── Private helpers ───────────────────────────────────────────────────

    /**
     * Wraps a Survivor's Arsenal vanilla ListTag into a read-only IItemHandler.
     * Each list entry is a CompoundTag with a "Slot" byte + full ItemStack NBT.
     */
    private static IItemHandler buildSaHandler(ListTag list, int slots) {
        ItemStack[] items = new ItemStack[slots];
        Arrays.fill(items, ItemStack.EMPTY);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = Byte.toUnsignedInt(entry.getByte("Slot"));
            if (slot < slots) {
                items[slot] = ItemStack.of(entry);
            }
        }
        return new ReadOnlyArrayItemHandler(items);
    }

    /**
     * Returns the slot count for a known Survivor's Arsenal backpack.
     * Derived from SmallBackpackItem (18), HikingBackpackItem (36),
     * MilitaryBackpackItem (54) constructor arguments found via bytecode analysis.
     */
    private static int getSaSlotCount(ItemStack stack) {
        String id = idString(stack);
        if (id.startsWith("survivorsarsenal:military_backpack")) return 54;
        if (id.startsWith("survivorsarsenal:hiking_backpack"))   return 36;
        // small_backpack and leather_backpack — 18 and 36 respectively;
        // leather is assumed to match hiking since it shares the same class tier
        if (id.equals("survivorsarsenal:leather_backpack"))      return 36;
        return 18; // small backpacks
    }

    private static String idString(ItemStack stack) {
        var key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null ? key.toString() : "";
    }

    private static String getNamespace(Item item) {
        var key = ForgeRegistries.ITEMS.getKey(item);
        return key != null ? key.getNamespace() : "";
    }

    // ── Read-only IItemHandler wrapper ────────────────────────────────────

    /**
     * Minimal read-only IItemHandler backed by a fixed ItemStack array.
     * Write operations are no-ops (inserts return the input stack unchanged;
     * extracts return EMPTY).  Used for NBT-deserialized inventories that
     * do not live as a server-side capability.
     */
    private static final class ReadOnlyArrayItemHandler implements IItemHandler {

        private final ItemStack[] items;

        ReadOnlyArrayItemHandler(ItemStack[] items) {
            this.items = items;
        }

        @Override public int getSlots() { return items.length; }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return (slot >= 0 && slot < items.length) ? items[slot] : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack; // read-only
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY; // read-only
        }

        @Override
        public int getSlotLimit(int slot) { return 64; }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) { return false; }
    }

    // ── RigInventory IItemHandler wrapper ────────────────────────────────

    /**
     * Wraps a RigInventory as an IItemHandler with write-through to NBT.
     */
    private static final class RigInventoryItemHandler implements IItemHandler {
        private final com.tarkovinventory.inventory.RigInventory rigInv;
        private final CompoundTag parentTag;

        RigInventoryItemHandler(com.tarkovinventory.inventory.RigInventory rigInv, CompoundTag parentTag) {
            this.rigInv = rigInv;
            this.parentTag = parentTag;
        }

        @Override
        public int getSlots() {
            return rigInv.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return rigInv.getItem(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            ItemStack copy = stack.copy();
            if (!simulate) {
                rigInv.insertItem(slot, copy);
                saveToNBT();
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack extracted = rigInv.extractItem(slot, amount);
            if (!extracted.isEmpty() && !simulate) {
                saveToNBT();
            }
            return extracted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return true;
        }

        private void saveToNBT() {
            parentTag.put("TarkovRigInventory", rigInv.serializeNBT());
        }
    }
}
