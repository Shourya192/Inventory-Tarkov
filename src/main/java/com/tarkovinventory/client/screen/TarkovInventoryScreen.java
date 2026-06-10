package com.tarkovinventory.client.screen;

import com.tarkovinventory.client.screen.modules.DragState;
import com.tarkovinventory.client.screen.modules.EquipmentPanelRenderer;
import com.tarkovinventory.client.screen.modules.InventoryGridRenderer;
import com.tarkovinventory.client.screen.modules.RightInventoryPanelRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class TarkovInventoryScreen extends Screen {

    private final EquipmentPanelRenderer equipment = new EquipmentPanelRenderer();
    private final InventoryGridRenderer grid = new InventoryGridRenderer();
    private final RightInventoryPanelRenderer rightPanel = new RightInventoryPanelRenderer();
    private final DragState dragState = new DragState();

    private int leftX;
    private int centerX;
    private int rightX;
    private int topY;

    public TarkovInventoryScreen() {
        super(Component.literal("Tarkov Inventory"));
    }

    @Override
    protected void init() {
        super.init();

        int spacing = 35;

        int equipmentW = 180;
        int gridW = (10 * 18) + 32;
        int rightW = (10 * 18) + 16;

        int totalW = equipmentW + gridW + rightW + (spacing * 2);

        int startX = (this.width - totalW) / 2;

        this.topY = (this.height - 260) / 2;

        this.leftX = startX;
        this.centerX = leftX + equipmentW + spacing;
        this.rightX = centerX + gridW + spacing;

        equipment.init(leftX, topY);
        grid.init(centerX, topY);
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {

        renderBackground(g);

        grid.updateHover(mouseX, mouseY);

        equipment.render(g);
        grid.renderWithBackground(g);

        rightPanel.render(g, rightX, topY);

        if (dragState.isDragging()) {
            ItemStack stack = dragState.getDragging();
            g.renderItem(stack, mouseX - 8, mouseY - 8);
        }

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics g) {
        g.fill(0, 0, width, height, 0xCC000000);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
