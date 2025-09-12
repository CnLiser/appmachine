package com.liser.appmachine.api.cover.trait;

import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.liser.appmachine.api.machine.trait.AESimpleTieredMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;

/**
 * 提供了覆盖版连接ME网络的基本功能
 */
public abstract class MECover extends CoverBehavior {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MECover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    @Persisted
    @Getter
    @Setter
    private boolean workingEnabled = true;

    protected final IActionSource actionSource;


    public MECover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        if(getMainNode() != null) {
            this.actionSource = IActionSource.ofMachine(getMainNode()::getNode);
        }else {
            this.actionSource = null;
        }
    }

    public IManagedGridNode getMainNode() {
        if(!(coverHolder instanceof MachineCoverContainer machineCoverContainer)) return null;
        MetaMachine machine = machineCoverContainer.getMachine();
        if(!(machine instanceof IGridConnectedMachine)) return null;
        return ((IGridConnectedMachine) machine).getMainNode();
    }

    @Override
    public boolean canAttach() {
        if (!(coverHolder instanceof MachineCoverContainer)) return false;

        MetaMachine machine = ((MachineCoverContainer) coverHolder).getMachine();

        if(!(machine instanceof AESimpleTieredMachine)) return false;

        for (var dir : Direction.values()) {
            if (coverHolder.hasCover(dir) && coverHolder.getCoverAtSide(dir) instanceof MECover) {
                return false;
            }
        }
        return super.canAttach();
    }

    @Override
    public void onAttached(@NotNull ItemStack itemStack, @NotNull ServerPlayer player) {
        super.onAttached(itemStack, player);
        if(coverHolder instanceof MachineCoverContainer) {
            MetaMachine machine = ((MachineCoverContainer) coverHolder).getMachine();

            if(machine instanceof AESimpleTieredMachine) {
                ((AESimpleTieredMachine) machine).onAttached(EnumSet.of(attachedSide));
            }
        }
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        getMachine().onCoverRemove();
    }

    private AESimpleTieredMachine getMachine() {
        return (AESimpleTieredMachine) ((MachineCoverContainer) coverHolder).getMachine();
    }


    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

}
