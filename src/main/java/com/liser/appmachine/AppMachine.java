package com.liser.appmachine;

import com.gregtechceu.gtceu.api.data.chemical.material.event.PostMaterialEvent;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.liser.appmachine.api.cover.CoverItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.liser.appmachine.data.AMDatagen;
import com.liser.appmachine.registry.AMCreativeModeTabs;
import com.liser.appmachine.registry.AMRegistries;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import static com.gregtechceu.gtceu.common.data.GTMaterials.Copper;

@Mod(AppMachine.MOD_ID)
public class AppMachine {

    public static final String MOD_ID = "appmachine";
    public static final String NAME = "AppMachines";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AppMachine() {
        AppMachine.init();
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
    }

    public static void init() {
        ConfigHolder.init();

        AMCreativeModeTabs.init();

        CoverItems.init();

        AMDatagen.init();

        AMRegistries.REGISTRATE.registerRegistrate();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation id(String name) {
        return new ResourceLocation(MOD_ID, FormattingUtil.toLowerCaseUnderscore(name));
    }

    @SubscribeEvent
    public void modifyMaterials(PostMaterialEvent event) {
        // add copper stuff
        Copper.addFlags(
                MaterialFlags.GENERATE_SMALL_GEAR,
                MaterialFlags.GENERATE_ROTOR,
                MaterialFlags.GENERATE_BOLT_SCREW);
    }
}
