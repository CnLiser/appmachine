package com.liser.appmachine.mixin.gt;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.CommonProxy;
import com.liser.appmachine.api.machine.Machines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CommonProxy.class)
public class CommonProxyMixin {

    @Inject(method = "init",
            at = @At(value = "INVOKE",
                    target = "Lcom/gregtechceu/gtceu/common/data/machines/GTMachineUtils;init()V",
                    shift = At.Shift.BEFORE),
            remap = false)
    private static void onInit(CallbackInfo ci) {
        Machines.init();
        GTRegistries.MACHINES.freeze();
    }

}
