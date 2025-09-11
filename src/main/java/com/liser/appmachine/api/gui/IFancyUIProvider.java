package com.liser.appmachine.api.gui;

import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;

public interface IFancyUIProvider {

    /**
     * Attach configurators to the right panel.
     */
    default void attachRightConfigurators(ConfiguratorPanel configuratorPanel) {}

}
