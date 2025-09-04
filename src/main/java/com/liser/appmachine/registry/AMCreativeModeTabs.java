package com.liser.appmachine.registry;

import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import com.liser.appmachine.api.cover.CoverItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import com.liser.appmachine.AppMachine;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class AMCreativeModeTabs {

    public static RegistryEntry<CreativeModeTab> ITEM = AMRegistries.REGISTRATE.defaultCreativeTab(AppMachine.MOD_ID,
            builder -> builder
                    .displayItems(new GTCreativeModeTabs.RegistrateDisplayItemsGenerator(AppMachine.MOD_ID,
                            AMRegistries.REGISTRATE))
                     .icon(CoverItems.ME_INPUT_BUS::asStack)
                    .title(Component.literal("ULV Covers Modern"))
                    .build())
            .register();

    public static void init() {}
}
