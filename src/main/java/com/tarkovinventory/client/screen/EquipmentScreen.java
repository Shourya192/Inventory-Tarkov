package com.tarkovinventory.client.screen;

import com.tarkovinventory.client.screen.modules.EquipmentPanelRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class EquipmentScreen extends Screen {

    private final EquipmentPanelRenderer panel = new EquipmentPanelRenderer();

    public EquipmentScreen() {
        super(Component.literal("Equipment"));
    }

    @Override
    protected void init() {
        super.init();

        int left = this.width / 2 - 120;
        int top = this.height / 2 - 120;

        panel.init(left, top);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {

        this.renderBackground(graphics);

        panel.render(graphics);

        super.render(graphics, mouseX, mouseY, delta);
    }

    // ======================
    // INPUT FORWARDING (CLEAN)
    // ======================
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return panel.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return panel.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
