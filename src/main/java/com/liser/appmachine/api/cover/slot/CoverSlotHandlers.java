package com.liser.appmachine.api.cover.slot;

import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import net.minecraft.world.item.ItemStack;

public interface CoverSlotHandlers {

    static CoverSlotHandler<ItemStack, CoverSlot> slot(IEnhancedManaged container) {
        return new CoverSlotHandler<>(container) {

            @Override
            protected CoverSlot loadSlot(ItemStack itemStack) {
                return CoverSlot.loadSlot(itemStack);
            }

            @Override
            protected CoverSlot getEmptySlot() {
                return CoverSlot.EMPTY;
            }

            @Override
            protected boolean canInsertSlotItem(ItemStack itemStack) {
                return CoverSlot.SLOTS.containsKey(itemStack.getItem());
            }
        };
    }

}
