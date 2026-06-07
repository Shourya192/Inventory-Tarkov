package com.tarkovinventory.compat;

import com.tarkovinventory.inventory.RigInventory;
import com.tarkovinventory.inventory.RigSizes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * SAFE CORE RULES:
 * - Never mutate inventories inside handlers
 * - Never auto-save inside capability wrappers
 * - Always use RigTransaction for modifications
 */
public final class BackpackCompat {

    private BackpackCompat() {}

    // ─────────────────────────────────────────────────────────────
    // 🔥 SINGLE SOURCE OF TRUTH: TRANSACTION SYSTEM
    // ─────────────────────────────────────────────────────────────

    /**
     * Opens a safe, isolated snapshot of a rig inventory.
     * MUST be committed manually using commit().
     */
    public static RigTransaction openRig(ItemStack rig) {
        return new RigTransaction(rig);
    }

    // ─────────────────────────────────────────────────────────────
    // 🔥 RIG TRANSACTION (CORE ANTI-DUPE SYSTEM)
    // ─────────────────────────────────────────────────────────────

    public static final class RigTransaction {

        public final ItemStack rig;
        public final CompoundTag tag;
        public final RigInventory inv;

        private final int cols;
        private final int rows;

        public RigTransaction(ItemStack rig) {
            this.rig = rig;
            this.tag = rig.getOrCreateTag();

            this.cols = RigSizes.getCols(rig);
            this.rows = RigSizes.getRows(rig);

            this.inv = new RigInventory(cols, rows);

            // Load saved data if present
            if (tag.contains("TarkovRigInventory")) {
                this.inv.deserializeNBT(tag.getCompound("TarkovRigInventory"));
            }
        }

        /**
         * Commit changes back to item NBT.
         * ONLY CALL ONCE per packet/action.
         */
        public void commit() {
            tag.put("TarkovRigInventory", inv.serializeNBT());
        }

        /**
         * Slot validation helper
         */
        public boolean isValidSlot(int slot) {
            return slot >= 0 && slot < inv.getSlots();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ❌ LEGACY METHOD (DEPRECATED - DO NOT USE FOR LOGIC)
    // ─────────────────────────────────────────────────────────────

    /**
     * ⚠️ ONLY FOR UI RENDERING / READ-ONLY DISPLAY
     * DO NOT USE FOR GAME LOGIC OR EXTRACTION
     */
    @Deprecated
    public static RigInventory getLegacyRigInventory(ItemStack rig) {
        int cols = RigSizes.getCols(rig);
        int rows = RigSizes.getRows(rig);

        CompoundTag tag = rig.getTag();
        RigInventory inv = new RigInventory(cols, rows);

        if (tag != null && tag.contains("TarkovRigInventory")) {
            inv.deserializeNBT(tag.getCompound("TarkovRigInventory"));
        }

        return inv;
    }

    // ─────────────────────────────────────────────────────────────
    // 🔥 SAFE SYNC HELPERS (OPTIONAL USE)
    // ─────────────────────────────────────────────────────────────

    /**
     * Writes inventory back safely WITHOUT modifying logic flow.
     * (Used only when you already own a transaction)
     */
    public static void commitRig(RigTransaction tx) {
        tx.commit();
    }
}        return new ReadOnlyArrayItemHandler(items);
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
    if (simulate) {
        ItemStack stack = rigInv.getItem(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;

        ItemStack copy = stack.copy();
        copy.setCount(Math.min(amount, copy.getCount()));
        return copy;
    }

    ItemStack extracted = rigInv.extractItem(slot, amount);

    if (!extracted.isEmpty()) {
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
