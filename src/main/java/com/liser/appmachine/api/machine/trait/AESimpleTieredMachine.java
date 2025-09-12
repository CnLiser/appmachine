package com.liser.appmachine.api.machine.trait;

import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

public interface AESimpleTieredMachine extends IGridConnectedMachine {

    void onAttached(Set<Direction> directions);

    @Unique
    public void onCoverRemove();

}
