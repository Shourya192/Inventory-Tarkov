package com.tarkovinventory.container;

import com.tarkovinventory.capability.IPlayerEquipment;
import com.tarkovinventory.capability.ModCapabilities;
import com.tarkovinventory.inventory.GridInventory;
import com.tarkovinventory.inventory.GridItemSizes;
import com.tarkovinventory.inventory.GridSize;
import com.tarkovinventory.registry.ModMenuTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Menu for the full Tarkov inventory screen.
 *
 * Slot layout:
 *   [0 .. GRID_SLOTS-1]      backpack grid (max 12×12 = 144 slots; active region is per-backpack)
 *   [GRID_SLOTS .. +26]      player main inventory (3×9 = 27)
 *   [GRID_SLOTS+27 .. +35]   hotbar (9 slots)
 *
 * Pocket slots 0-6 map directly to hotbar indices 2-8.
 * Primary weapon is hotbar index 0, secondary is hotbar index 1.
 * These are NOT separate menu slots — they live inside the hotbar range above.
 */
public class TarkovInventoryMenu extends AbstractContainerMenu {

    public static final int GRID_SLOTS    = GridInventory.TOTAL_CELLS; // 64
    public static final int PLAYER_START  = GRID_SLOTS;                // 64
    public static final int HOTBAR_START  = PLAYER_START + 27;         // 91

    /** Number of pocket slots (backed by hotbar 2-8). */
    public static final int POCKETS_COUNT = 7;

    private final GridInventory  gridInventory;
    private final IPlayerEquipment cap;
    private final Inventory        playerInventory;

    public TarkovInventoryMenu(int windowId, Inventory playerInv, int ignoredHand) {
        super(ModMenuTypes.TARKOV_INVENTORY.get(), windowId);
        this.playerInventory = playerInv;

        this.cap = ModCapabilities.get(playerInv.player)
                .orElseThrow(() -> new IllegalStateException("Player missing Tarkov capability"));
        this.gridInventory = cap.getGridInventory();

        // Grid slots (virtual — positions set by screen)
        for (int i = 0; i < GRID_SLOTS; i++) {
            addSlot(new Slot(gridInventory, i, -1000, -1000) {
                @Override public boolean mayPlace(@NotNull ItemStack s) { return true; }
            });
        }

        // Player main inventory (rows 0-2, indices 9-35)
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInv, col + row * 9 + 9, 0, 0));

        // Hotbar (indices 0-8) — pockets 0-6 live at indices 2-8 here
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInv, col, 0, 0));
    }

    // ---------------------------------------------------------------
    // Public accessors — pockets are hotbar 2-8
    // ---------------------------------------------------------------

    public GridInventory getGridInventory() { return gridInventory; }

    /** Pocket slot i corresponds to hotbar slot i+2 (hotbar keys 3–9). */
    public ItemStack getPocketSlot(int i) {
        if (i < 0 || i >= POCKETS_COUNT) return ItemStack.EMPTY;
        return playerInventory.getItem(i + 2);
    }

    public void setPocketSlot(int i, ItemStack s) {
        if (i < 0 || i >= POCKETS_COUNT) return;
        playerInventory.setItem(i + 2, s == null ? ItemStack.EMPTY : s);
    }

    /** Place stack into the backpack grid. Returns leftover. */
    public ItemStack placeInGrid(ItemStack stack, int col, int row, GridSize size) {
        if (gridInventory.canPlace(col, row, size)) {
            ItemStack single = stack.copyWithCount(1);
            gridInventory.placeItem(single, col, row, size);
            stack.shrink(1);
        }
        return stack;
    }

    /** Remove item from grid anchor slot. */
    public ItemStack pickFromGrid(int slotIdx) {
        return gridInventory.removeItem(slotIdx);
    }

    /** Auto-place into grid. Returns true if placed. */
    public boolean autoPlace(ItemStack stack) {
        ItemStack single = stack.copyWithCount(1);
        int idx = gridInventory.autoPlace(single);
        if (idx >= 0) { stack.shrink(1); return true; }
        return false;
    }

    // ---------------------------------------------------------------
    // AbstractContainerMenu
    // ---------------------------------------------------------------

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return result;

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < GRID_SLOTS) {
            if (!moveItemStackTo(stack, PLAYER_START, HOTBAR_START + 9, false)) return ItemStack.EMPTY;
            gridInventory.removeItem(index);
        } else {
            ItemStack single = stack.copyWithCount(1);
            int placed = gridInventory.autoPlace(single);
            if (placed < 0) return ItemStack.EMPTY;
            stack.shrink(1);
            slot.setChanged();
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) { return true; }
}
