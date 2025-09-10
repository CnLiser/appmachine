package com.liser.appmachine.registry;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import com.gregtechceu.gtceu.common.data.GTMachines;
import com.liser.appmachine.api.cover.CoverItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import com.liser.appmachine.AppMachine;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class AMCreativeModeTabs {

    public static RegistryEntry<CreativeModeTab> COVER = AMRegistries.REGISTRATE.defaultCreativeTab(modeTabName("cover"),
                    builder -> builder
                            .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator(modeTabName("cover"),
                                    AMRegistries.REGISTRATE))
                            .icon(CoverItems.ME_INPUT_BUS::asStack)
                            .title(Component.translatable("tab.appmachine.gtceu.covers.title"))
                            .build())
            .register();

    public static RegistryEntry<CreativeModeTab> MACHINE = AMRegistries.REGISTRATE.defaultCreativeTab(modeTabName("machine"),
                    builder -> builder
                            .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator(modeTabName("machine"),
                                    AMRegistries.REGISTRATE))
                            .icon(() -> GTMachines.ELECTROLYZER[GTValues.LV].asStack())
                            .title(Component.translatable("tab.appmachine.gtceu.machines.title"))
                            .build())
            .register();

    private static String modeTabName(String id) {
        int i = id.indexOf('_');
        if (i > 0) {
            return id;
        } else if (i == 0) {
            return AppMachine.MOD_ID + id;
        } else {
            return AppMachine.MOD_ID + "_" + id;
        }
    }

    public static void init() {
    }
}
