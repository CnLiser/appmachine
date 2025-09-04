package com.liser.appmachine.api.cover;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.item.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;
import com.liser.appmachine.registry.AMCreativeModeTabs;
import com.liser.appmachine.registry.AMRegistries;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.network.chat.Component;

@SuppressWarnings("null")
public class CoverItems {
    static {
        AMRegistries.REGISTRATE.creativeModeTab(() -> AMCreativeModeTabs.ITEM);
    }

    public static ItemEntry<ComponentItem> ME_INPUT_BUS = AMRegistries.REGISTRATE.item("me_input_bus", ComponentItem::create)
            .lang("me input bus cover")
            .onRegister(attach(new CoverPlaceBehavior(Covers.ME_INPUT_BUS_COVER)))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.gtceu.robot.arm.tooltip"));
                lines.add(Component.translatable("gtceu.universal.tooltip.item_transfer_rate", 2));
            })))
            .register();


    public static <T extends ComponentItem> NonNullConsumer<T> attach(IItemComponent... components) {
        return item -> item.attachComponents(components);
    }

    public static void init() {};
}
