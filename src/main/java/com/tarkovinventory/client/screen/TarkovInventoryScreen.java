package com.tarkovinventory.client.screen;

import com.tarkovinventory.client.screen.modules.*;
import com.tarkovinventory.container.TarkovInventoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * CLEAN CONTROLLER:
 * - No inventory logic
 * - No rig logic
 * - No loot logic
 * - Only delegates rendering + input
 */
public class TarkovInventoryScreen extends AbstractContainerScreen<TarkovInventoryMenu> {

    // Modules
    private final EquipmentPanelRenderer equipment = new EquipmentPanelRenderer();
    private final InventoryGridRenderer grid = new InventoryGridRenderer();
    private final LootPanelRenderer loot = new LootPanelRenderer();
    private final VicinityRenderer vicinity = new VicinityRenderer();
    private final DragState dragState = new DragState();

    public TarkovInventoryScreen(TarkovInventoryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();

        equipment.init(leftPos, topPos);
        grid.init(leftPos, topPos);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        this.renderBackground(g);

        // Render modules
        equipment.render(g);
        grid.render(g);
        loot.render(g, leftPos, topPos);
        vicinity.render(g, leftPos, topPos);

        // Drag preview
        if (dragState.isDragging()) {
            ItemStack stack = dragState.getDragging();
            g.renderItem(stack, mouseX - 8, mouseY - 8);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(
                leftPos,
                topPos,
                leftPos + imageWidth,
                topPos + imageHeight,
                0xFF101010
        );
    }

    // =========================
    // INPUT HANDLING (FIXED)
    // =========================

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        int x = leftPos;
        int y = topPos;

        equipment.mouseClicked(mouseX, mouseY, button, x, y);
        grid.mouseClicked(mouseX, mouseY, button, x, y);
        loot.mouseClicked(mouseX, mouseY, button, x, y);
        vicinity.mouseClicked(mouseX, mouseY, button, x, y);

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {

        equipment.mouseReleased(mouseX, mouseY, button);
        grid.mouseReleased(mouseX, mouseY, button);
        loot.mouseReleased(mouseX, mouseY, button);

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {

        grid.mouseDragged(mouseX, mouseY, button, dx, dy, dragState);
        loot.mouseDragged(mouseX, mouseY, button, dx, dy, dragState);

        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }
}
