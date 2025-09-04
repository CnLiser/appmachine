package com.liser.appmachine.mixin;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.LoadingModList;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AMMixinPlugin implements IMixinConfigPlugin {

    private static final String MIXIN_PACKAGE = "com.liser.appmachine.mixin.";
    private static final Map<String, String> MOD_COMPAT_MIXINS = new HashMap<>();

    static {
        MOD_COMPAT_MIXINS.put("roughlyenoughitems", MIXIN_PACKAGE + "rei");
        addModCompatMixin("gtceu");
        addModCompatMixin("ae2");
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        for (var compatMod : MOD_COMPAT_MIXINS.entrySet()) {
            if (mixinClassName.startsWith(compatMod.getValue())) {
                return isModLoaded(compatMod.getKey());
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    private static void addModCompatMixin(String modId) {
        MOD_COMPAT_MIXINS.put(modId, MIXIN_PACKAGE + modId);
    }

    private static boolean isModLoaded(String modId) {
        if (ModList.get() == null) {
            return LoadingModList.get().getModFileById(modId) != null;
        }
        return ModList.get().isLoaded(modId);
    }
}
