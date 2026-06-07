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
}
