package com.tarkovinventory.client;

import com.tarkovinventory.TarkovInventoryMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * CLEAN CLIENT SETUP (STANDALONE UI VERSION)
 * - NO MenuScreens.register (we are not using containers anymore)
 * - ONLY keybind registration
 */
@Mod.EventBusSubscriber(
        modid = TarkovInventoryMod.MOD_ID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientSetup {

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyHandler.OPEN_INVENTORY);
    }
}
