package com.liser.appmachine.api.cover.slot;


import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.liser.appmachine.api.cover.trait.MECover;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Slot<T, S extends Slot<T, S>> extends Predicate<T> {

    WidgetGroup openConfigurator(int x, int y);

    CompoundTag saveSlot();

    void setOnUpdated(Consumer<S> onUpdated);

    default boolean isBlackList() {
        return false;
    }

    default boolean isBlank() {
        return false;
    }

    void setMachine(MetaMachine machine);

    void setCover(MECover cover);
}
