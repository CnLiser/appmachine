package com.liser.appmachine.mixin.gt;

import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.liser.appmachine.api.cover.trait.CoverBehaviorConfigurator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CoverBehavior.class)
public abstract class CoverBehaviorMixin implements CoverBehaviorConfigurator {

    @Unique
    public @Nullable IFancyConfigurator getRightConfigurator() {
        return null;
    }

}
