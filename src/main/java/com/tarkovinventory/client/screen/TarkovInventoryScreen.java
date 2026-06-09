package com.tarkovinventory.client.screen;

import com.tarkovinventory.client.screen.modules.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * STANDALONE TARKOV UI SCREEN
 * - NO Forge Container
 * - NO Vanilla Inventory GUI
 * - Pure custom rendering system
 */
public class TarkovInventoryScreen extends Screen {

    // ── Modules ─────────────────────────────
    private final EquipmentPanelRenderer equipment = new EquipmentPanelRenderer();
    private final InventoryGridRenderer grid = new InventoryGridRenderer();
    private final LootPanelRenderer loot = new LootPanelRenderer();
    private final VicinityRenderer vicinity = new VicinityRenderer();
    private final DragState dragState = new DragState();

    protected TarkovInventoryScreen() {
        super(Component.literal("Tarkov Inventory"));
    }

    @Override
    protected void init() {
        super.init();

        equipment.init(leftPos(), topPos());
        grid.init(leftPos(), topPos());
    }

    // ───────────────────────────────────────
    // MAIN RENDER LOOP
    // ───────────────────────────────────────

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        // dark background
        this.renderBackground(g);

        // CORE UI MODULES
        equipment.render(g);
        grid.render(g);
        loot.render(g, leftPos(), topPos());
        vicinity.render(g, leftPos(), topPos());

        // drag preview
        if (dragState.isDragging()) {
            ItemStack stack = dragState.getDragging();
            g.renderItem(stack, mouseX - 8, mouseY - 8);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    // ───────────────────────────────────────
    // BACKGROUND
    // ───────────────────────────────────────

    @Override
    public void renderBackground(@NotNull GuiGraphics g) {
        g.fill(0, 0, width, height, 0xAA000000);
    }

    // ───────────────────────────────────────
    // INPUT HANDLING
    // ───────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        int slot = grid.getSlotAt(mouseX, mouseY);

        if (slot >= 0) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
