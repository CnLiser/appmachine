package com.liser.appmachine.api.cover;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.SimpleCoverRenderer;
import com.liser.appmachine.AppMachine;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Covers {
    public static final List<CoverDefinition> ALL_COVERS = new ArrayList<>();

    public final static CoverDefinition ME_INPUT_BUS_COVER = register("me_input_bus_cover", MEInputBusCover::new);
    public final static CoverDefinition ME_NETWORK_COVER = register("me_network_cover", MENetworkCover::new);
    public final static CoverDefinition ME_PATTERN_PROVIDER_COVER = register("me_pattern_provider_cover", MEPatternProviderCover::new);


    private static CoverDefinition register(String id, CoverDefinition.CoverBehaviourProvider behaviorCreator) {
        return register(id, behaviorCreator, () -> () -> new SimpleCoverRenderer(AppMachine.id("block/cover/" + id)));
    }

    public static CoverDefinition register(ResourceLocation id, CoverDefinition.CoverBehaviourProvider behaviorCreator,
                                           Supplier<Supplier<ICoverRenderer>> coverRenderer) {
        var definition = new CoverDefinition(id, behaviorCreator, coverRenderer);
        GTRegistries.COVERS.register(definition.getId(), definition);
        return definition;
    }

    public static CoverDefinition register(
            String id,
            CoverDefinition.CoverBehaviourProvider behavior,
            Supplier<Supplier<ICoverRenderer>> renderer
    ) {

        var definition = new CoverDefinition(AppMachine.id(id), behavior, renderer);
        ALL_COVERS.add(definition);
        return definition;
    }

}
