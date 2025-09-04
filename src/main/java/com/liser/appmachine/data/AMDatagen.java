package com.liser.appmachine.data;

import com.liser.appmachine.data.lang.LangHandler;
import com.liser.appmachine.registry.AMRegistries;
import com.tterrag.registrate.providers.ProviderType;

public class AMDatagen {

    public static void init() {
        AMRegistries.REGISTRATE.addDataGenerator(ProviderType.LANG, LangHandler::init);
    }
}
