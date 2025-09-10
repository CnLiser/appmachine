package com.liser.appmachine.data;

import com.liser.appmachine.api.cover.CoverItems;
import com.liser.appmachine.registry.AMCreativeModeTabs;
import com.liser.appmachine.registry.AMRegistries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CommonProxy {

    public CommonProxy() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.register(this);
    }

    public static void init() {
        AMRegistries.REGISTRATE.registerRegistrate();
        AMCreativeModeTabs.init();
        AMDatagen.init();
        CoverItems.init();
    }

    @SubscribeEvent
    public void modConstruct(FMLConstructModEvent event) {
        // this is done to delay initialization of content to be after KJS has set up.
        event.enqueueWork(CommonProxy::init);
    }
}
