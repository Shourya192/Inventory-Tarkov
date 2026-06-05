package com.tarkovinventory.client;

import com.tarkovinventory.TarkovInventoryMod;
import com.tarkovinventory.network.C2SOpenTarkovPacket;
import com.tarkovinventory.network.ModNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = TarkovInventoryMod.MOD_ID, value = Dist.CLIENT)
public final class KeyHandler {

    public static final KeyMapping OPEN_INVENTORY = new KeyMapping(
            "key.tarkovinventory.open",   // translation key
            GLFW.GLFW_KEY_I,              // default: I key
            "key.categories.tarkovinventory"
    );

    /** Called on the MOD bus to register keymappings. */
    @Mod.EventBusSubscriber(modid = TarkovInventoryMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_INVENTORY);
        }
    }

    /** Called on the FORGE bus to detect key presses each tick. */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (OPEN_INVENTORY.consumeClick()) {
            ModNetwork.CHANNEL.sendToServer(new C2SOpenTarkovPacket());
        }
    }
}
