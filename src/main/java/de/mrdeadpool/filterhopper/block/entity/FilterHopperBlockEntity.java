package de.mrdeadpool.filterhopper.block.entity;

import de.mrdeadpool.filterhopper.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public class FilterHopperBlockEntity extends BlockEntity implements MenuProvider {

    public FilterHopperBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FILTER_HOPPER_BE.get(), pos, state);
    }

    // Transport-Inventar (wie Vanilla Hopper)
    private final ItemStackHandler inventory = new ItemStackHandler(5) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final ItemStackHandler filterItems = new ItemStackHandler(5) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("FilterItems", filterItems.serializeNBT(provider));
        tag.put("Inventory", inventory.serializeNBT(provider));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("FilterItems")) {
            filterItems.deserializeNBT(provider, tag.getCompound("FilterItems"));
        }
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(provider, tag.getCompound("Inventory"));
        }
    }

    public ItemStackHandler getFilterItems() {
        return filterItems;
    }

    // ── Filter-Logik ──────────────────────────────────────────────────────────

    // Prüft ob ein Item einem der Filter-Templates entspricht
    public boolean matchesFilter(ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (int i = 0; i < filterItems.getSlots(); i++) {
            ItemStack template = filterItems.getStackInSlot(i);
            if (!template.isEmpty() && ItemStack.isSameItem(template, stack)) {
                return true;
            }
        }
        return false;
    }

    // Prüft ob mindestens ein Filter-Slot belegt ist
    public boolean hasAnyFilter() {
        for (int i = 0; i < filterItems.getSlots(); i++) {
            if (!filterItems.getStackInSlot(i).isEmpty()) return true;
        }
        return false;
    }

    // ── Hopper Tick-Logik ─────────────────────────────────────────────────────

    public static void serverTick(net.minecraft.world.level.Level level,
                                  BlockPos pos, BlockState state, FilterHopperBlockEntity be) {

        // Kein Filter gesetzt → nichts tun
        if (!be.hasAnyFilter()) return;

        // Prüfen ob Quelle oben vorhanden
        IItemHandler source = level.getCapability(
                Capabilities.ItemHandler.BLOCK, pos.above(), Direction.DOWN);
        if (source == null) return;

        // Prüfen ob Ziel in Facing-Richtung vorhanden
        Direction facing = state.getValue(
                de.mrdeadpool.filterhopper.block.FilterHopperBlock.FACING);
        IItemHandler target = level.getCapability(
                Capabilities.ItemHandler.BLOCK, pos.relative(facing), facing.getOpposite());
        if (target == null) return;

        // Cooldown (wie Vanilla Hopper: alle 8 Ticks)
        be.tickCounter++;
        if (be.tickCounter < 8) return;
        be.tickCounter = 0;

        // 1. Items von oben ansaugen
        be.pullItems(level, pos);

        // 2. Items in Facing-Richtung weitergeben
        be.pushItems(level, pos);
    }

    private int tickCounter = 0;

    // Items von oben (oder aus Inventar darüber) ansaugen
    private void pullItems(net.minecraft.world.level.Level level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockEntity aboveBe = level.getBlockEntity(above);
        if (aboveBe == null) return;

        IItemHandler source = level.getCapability(
                Capabilities.ItemHandler.BLOCK, above, Direction.DOWN);
        if (source == null) return;

        for (int i = 0; i < source.getSlots(); i++) {
            ItemStack stack = source.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (!matchesFilter(stack)) continue;

            // Versuchen zu extrahieren
            ItemStack extracted = source.extractItem(i, 1, true);
            if (extracted.isEmpty()) continue;

            // In eigenes Inventar legen
            ItemStack remainder = inventory.insertItem(
                    getFirstAvailableSlot(), extracted, false);
            if (remainder.isEmpty()) {
                source.extractItem(i, 1, false);
                setChanged();
                break;
            }
        }
    }

    // Items nach unten weitergeben
    // Items in Richtung FACING weitergeben
    private void pushItems(net.minecraft.world.level.Level level, BlockPos pos) {
        Direction facing = level.getBlockState(pos)
                .getValue(de.mrdeadpool.filterhopper.block.FilterHopperBlock.FACING);
        BlockPos target = pos.relative(facing);

        IItemHandler targetHandler = level.getCapability(
                Capabilities.ItemHandler.BLOCK, target, facing.getOpposite());
        if (targetHandler == null) return;

        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (!matchesFilter(stack)) continue;

            ItemStack remainder = ItemHandlerHelper.insertItem(targetHandler, stack, false);
            if (remainder.getCount() < stack.getCount()) {
                inventory.setStackInSlot(i, remainder);
                setChanged();
                break;
            }
        }
    }

    private int getFirstAvailableSlot() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).isEmpty()) return i;
        }
        return 0;
    }

    public boolean stillValid(Player player) {
        if (this.level == null) return false;
        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.filterhopper.filter_hopper");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new de.mrdeadpool.filterhopper.menu.FilterHopperMenu(id, inv, this);
    }
}