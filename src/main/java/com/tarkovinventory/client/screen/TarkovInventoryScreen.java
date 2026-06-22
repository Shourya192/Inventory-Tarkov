package com.tarkovinventory.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tarkovinventory.client.screen.modules.EquipmentPanelRenderer;
import com.tarkovinventory.client.screen.modules.EquipmentSlot;
import com.tarkovinventory.client.screen.modules.InventoryGridRenderer;
import com.tarkovinventory.client.screen.modules.RightInventoryPanelRenderer;
import com.tarkovinventory.container.TarkovInventoryMenu;
import com.tarkovinventory.inventory.GridInventory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

/**
 * Tarkov inventory UI backed by the real Forge menu.
 *
 * The previous implementation was a standalone Screen, so Minecraft's slot
 * engine never received mouse input. Keeping this class and the existing
 * renderers, but deriving from AbstractContainerScreen, lets vanilla handle
 * hover, click, pickup, drop and swap semantics against server-owned slots.
 */
public class TarkovInventoryScreen extends AbstractContainerScreen<TarkovInventoryMenu> {

    private final EquipmentPanelRenderer equipment = new EquipmentPanelRenderer();
    private final InventoryGridRenderer grid = new InventoryGridRenderer();
    private final RightInventoryPanelRenderer rightPanel = new RightInventoryPanelRenderer();

    private int leftX;
    private int centerX;
    private int rightX;
    private int topY;

    public TarkovInventoryScreen(TarkovInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 650;
        this.imageHeight = 330;
        this.inventoryLabelY = -1000;
        this.titleLabelY = -1000;
    }

    @Override
    protected void init() {
        super.init();

        int spacing = 35;
        int equipmentW = 190;
        int gridW = (GridInventory.MAX_COLS * 18) + 32;
        int rightW = (10 * 18) + 16;
        int totalW = equipmentW + gridW + rightW + (spacing * 2);
        int startX = (this.width - totalW) / 2;

        this.topY = Math.max(18, (this.height - 300) / 2);
        this.leftX = startX;
        this.centerX = leftX + equipmentW + spacing;
        this.rightX = centerX + gridW + spacing;

        equipment.init(leftX, topY);
        grid.init(centerX, topY, menu.getGridInventory().getActiveCols(), menu.getGridInventory().getActiveRows());
        layoutMenuSlots();
    }

    private void layoutMenuSlots() {
        int gridBaseY = grid.getBackpackY();
        for (int i = 0; i < TarkovInventoryMenu.GRID_SLOTS; i++) {
            Slot slot = menu.slots.get(i);
            int col = i % GridInventory.MAX_COLS;
            int row = i / GridInventory.MAX_COLS;
            if (col < menu.getGridInventory().getActiveCols() && row < menu.getGridInventory().getActiveRows()) {
                slot.x = centerX + col * 18;
                slot.y = gridBaseY + row * 18;
            } else {
                slot.x = -1000;
                slot.y = -1000;
            }
        }

        // Main inventory in the right-side panel.
        int mainStart = TarkovInventoryMenu.PLAYER_START;
        int mainY = topY + 20;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                Slot slot = menu.slots.get(mainStart + row * 9 + col);
                slot.x = rightX + col * 18;
                slot.y = mainY + row * 18;
            }
        }

        // Hotbar mirrors Tarkov slots: 1 primary, 2 secondary, 3-9 pockets.
        EquipmentSlot primary = equipment.getSlot("PRIMARY");
        EquipmentSlot secondary = equipment.getSlot("SECONDARY");
        menu.slots.get(TarkovInventoryMenu.HOTBAR_START).x = primary == null ? centerX : primary.x1 + 4;
        menu.slots.get(TarkovInventoryMenu.HOTBAR_START).y = primary == null ? topY : primary.y1 + 7;
        menu.slots.get(TarkovInventoryMenu.HOTBAR_START + 1).x = secondary == null ? centerX + 20 : secondary.x1 + 4;
        menu.slots.get(TarkovInventoryMenu.HOTBAR_START + 1).y = secondary == null ? topY : secondary.y1 + 7;
        for (int i = 0; i < TarkovInventoryMenu.POCKETS_COUNT; i++) {
            Slot slot = menu.slots.get(TarkovInventoryMenu.HOTBAR_START + 2 + i);
            slot.x = grid.getPocketX(i);
            slot.y = grid.getPocketsY();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        grid.updateHover(mouseX, mouseY);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        RenderSystem.disableDepthTest();
        equipment.updateHover(mouseX, mouseY);
        equipment.render(g, false);
        grid.renderWithBackground(g);
        rightPanel.render(g, rightX, topY);
        RenderSystem.enableDepthTest();
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
