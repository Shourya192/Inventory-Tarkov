package com.tarkovinventory;

import com.tarkovinventory.capability.ModCapabilities;
import com.tarkovinventory.compat.TaczCompat;
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
        modEventBus.addListener(ModCapabilities::onRegisterCapabilities);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ModCapabilities.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            TaczCompat.registerSizes();
        });
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }
}
