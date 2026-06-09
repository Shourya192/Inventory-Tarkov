package com.tarkovinventory.client.screen.modules;

import com.tarkovinventory.client.screen.DragState;

public interface IModule {

    default void mouseClicked(double mouseX, double mouseY, int button, int screenX, int screenY) {}

    default void mouseReleased(double mouseX, double mouseY, int button) {}

    default void mouseDragged(double mouseX, double mouseY, int button, double dx, double dy, DragState drag) {}
}
