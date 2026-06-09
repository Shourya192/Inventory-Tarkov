package com.tarkovinventory.client.screen;

import com.tarkovinventory.client.screen.layout.*;
import com.tarkovinventory.client.screen.modules.*;
import com.tarkovinventory.container.TarkovInventoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class TarkovInventoryScreen extends AbstractContainerScreen<TarkovInventoryMenu> {

    private final EquipmentPanelRenderer equipment = new EquipmentPanelRenderer();
    private final InventoryGridRenderer grid = new InventoryGridRenderer();
    private final LootPanelRenderer loot = new LootPanelRenderer();
    private final VicinityRenderer vicinity = new VicinityRenderer();

    private UIRoot root;
    private UILayout layout;

    public TarkovInventoryScreen(TarkovInventoryMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();

        root = new UIRoot(width, height);
        layout = new UILayout(root);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        renderBackground(g);

        Panel eq = layout.equipment();
        Panel gr = layout.grid();
        Panel lo = layout.loot();
        Panel vi = layout.vicinity();

        eq.draw(g);
        gr.draw(g);
        lo.draw(g);
        vi.draw(g);

        equipment.render(g, eq);
        grid.render(g, gr);
        loot.render(g, lo);
        vicinity.render(g, vi);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF101010);
    }
}
