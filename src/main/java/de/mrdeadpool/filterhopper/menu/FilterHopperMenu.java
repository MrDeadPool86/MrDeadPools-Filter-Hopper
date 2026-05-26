package de.mrdeadpool.filterhopper.menu;

import de.mrdeadpool.filterhopper.ModMenuTypes;
import de.mrdeadpool.filterhopper.block.entity.FilterHopperBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class FilterHopperMenu extends AbstractContainerMenu {

    private final FilterHopperBlockEntity blockEntity;

    // Server-Konstruktor
    public FilterHopperMenu(int id, Inventory inv, FilterHopperBlockEntity be) {
        super(ModMenuTypes.FILTER_HOPPER_MENU.get(), id);
        this.blockEntity = be;

        // 5 Ghost-Slots (Vanilla Hopper Positionen)
        for (int i = 0; i < 5; i++) {
            this.addSlot(new GhostSlot(be.getFilterItems(), i, 44 + i * 18, 20));
        }

        // Spieler-Inventar (3 Reihen)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9,
                        8 + col * 18, 51 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 109));
        }
    }

    // Client-Konstruktor (Netzwerk)
    public FilterHopperMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, (FilterHopperBlockEntity)
                inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public FilterHopperBlockEntity getBlockEntity() {
        return blockEntity;
    }

    // Ghost-Slots nehmen kein Shift+Klick an
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 5) return ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < 5 + 27) {
            if (!this.moveItemStackTo(stack, 5 + 27, 5 + 36, false))
                return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(stack, 5, 5 + 27, false))
                return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity.stillValid(player);
    }
}