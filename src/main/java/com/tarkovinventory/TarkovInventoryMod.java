package com.tarkovinventory;

import com.tarkovinventory.capability.ModCapabilities;
import com.tarkovinventory.compat.TaczCompat;
import com.tarkovinventory.network.ModNetwork;
import com.tarkovinventory.registry.ModItems;
import com.tarkovinventory.registry.ModMenuTypes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TarkovInventoryMod.MOD_ID)
public class TarkovInventoryMod {

    public static final String MOD_ID = "tarkovinventory";

    public TarkovInventoryMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // RegisterCapabilitiesEvent fires on the MOD bus — register it here explicitly.
        // NOTE: do NOT also call MinecraftForge.EVENT_BUS.register(ModCapabilities.class)
        // because @Mod.EventBusSubscriber on that class already handles the FORGE bus events
        // (AttachCapabilitiesEvent). Double-registering causes duplicate capability attachment.
        modEventBus.addListener(ModCapabilities::onRegisterCapabilities);

        MinecraftForge.EVENT_BUS.register(this);
        // ModCapabilities Forge-bus events are handled automatically via @Mod.EventBusSubscriber
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetwork.register();
            TaczCompat.registerSizes();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }
}
