package com.liser.appmachine.api.cover.slot;

import appeng.api.networking.IManagedGridNode;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.liser.appmachine.api.cover.trait.MECover;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public interface CoverSlot extends Slot<ItemStack, CoverSlot> {

    Map<ItemLike, Function<ItemStack, CoverSlot>> SLOTS = new HashMap<>();

    static CoverSlot loadSlot(ItemStack itemStack) {
        return SLOTS.get(itemStack.getItem()).apply(itemStack);
    }

    int testItemCount(ItemStack itemStack);

    default void onLoad() {};

    default void onRemove(IManagedGridNode mainNode) {};

    default void onUpdate() {};

    default boolean supportsAmounts() {
        return !isBlackList();
    }

    CoverSlot EMPTY = new CoverSlot() {

        @Override
        public int testItemCount(ItemStack itemStack) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean test(ItemStack itemStack) {
            return true;
        }

        @Override
        public WidgetGroup openConfigurator(int x, int y) {
            throw new NotImplementedException("Not available for empty cover slots");
        }

        @Override
        public CompoundTag saveSlot() {
            throw new NotImplementedException("Not available for empty cover slots");
        }

        @Override
        public void setOnUpdated(Consumer<CoverSlot> onUpdated) {
            throw new NotImplementedException("Not available for empty cover slots");
        }

        @Override
        public void setMachine(MetaMachine machine) {
            throw new NotImplementedException("Not available for empty cover slots");
        }

        @Override
        public void setCover(MECover cover) {
            throw new NotImplementedException("Not available for empty cover slots");
        }

        @Override
        public void update() {
            throw new NotImplementedException("Not available for empty cover slots");
        }
    };

}
