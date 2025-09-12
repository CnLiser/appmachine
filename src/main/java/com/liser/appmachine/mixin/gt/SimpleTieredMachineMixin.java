package com.liser.appmachine.mixin.gt;

import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;

import com.liser.appmachine.api.cover.trait.CoverBehaviorConfigurator;
import com.liser.appmachine.api.gui.IFancyUIProvider;
import com.liser.appmachine.api.machine.trait.AESimpleTieredMachine;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.core.Direction;

import appeng.api.networking.IManagedGridNode;
import com.liser.appmachine.AppMachine;
import com.liser.appmachine.api.machine.trait.GridMachineNodeHolder;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;
import lombok.Setter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(value = SimpleTieredMachine.class)
public abstract class SimpleTieredMachineMixin implements AESimpleTieredMachine, IFancyUIProvider {

    @Unique
    @Persisted
    protected GridMachineNodeHolder nodeHolder;

    @Unique
    @DescSynced
    @Getter
    @Setter
    protected boolean isOnline;

    @Unique
    @Getter
    @Setter
    private Set<Direction> directions;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void onSimpleTieredMachineConstruct(IMachineBlockEntity holder, int tier,
                                                Int2IntFunction tankScalingFunction, Object[] args, CallbackInfo ci) {
        this.nodeHolder = createNodeHolder();
        AppMachine.LOGGER.warn("machine {} is being constructed!", this);
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Unique
    protected GridMachineNodeHolder createNodeHolder() {
        return new GridMachineNodeHolder(this);
    }

    @Unique
    public void onAttached(Set<Direction> directions) {
        this.setDirections(directions);
        this.getMainNode().setExposedOnSides(directions);
    }

    @Unique
    public void onCoverRemove() {
        this.setDirections(new HashSet<>());
        this.getMainNode().setExposedOnSides(directions);
    }

    @Unique
    public void attachRightConfigurators(ConfiguratorPanel configuratorPanel) {
        MetaMachine machine = (SimpleTieredMachine) (Object)this;
        for (var direction : Direction.values()) {
            if (machine.getCoverContainer().hasCover(direction)) {
                var configurator = ((CoverBehaviorConfigurator) machine.getCoverContainer().getCoverAtSide(direction)).getRightConfigurator();
                if (configurator != null)
                    configuratorPanel.attachConfigurators(configurator);
            }
        }
    }

    @Shadow(remap = false)
    protected abstract IFancyConfigurator createAutoOutputItemConfigurator();
}
