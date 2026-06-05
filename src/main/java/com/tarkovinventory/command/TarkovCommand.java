package com.tarkovinventory.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.tarkovinventory.capability.IPlayerEquipment;
import com.tarkovinventory.capability.ModCapabilities;
import com.tarkovinventory.container.TarkovInventoryMenu;
import com.tarkovinventory.inventory.BackpackSizes;
import com.tarkovinventory.inventory.GridInventory;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public final class TarkovCommand {

    private TarkovCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // /ti                        — open inventory screen
        // /ti backpack               — give yourself a Tactical Backpack item
        // /ti curiosinfo             — print all Curios slot IDs + current items to chat
        // /ti gridinfo               — show equipped backpack ID and active grid size
        // /ti setsize <cols> <rows>  — register equipped backpack at given size (session only)
        var root = Commands.literal("ti")
            .executes(ctx -> openInventory(ctx.getSource()))
            .then(Commands.literal("backpack")
                .executes(ctx -> giveBackpack(ctx.getSource())))
            .then(Commands.literal("curiosinfo")
                .executes(ctx -> printCuriosInfo(ctx.getSource())))
            .then(Commands.literal("gridinfo")
                .executes(ctx -> printGridInfo(ctx.getSource())))
            .then(Commands.literal("setsize")
                .then(Commands.argument("cols", IntegerArgumentType.integer(1, GridInventory.MAX_COLS))
                    .then(Commands.argument("rows", IntegerArgumentType.integer(1, GridInventory.MAX_ROWS))
                        .executes(ctx -> setBackpackSize(ctx.getSource(),
                            IntegerArgumentType.getInteger(ctx, "cols"),
                            IntegerArgumentType.getInteger(ctx, "rows"))))));

        dispatcher.register(root);
        // Also register under the full name
        dispatcher.register(Commands.literal("tarkovinventory").redirect(
            dispatcher.getRoot().getChild("ti")));
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

    /**
     * /ti gridinfo — shows the equipped backpack's item ID and active grid size.
     * Useful for finding the item ID to add to BackpackSizes.java.
     */
    private static int printGridInfo(CommandSourceStack source) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ItemStack backpack = getBackSlotItem(player);

            player.displayClientMessage(
                Component.literal("§6=== Tarkov Grid Info ==="), false);

            if (backpack.isEmpty()) {
                player.displayClientMessage(
                    Component.literal("§eNo backpack equipped in the ON BACK / Curios 'back' slot."), false);
                player.displayClientMessage(
                    Component.literal("§7Equip something in that slot, then run §f/ti gridinfo§7 again."), false);
                return 1;
            }

            var regKey = ForgeRegistries.ITEMS.getKey(backpack.getItem());
            String itemId = regKey != null ? regKey.toString() : "unknown";
            int cols = BackpackSizes.getCols(backpack);
            int rows = BackpackSizes.getRows(backpack);
            boolean isRegistered = cols != BackpackSizes.DEFAULT_COLS || rows != BackpackSizes.DEFAULT_ROWS;

            player.displayClientMessage(
                Component.literal("§7Item ID: §f" + itemId), false);
            player.displayClientMessage(
                Component.literal("§7Display name: §f" + backpack.getHoverName().getString()), false);
            player.displayClientMessage(
                Component.literal("§7Active grid: §a" + cols + "§7×§a" + rows
                    + (isRegistered ? " §7(registered)" : " §e(default — not in BackpackSizes.java)")),
                false);

            if (!isRegistered) {
                player.displayClientMessage(
                    Component.literal("§7To permanently set a size, add this to BackpackSizes.java:"), false);
                player.displayClientMessage(
                    Component.literal("§f  register(\"" + itemId + "\", " + cols + ", " + rows + ");"), false);
                player.displayClientMessage(
                    Component.literal("§7Or test it now: §f/ti setsize <cols> <rows>"), false);
            }

        } catch (Exception e) {
            try {
                source.getPlayerOrException().displayClientMessage(
                    Component.literal("§cError: " + e.getClass().getSimpleName() + ": " + e.getMessage()), false);
            } catch (Exception ignored) {}
        }
        return 1;
    }

    /**
     * /ti setsize <cols> <rows> — registers the equipped backpack at the given
     * grid size for this session. Resets when the world is reloaded.
     * Use /ti gridinfo afterwards to confirm, then copy the line into BackpackSizes.java.
     */
    private static int setBackpackSize(CommandSourceStack source, int cols, int rows) {
        try {
            ServerPlayer player = source.getPlayerOrException();
            ItemStack backpack = getBackSlotItem(player);

            if (backpack.isEmpty()) {
                player.displayClientMessage(
                    Component.literal("§cNo backpack in the ON BACK / Curios 'back' slot. Equip one first."), false);
                return 0;
            }

            var regKey = ForgeRegistries.ITEMS.getKey(backpack.getItem());
            String itemId = regKey != null ? regKey.toString() : null;
            if (itemId == null) {
                player.displayClientMessage(
                    Component.literal("§cCouldn't get an item ID for that backpack."), false);
                return 0;
            }

            BackpackSizes.register(itemId, cols, rows);

            player.displayClientMessage(
                Component.literal("§aRegistered §f\"" + itemId + "\"§a → §f"
                    + cols + "§a×§f" + rows + "§a grid for this session."), false);
            player.displayClientMessage(
                Component.literal("§7Reopen the inventory (§f/ti§7) to see it."), false);
            player.displayClientMessage(
                Component.literal("§7To make it permanent, add to BackpackSizes.java:"), false);
            player.displayClientMessage(
                Component.literal("§f  register(\"" + itemId + "\", " + cols + ", " + rows + ");"), false);

        } catch (Exception e) {
            try {
                source.getPlayerOrException().displayClientMessage(
                    Component.literal("§cError: " + e.getClass().getSimpleName() + ": " + e.getMessage()), false);
            } catch (Exception ignored) {}
        }
        return 1;
    }

    /**
     * Returns the item in the player's ON BACK slot (Curios 'back' index 0,
     * or capability SLOT_ON_BACK as fallback).
     */
    private static ItemStack getBackSlotItem(ServerPlayer player) {
        // Try Curios 'back' slot first
        try {
            if (net.minecraftforge.fml.ModList.get().isLoaded("curios")) {
                Class<?> apiClass = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                Object helper = apiClass.getMethod("getCuriosHelper").invoke(null);
                Optional<?> optHandler = (Optional<?>) helper.getClass()
                    .getMethod("getCuriosHandler", Player.class).invoke(helper, player);
                if (optHandler.isPresent()) {
                    Object handler = optHandler.get();
                    Map<?, ?> curios = (Map<?, ?>) handler.getClass().getMethod("getCurios").invoke(handler);
                    for (Map.Entry<?, ?> e : curios.entrySet()) {
                        if (!"back".equals(e.getKey().toString())) continue;
                        Object stacks = e.getValue().getClass().getMethod("getStacks").invoke(e.getValue());
                        ItemStack s = (ItemStack) stacks.getClass()
                            .getMethod("getStackInSlot", int.class).invoke(stacks, 0);
                        if (!s.isEmpty()) return s;
                    }
                }
            }
        } catch (Exception ignored) {}

        // Fallback: capability SLOT_ON_BACK
        return ModCapabilities.get(player)
            .map(cap -> cap.getSlot(IPlayerEquipment.SLOT_ON_BACK))
            .orElse(ItemStack.EMPTY);
    }
}
