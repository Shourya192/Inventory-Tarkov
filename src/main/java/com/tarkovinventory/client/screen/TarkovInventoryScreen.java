package com.tarkovinventory.client.screen;

import com.tarkovinventory.client.screen.layout.Panel;
import com.tarkovinventory.client.screen.layout.UILayout;
import com.tarkovinventory.client.screen.modules.*;
import com.tarkovinventory.container.TarkovInventoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TarkovInventoryScreen extends AbstractContainerScreen<TarkovInventoryMenu> {

    private final EquipmentPanelRenderer equipment = new EquipmentPanelRenderer();
    private final InventoryGridRenderer grid = new InventoryGridRenderer();
    private final LootPanelRenderer loot = new LootPanelRenderer();
    private final VicinityRenderer vicinity = new VicinityRenderer();
    private final DragState dragState = new DragState();

    private UILayout layout;

    public TarkovInventoryScreen(TarkovInventoryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        layout = new UILayout(leftPos, topPos, width, height);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        renderBackground(g);

        Panel eq = layout.equipment();
        Panel gr = layout.grid();
        Panel lo = layout.loot();
        Panel vi = layout.vicinity();

        eq.drawBase(g);
        gr.drawBase(g);
        lo.drawBase(g);
        vi.drawBase(g);

        equipment.render(g);
        grid.render(g);
        loot.render(g, lo.x, lo.y);
        vicinity.render(g, vi.x, vi.y);

        if (dragState.isDragging()) {
            ItemStack stack = dragState.getDragging();
            g.renderItem(stack, mouseX - 8, mouseY - 8);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF101010);
    }
}
