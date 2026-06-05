package com.tarkovinventory.event;

import com.tarkovinventory.TarkovInventoryMod;
import com.tarkovinventory.block.TarkovCorpseBlockEntity;
import com.tarkovinventory.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Intercepts player death item drops and converts them to a
 * {@link com.tarkovinventory.block.TarkovCorpseBlock} placed at the player's feet.
 *
 * <p>Registered automatically via {@link Mod.EventBusSubscriber} on the FORGE bus.
 */
@Mod.EventBusSubscriber(modid = TarkovInventoryMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerDeathHandler {

    private PlayerDeathHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        // Collect all ItemStacks the game was about to drop
        List<ItemStack> collected = new ArrayList<>();
        for (ItemEntity ie : event.getDrops()) {
            ItemStack s = ie.getItem();
            if (!s.isEmpty()) collected.add(s.copy());
        }
        if (collected.isEmpty()) return;

        // Find a valid placement position (air or replaceable block)
        BlockPos pos = player.blockPosition();
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos candidate = pos.above(dy);
            BlockState bs = level.getBlockState(candidate);
            if (bs.isAir() || bs.canBeReplaced()) {
                pos = candidate;
                break;
            }
        }

        // Place corpse block and store items
        final BlockPos finalPos = pos;
        level.setBlock(finalPos, ModBlocks.TARKOV_CORPSE.get().defaultBlockState(), 3);
        if (level.getBlockEntity(finalPos) instanceof TarkovCorpseBlockEntity be) {
            be.setItems(collected);
            be.setOwnerName(player.getGameProfile().getName());
        }

        // Cancel vanilla drop so items don't scatter on the ground as well
        event.setCanceled(true);
    }
}
