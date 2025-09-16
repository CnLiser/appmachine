package com.liser.appmachine.api.item;

import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.liser.appmachine.api.cover.slot.CoverSlot;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public record CoverSlotBehaviour(Function<ItemStack, CoverSlot> SlotCreator) implements IItemUIFactory {

    @Override
    public void onAttached(Item item) {
        IItemUIFactory.super.onAttached(item);
        CoverSlot.SLOTS.put(item, SlotCreator);
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player entityPlayer) {
        return null;
    }
}
