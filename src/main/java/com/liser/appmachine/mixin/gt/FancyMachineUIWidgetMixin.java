package com.liser.appmachine.mixin.gt;

import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FancyMachineUIWidget.class)
public class FancyMachineUIWidgetMixin extends WidgetGroup {

    @Unique
    protected ConfiguratorPanel configuratorRightPanel;

    @Shadow(remap = false)
    protected int border = 4;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void onFancyMachineUIWidgetConstruct(IFancyUIProvider mainPage, int width, int height, CallbackInfo ci) {
        addWidget(this.configuratorRightPanel = new ConfiguratorPanel(-(24 + 2), height));
    }

    @Inject(method = "setupFancyUI*",
            at = @At(value = "TAIL"),
            remap = false)
    protected void setupFancyUIMixin(IFancyUIProvider fancyUI, boolean showInventory, CallbackInfo ci) {
        var page = fancyUI.createMainPage((FancyMachineUIWidget) (Object)this);
        // layout
        var size = new Size(Math.max(172, page.getSize().width + border * 2),
                Math.max(86, page.getSize().height + border * 2));

        if(fancyUI instanceof com.liser.appmachine.api.gui.IFancyUIProvider) {
            ( (com.liser.appmachine.api.gui.IFancyUIProvider) fancyUI).attachRightConfigurators(configuratorRightPanel);
            configuratorRightPanel
                    .setSelfPosition(new Position(size.width + 2, getGui().getHeight() - configuratorRightPanel.getSize().height - 4));
        }
    }

    @Inject(method = "clearUI",
            at = @At(value = "TAIL"),
            remap = false)
    protected void onClearUI(CallbackInfo ci) {
        this.configuratorRightPanel.clear();
    }
}
