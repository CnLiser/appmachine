package com.liser.appmachine;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import com.liser.appmachine.api.cover.Covers;
import net.minecraft.data.recipes.FinishedRecipe;

import com.liser.appmachine.data.AMRecipes;
import com.liser.appmachine.registry.AMRegistries;

import java.util.function.Consumer;

@GTAddon
public class AppMachineAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return AMRegistries.REGISTRATE;
    }

    @Override
    public void initializeAddon() {}

    @Override
    public String addonModId() {
        return AppMachine.MOD_ID;
    }

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        AMRecipes.init(provider);
    }

    @Override
    public void registerCovers() {
        for (var cover : Covers.ALL_COVERS) {
            GTRegistries.COVERS.register(cover.getId(), cover);
        }
    }
}
