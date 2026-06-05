package com.tarkovinventory.command;

import com.mojang.brigadier.CommandDispatcher;
import com.tarkovinventory.container.TarkovInventoryMenu;
import com.tarkovinventory.registry.ModItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public final class TarkovCommand {

    private TarkovCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /ti               — open inventory
        // /ti backpack      — give yourself a Tactical Backpack item
        // /ti curiosinfo    — print all Curios slot IDs and their current items to chat
        dispatcher.register(
            Commands.literal("ti")
                .executes(ctx -> openInventory(ctx.getSource()))
                .then(Commands.literal("backpack")
                    .executes(ctx -> giveBackpack(ctx.getSource())))
                .then(Commands.literal("curiosinfo")
                    .executes(ctx -> printCuriosInfo(ctx.getSource())))
        );
        dispatcher.register(
            Commands.literal("tarkovinventory")
                .executes(ctx -> openInventory(ctx.getSource()))
                .then(Commands.literal("backpack")
                    .executes(ctx -> giveBackpack(ctx.getSource())))
                .then(Commands.literal("curiosinfo")
                    .executes(ctx -> printCuriosInfo(ctx.getSource())))
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

    private static int giveBackpack(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ItemStack backpack = new ItemStack(ModItems.TACTICAL_BACKPACK.get());
            player.getInventory().add(backpack);
            player.displayClientMessage(
                Component.literal("§aGave you a Tactical Backpack! Equip it in the ON BACK slot."),
                false
            );
        } catch (Exception ignored) {}
        return 1;
    }

    /**
     * Prints every Curios slot ID and its current item to the player's chat.
     * Use this to find out what slot IDs your mods register.
     */
    private static int printCuriosInfo(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();

            // Check Curios is loaded
            if (!net.minecraftforge.fml.ModList.get().isLoaded("curios")) {
                player.displayClientMessage(
                    Component.literal("§cCurios API is not loaded."), false);
                return 1;
            }

            // Use reflection (same pattern as CuriosCompat) to enumerate slots server-side
            Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method helperMethod = apiClass.getMethod("getCuriosHelper");
            Object helper = helperMethod.invoke(null);
            Method handlerMethod = helper.getClass().getMethod("getCuriosHandler", Player.class);
            Optional<?> optHandler = (Optional<?>) handlerMethod.invoke(helper, player);

            if (!optHandler.isPresent()) {
                player.displayClientMessage(
                    Component.literal("§eNo Curios handler found for your player."), false);
                return 1;
            }

            Object handler = optHandler.get();
            Method getCurios = handler.getClass().getMethod("getCurios");
            Map<?, ?> curios = (Map<?, ?>) getCurios.invoke(handler);

            if (curios.isEmpty()) {
                player.displayClientMessage(
                    Component.literal("§eNo Curios slots found. Make sure mods that add Curios slots are installed."), false);
                return 1;
            }

            player.displayClientMessage(
                Component.literal("§6=== Curios Slot IDs ==="), false);

            for (Map.Entry<?, ?> entry : curios.entrySet()) {
                String slotId = entry.getKey().toString();
                Object stacksHandler = entry.getValue();
                Method getStacks = stacksHandler.getClass().getMethod("getStacks");
                Object stacks = getStacks.invoke(stacksHandler);
                Method getSlots = stacks.getClass().getMethod("getSlots");
                int slotCount = (int) getSlots.invoke(stacks);
                Method getStack = stacks.getClass().getMethod("getStackInSlot", int.class);

                for (int i = 0; i < slotCount; i++) {
                    ItemStack stack = (ItemStack) getStack.invoke(stacks, i);
                    String itemName = stack.isEmpty() ? "§8(empty)" : "§a" + stack.getHoverName().getString();
                    player.displayClientMessage(
                        Component.literal("§e\"" + slotId + "\"§7 [" + i + "] → " + itemName),
                        false
                    );
                }
            }

            player.displayClientMessage(
                Component.literal("§7Use these IDs to map your Curios slots."), false);

        } catch (Exception e) {
            try {
                source.getPlayerOrException().displayClientMessage(
                    Component.literal("§cError reading Curios slots: " + e.getClass().getSimpleName()), false);
            } catch (Exception ignored) {}
        }
        return 1;
    }
}
