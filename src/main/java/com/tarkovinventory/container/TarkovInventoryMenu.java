package com.tarkovinventory.container;

import com.tarkovinventory.inventory.GridInventory;
import com.tarkovinventory.inventory.GridItemSizes;
import com.tarkovinventory.inventory.GridSize;
import com.tarkovinventory.item.TarkovBackpackItem;
import com.tarkovinventory.registry.ModMenuTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
 *   [0 .. GRID_SLOTS-1]          grid inventory  (10×12 = 120)
 *   [GRID_SLOTS .. +3]           pockets         (4 fixed slots)
 *   [GRID_SLOTS+4 .. +6]         pouch           (3 fixed slots)
 *   [GRID_SLOTS+7 .. +33]        player main inv (3×9 = 27)
 *   [GRID_SLOTS+34 .. +42]       hotbar          (9)
 *
 * Equipment slots (helmet/chest/legs/boots/capability slots) are accessed
 * directly from player.getArmorSlot() and capability — NOT as container slots.
 */
public class TarkovInventoryMenu extends AbstractContainerMenu {

    public static final int GRID_SLOTS    = GridInventory.TOTAL_CELLS; // 120
    public static final int POCKETS_START = GRID_SLOTS;                // 120
    public static final int POCKETS_COUNT = 4;
    public static final int POUCH_START   = POCKETS_START + POCKETS_COUNT; // 124
    public static final int POUCH_COUNT   = 3;
    public static final int PLAYER_START  = POUCH_START + POUCH_COUNT;     // 127
    public static final int HOTBAR_START  = PLAYER_START + 27;             // 154

    // Tags used to persist pockets/pouch in the backpack item NBT
    private static final String TAG_POCKETS = "Pockets";
    private static final String TAG_POUCH   = "Pouch";

    private final GridInventory gridInventory;
    private final ItemStack[]   pocketSlots = new ItemStack[POCKETS_COUNT];
    private final ItemStack[]   pouchSlots  = new ItemStack[POUCH_COUNT];
    private final Inventory     playerInventory;
    private final int           hand;

    public TarkovInventoryMenu(int windowId, Inventory playerInv, int hand) {
        super(ModMenuTypes.TARKOV_INVENTORY.get(), windowId);
        this.playerInventory = playerInv;
        this.hand = hand;

        for (int i = 0; i < POCKETS_COUNT; i++) pocketSlots[i] = ItemStack.EMPTY;
        for (int i = 0; i < POUCH_COUNT;   i++) pouchSlots[i]  = ItemStack.EMPTY;

        ItemStack backpack = getBackpackStack();
        this.gridInventory = TarkovBackpackItem.loadInventory(backpack);
        loadSmallContainers(backpack);

        // Grid slots (virtual — positions set by screen)
        for (int i = 0; i < GRID_SLOTS; i++) {
            int finalI = i;
            addSlot(new Slot(gridInventory, i, -1000, -1000) {
                @Override public boolean mayPlace(@NotNull ItemStack s) { return true; }
            });
        }

        // Pocket slots (virtual placeholder SimpleContainers)
        net.minecraft.world.SimpleContainer pocketsContainer =
                new net.minecraft.world.SimpleContainer(POCKETS_COUNT);
        for (int i = 0; i < POCKETS_COUNT; i++) {
            pocketsContainer.setItem(i, pocketSlots[i]);
            int idx = i;
            addSlot(new Slot(pocketsContainer, i, -1000, -1000) {
                @Override public boolean mayPlace(@NotNull ItemStack s) { return true; }
                @Override public void set(@NotNull ItemStack s) {
                    super.set(s);
                    pocketSlots[idx] = s;
                    saveBack();
                }
            });
        }

        // Pouch slots
        net.minecraft.world.SimpleContainer pouchContainer =
                new net.minecraft.world.SimpleContainer(POUCH_COUNT);
        for (int i = 0; i < POUCH_COUNT; i++) {
            pouchContainer.setItem(i, pouchSlots[i]);
            int idx = i;
            addSlot(new Slot(pouchContainer, i, -1000, -1000) {
                @Override public boolean mayPlace(@NotNull ItemStack s) { return true; }
                @Override public void set(@NotNull ItemStack s) {
                    super.set(s);
                    pouchSlots[idx] = s;
                    saveBack();
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
    // Public accessors
    // ---------------------------------------------------------------

    public GridInventory getGridInventory() { return gridInventory; }

    public ItemStack getPocketSlot(int i)  { return i < POCKETS_COUNT ? pocketSlots[i] : ItemStack.EMPTY; }
    public ItemStack getPouchSlot(int i)   { return i < POUCH_COUNT   ? pouchSlots[i]  : ItemStack.EMPTY; }

    public void setPocketSlot(int i, ItemStack s) {
        if (i >= 0 && i < POCKETS_COUNT) { pocketSlots[i] = s; saveBack(); }
    }

    public void setPouchSlot(int i, ItemStack s) {
        if (i >= 0 && i < POUCH_COUNT) { pouchSlots[i] = s; saveBack(); }
    }

    /** Place stack into the backpack grid. Returns leftover. */
    public ItemStack placeInGrid(ItemStack stack, int col, int row, GridSize size) {
        if (gridInventory.canPlace(col, row, size)) {
            ItemStack single = stack.copyWithCount(1);
            gridInventory.placeItem(single, col, row, size);
            stack.shrink(1);
            saveBack();
        }
        return stack;
    }

    /** Remove item from grid anchor slot. */
    public ItemStack pickFromGrid(int slotIdx) {
        ItemStack taken = gridInventory.removeItem(slotIdx);
        saveBack();
        return taken;
    }

    /** Auto-place into grid. Returns true if placed. */
    public boolean autoPlace(ItemStack stack) {
        ItemStack single = stack.copyWithCount(1);
        int idx = gridInventory.autoPlace(single);
        if (idx >= 0) { stack.shrink(1); saveBack(); return true; }
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
            // Grid → player inv
            if (!moveItemStackTo(stack, PLAYER_START, HOTBAR_START + 9, false)) return ItemStack.EMPTY;
            gridInventory.removeItem(index);
            saveBack();
        } else if (index < PLAYER_START) {
            // Pockets or pouch → player inv
            if (!moveItemStackTo(stack, PLAYER_START, HOTBAR_START + 9, false)) return ItemStack.EMPTY;
            slot.set(ItemStack.EMPTY);
        } else {
            // Player → grid auto-place
            ItemStack single = stack.copyWithCount(1);
            int placed = gridInventory.autoPlace(single);
            if (placed < 0) return ItemStack.EMPTY;
            stack.shrink(1);
            slot.setChanged();
            saveBack();
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) { return true; }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        saveBack();
    }

    // ---------------------------------------------------------------
    // Persistence
    // ---------------------------------------------------------------

    private void saveBack() {
        ItemStack backpack = getBackpackStack();
        if (backpack.isEmpty()) return;
        TarkovBackpackItem.saveInventory(backpack, gridInventory);
        saveSmallContainers(backpack);
    }

    private void loadSmallContainers(ItemStack backpack) {
        CompoundTag tag = backpack.getOrCreateTag();

        ListTag pList = tag.getList(TAG_POCKETS, Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(pList.size(), POCKETS_COUNT); i++) {
            pocketSlots[i] = ItemStack.of(pList.getCompound(i));
        }

        ListTag ouList = tag.getList(TAG_POUCH, Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(ouList.size(), POUCH_COUNT); i++) {
            pouchSlots[i] = ItemStack.of(ouList.getCompound(i));
        }
    }

    private void saveSmallContainers(ItemStack backpack) {
        CompoundTag tag = backpack.getOrCreateTag();

        ListTag pList = new ListTag();
        for (ItemStack s : pocketSlots) pList.add(s.save(new CompoundTag()));
        tag.put(TAG_POCKETS, pList);

        ListTag ouList = new ListTag();
        for (ItemStack s : pouchSlots) ouList.add(s.save(new CompoundTag()));
        tag.put(TAG_POUCH, ouList);
    }

    private ItemStack getBackpackStack() {
        ItemStack stack = playerInventory.player.getItemInHand(
                hand == 0 ? net.minecraft.world.InteractionHand.MAIN_HAND
                          : net.minecraft.world.InteractionHand.OFF_HAND);
        return (stack.getItem() instanceof TarkovBackpackItem) ? stack : ItemStack.EMPTY;
    }
}
