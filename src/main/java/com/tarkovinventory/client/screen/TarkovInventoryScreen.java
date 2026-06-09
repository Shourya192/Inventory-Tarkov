package com.tarkovinventory.client.screen;

import com.tarkovinventory.client.screen.modules.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * STANDALONE TARKOV UI SCREEN (FINAL FIXED VERSION)
 * - No Forge containers
 * - No MenuScreens
 * - Pure client-side UI
 */
public class TarkovInventoryScreen extends Screen {

    private final EquipmentPanelRenderer equipment = new EquipmentPanelRenderer();
    private final InventoryGridRenderer grid = new InventoryGridRenderer();
    private final LootPanelRenderer loot = new LootPanelRenderer();
    private final VicinityRenderer vicinity = new VicinityRenderer();
    private final DragState dragState = new DragState();

    public TarkovInventoryScreen() {
        super(Component.literal("Tarkov Inventory"));
    }

    // ───────────────────────────────────────
    // INIT
    // ───────────────────────────────────────

    @Override
    protected void init() {
        super.init();

        int baseX = 20;
        int baseY = 20;

        equipment.init(baseX, baseY);
        grid.init(baseX, baseY);
    }

    // ───────────────────────────────────────
    // RENDER
    // ───────────────────────────────────────

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        renderBackground(g);

        // MODULES (ALL CONSISTENT NOW)
        equipment.render(g);
        grid.render(g);
        loot.render(g, 0, 0);
        vicinity.render(g, 0, 0);

        // Drag item preview
        if (dragState.isDragging()) {
            ItemStack stack = dragState.getDragging();
            g.renderItem(stack, mouseX - 8, mouseY - 8);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics g) {
        g.fill(0, 0, width, height, 0xAA000000);
    }

    // ───────────────────────────────────────
    // INPUT
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
    public boolean isPauseScreen() {
        return false;
    }
}
