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
 * Grid, pockets, and pouch are stored in the player capability — no
 * physical backpack item required. Data persists across sessions.
 *
 * Slot layout:
 *   [0 .. GRID_SLOTS-1]          grid inventory  (10×12 = 120)
 *   [GRID_SLOTS .. +3]           pockets         (4 fixed slots)
 *   [GRID_SLOTS+4 .. +6]         pouch           (3 fixed slots)
 *   [GRID_SLOTS+7 .. +33]        player main inv (3×9 = 27)
 *   [GRID_SLOTS+34 .. +42]       hotbar          (9)
 */
public class TarkovInventoryMenu extends AbstractContainerMenu {

    public static final int GRID_SLOTS    = GridInventory.TOTAL_CELLS; // 120
    public static final int POCKETS_START = GRID_SLOTS;                // 120
    public static final int POCKETS_COUNT = IPlayerEquipment.POCKETS_COUNT; // 4
    public static final int POUCH_START   = POCKETS_START + POCKETS_COUNT;  // 124
    public static final int POUCH_COUNT   = IPlayerEquipment.POUCH_COUNT;   // 3
    public static final int PLAYER_START  = POUCH_START + POUCH_COUNT;      // 127
    public static final int HOTBAR_START  = PLAYER_START + 27;              // 154

    private final GridInventory  gridInventory;
    private final IPlayerEquipment cap;
    private final Inventory        playerInventory;

    public TarkovInventoryMenu(int windowId, Inventory playerInv, int ignoredHand) {
        super(ModMenuTypes.TARKOV_INVENTORY.get(), windowId);
        this.playerInventory = playerInv;

        // Load everything from the player capability
        this.cap = ModCapabilities.get(playerInv.player)
                .orElseThrow(() -> new IllegalStateException("Player missing Tarkov capability"));
        this.gridInventory = cap.getGridInventory();

        // Grid slots (virtual — positions set by screen)
        for (int i = 0; i < GRID_SLOTS; i++) {
            addSlot(new Slot(gridInventory, i, -1000, -1000) {
                @Override public boolean mayPlace(@NotNull ItemStack s) { return true; }
            });
        }

        // Pocket slots (backed by capability)
        net.minecraft.world.SimpleContainer pocketsContainer =
                new net.minecraft.world.SimpleContainer(POCKETS_COUNT);
        for (int i = 0; i < POCKETS_COUNT; i++) {
            pocketsContainer.setItem(i, cap.getPocketSlot(i));
            int idx = i;
            addSlot(new Slot(pocketsContainer, i, -1000, -1000) {
                @Override public boolean mayPlace(@NotNull ItemStack s) { return true; }
                @Override public void set(@NotNull ItemStack s) {
                    super.set(s);
                    cap.setPocketSlot(idx, s);
                }
            });
        }

        // Pouch slots (backed by capability)
        net.minecraft.world.SimpleContainer pouchContainer =
                new net.minecraft.world.SimpleContainer(POUCH_COUNT);
        for (int i = 0; i < POUCH_COUNT; i++) {
            pouchContainer.setItem(i, cap.getPouchSlot(i));
            int idx = i;
            addSlot(new Slot(pouchContainer, i, -1000, -1000) {
                @Override public boolean mayPlace(@NotNull ItemStack s) { return true; }
                @Override public void set(@NotNull ItemStack s) {
                    super.set(s);
                    cap.setPouchSlot(idx, s);
                }
            });
        }

        // Player main inventory (rows 0-2)
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInv, col + row * 9 + 9, 0, 0));

        // Hotbar
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInv, col, 0, 0));
    }

    // ---------------------------------------------------------------
    // Public accessors (used by the screen)
    // ---------------------------------------------------------------

    public GridInventory getGridInventory() { return gridInventory; }

    public ItemStack getPocketSlot(int i)  { return cap.getPocketSlot(i); }
    public ItemStack getPouchSlot(int i)   { return cap.getPouchSlot(i); }

    public void setPocketSlot(int i, ItemStack s) { cap.setPocketSlot(i, s); }
    public void setPouchSlot(int i, ItemStack s)  { cap.setPouchSlot(i, s); }

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
        } else if (index < PLAYER_START) {
            if (!moveItemStackTo(stack, PLAYER_START, HOTBAR_START + 9, false)) return ItemStack.EMPTY;
            slot.set(ItemStack.EMPTY);
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
