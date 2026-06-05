package com.tarkovinventory.block;

import com.tarkovinventory.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the full inventory of the player who died here.
 *
 * <p>Minecraft automatically syncs this entity to nearby clients via
 * {@link #getUpdatePacket()} and {@link #getUpdateTag()}, so the client
 * can read it directly from {@code level.getBlockEntity(pos)}.
 */
public class TarkovCorpseBlockEntity extends BlockEntity {

    private List<ItemStack> items     = new ArrayList<>();
    private String          ownerName = "Unknown";

    public TarkovCorpseBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TARKOV_CORPSE.get(), pos, state);
    }

    // ── Data access ──────────────────────────────────────────────────

    public List<ItemStack> getItems()      { return items; }
    public String          getOwnerName()  { return ownerName; }

    public void setOwnerName(String name) {
        this.ownerName = name;
        setChanged();
    }

    public void setItems(List<ItemStack> stacks) {
        this.items = new ArrayList<>();
        for (ItemStack s : stacks) if (!s.isEmpty()) this.items.add(s.copy());
        setChanged();
    }

    public boolean isEmpty() { return items.isEmpty(); }

    /**
     * Removes and returns the item at {@code slot}, or {@link ItemStack#EMPTY}.
     * Caller is responsible for giving the stack to the player.
     */
    public ItemStack takeItem(int slot) {
        if (slot < 0 || slot >= items.size()) return ItemStack.EMPTY;
        ItemStack result = items.remove(slot);
        setChanged();
        return result;
    }

    /**
     * Drains the entire contents and returns them as a snapshot list.
     */
    public List<ItemStack> takeAll() {
        List<ItemStack> snapshot = new ArrayList<>(items);
        items.clear();
        setChanged();
        return snapshot;
    }

    // ── NBT persistence ──────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putString("OwnerName", ownerName);
        ListTag list = new ListTag();
        for (ItemStack stack : items) {
            CompoundTag stag = new CompoundTag();
            stack.save(stag);
            list.add(stag);
        }
        tag.put("Items", list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ownerName = tag.getString("OwnerName");
        items = new ArrayList<>();
        ListTag list = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            ItemStack s = ItemStack.of(list.getCompound(i));
            if (!s.isEmpty()) items.add(s);
        }
    }

    // ── Client sync ──────────────────────────────────────────────────

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() != null) load(pkt.getTag());
    }
}
