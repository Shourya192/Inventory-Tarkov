package com.tarkovinventory.command;

import com.mojang.brigadier.CommandDispatcher;
import com.tarkovinventory.container.TarkovInventoryMenu;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public final class TarkovCommand {

    private TarkovCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("ti")
                .executes(ctx -> openInventory(ctx.getSource()))
        );
        dispatcher.register(
            Commands.literal("tarkovinventory")
                .executes(ctx -> openInventory(ctx.getSource()))
        );
    }

    private static int openInventory(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            NetworkHooks.openScreen(player, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return Component.literal("Tarkov Inventory");
                }
                @Override
                public @NotNull AbstractContainerMenu createMenu(
                        int windowId, @NotNull Inventory inv, @NotNull Player p) {
                    return new TarkovInventoryMenu(windowId, inv, 0);
                }
            }, buf -> buf.writeInt(0));
        } catch (Exception ignored) {}
        return 1;
    }
}
