package com.liser.appmachine.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.machine.trait.IRecipeHandlerTrait;
import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public abstract class NotifiableCoverRecipeHandlerTrait<T> extends CoverBehavior implements IRecipeHandlerTrait<T> {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NotifiableCoverRecipeHandlerTrait.class);
    protected List<Runnable> listeners = new ArrayList<>();

    @Persisted
    @DescSynced
    @Getter
    @Setter
    protected boolean isDistinct;

    public NotifiableCoverRecipeHandlerTrait(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public ISubscription addChangedListener(Runnable listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void notifyListeners() {
        listeners.forEach(Runnable::run);
    }
}
