package com.tarkovinventory.client.screen;

import com.tarkovinventory.client.screen.layout.*;
import com.tarkovinventory.client.screen.modules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TarkovInventoryScreen extends Screen {

    private UIRoot root;
    private UILayout layout;

    private final EquipmentPanelRenderer equipment = new EquipmentPanelRenderer();
    private final InventoryGridRenderer grid = new InventoryGridRenderer();
    private final LootPanelRenderer loot = new LootPanelRenderer();
    private final VicinityRenderer vicinity = new VicinityRenderer();

    public TarkovInventoryScreen() {
        super(Component.literal("Inventory"));
    }

    @Override
    protected void init() {
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
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
