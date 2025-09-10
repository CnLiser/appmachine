package com.liser.appmachine.api.machine;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.liser.appmachine.data.machines.AMMachineUtils;
import com.liser.appmachine.registry.AMCreativeModeTabs;
import com.liser.appmachine.registry.AMRegistries;



public class Machines {

    static {
        AMRegistries.REGISTRATE.creativeModeTab(() -> AMCreativeModeTabs.MACHINE);
        GTRegistries.MACHINES.unfreeze();
    }

    public static MachineDefinition[] ELECTRIC_FURNACE = AMMachineUtils.registerSimpleMachines("me_electric_furnace",
            GTRecipeTypes.FURNACE_RECIPES);

    public static void init() {}

}
