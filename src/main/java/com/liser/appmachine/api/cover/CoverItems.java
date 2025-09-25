package com.liser.appmachine.api.cover;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;
import com.liser.appmachine.api.item.CoverSlotBehaviour;
import com.liser.appmachine.api.item.MEInputBusSlot;
import com.liser.appmachine.registry.AMCreativeModeTabs;
import com.liser.appmachine.registry.AMRegistries;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.network.chat.Component;

@SuppressWarnings("null")
public class CoverItems {
    static {
        AMRegistries.REGISTRATE.creativeModeTab(() -> AMCreativeModeTabs.COVER);
    }

    public static ItemEntry<ComponentItem> ME_INPUT_BUS = AMRegistries.REGISTRATE.item("me_input_bus_cover", ComponentItem::create)
            .lang("me input bus cover")
            .onRegister(attach(new CoverSlotBehaviour(MEInputBusSlot::loadCoverSlot), new CoverPlaceBehavior(Covers.ME_INPUT_BUS_COVER)))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.appmachine.me_input_bus_cover.tooltip"));
                lines.add(Component.translatable("item.appmachine.me_input_bus_cover.tooltip.item_transfer_rate"));
            })))
            .register();

    public static ItemEntry<ComponentItem> ME_NETWORK_COVER = AMRegistries.REGISTRATE.item("me_network_cover", ComponentItem::create)
            .lang("me network cover")
            .onRegister(attach(new CoverPlaceBehavior(Covers.ME_NETWORK_COVER)))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.appmachine.me_network_cover.tooltip"));
                lines.add(Component.translatable("item.appmachine.me_network_cover.tooltip.item_transfer_rate"));
            })))
            .register();

    public static <T extends ComponentItem> NonNullConsumer<T> attach(IItemComponent... components) {
        return item -> item.attachComponents(components);
    }

    public static void init() {};
}
