package com.liser.appmachine.api.cover.trait;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.function.IntFunction;
import java.util.function.Predicate;

public class NotifiableItemStackHandler implements IItemHandlerModifiable {

    @Persisted
    @DescSynced
    public final CustomItemStackHandler storage;
    private Boolean isEmpty;
    @Getter
    @Setter
    protected Runnable changeListener;

    public NotifiableItemStackHandler(int slots, IntFunction<CustomItemStackHandler> storageFactory) {
        this.storage = storageFactory.apply(slots);
        this.storage.setOnContentsChanged(this::onContentsChanged);
    }

    public NotifiableItemStackHandler(int slots) {
        this(slots, CustomItemStackHandler::new);
    }

    public NotifiableItemStackHandler setFilter(Predicate<ItemStack> filter) {
        this.storage.setFilter(filter);
        return this;
    }

    public void onContentsChanged() {
        isEmpty = null;
        if (changeListener != null) {
            changeListener.run();
        }
    }

    public boolean isEmpty() {
        if (isEmpty == null) {
            isEmpty = true;
            for (int i = 0; i < storage.getSlots(); i++) {
                if (!storage.getStackInSlot(i).isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    public int getSlots() {
        return storage.getSlots();
    }

    //////////////////////////////////////
    // ******* Capability ********//

    /// ///////////////////////////////////
    @NotNull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return storage.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int index, @NotNull ItemStack stack) {
        storage.setStackInSlot(index, stack);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return storage.insertItem(slot, stack, simulate);
    }

    public ItemStack insertItemInternal(int slot, @NotNull ItemStack stack, boolean simulate) {
        return storage.insertItem(slot, stack, simulate);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return storage.extractItem(slot, amount, simulate);
    }

    public ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
        return storage.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return storage.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return storage.isItemValid(slot, stack);
    }

}
