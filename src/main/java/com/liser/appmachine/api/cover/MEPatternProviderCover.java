package com.liser.appmachine.api.cover;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.liser.appmachine.api.cover.slot.CoverSlot;
import com.liser.appmachine.api.cover.trait.CoverBehaviorConfigurator;
import com.liser.appmachine.api.cover.trait.MECover;
import com.liser.appmachine.api.machine.gui.GuiTextures;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MEPatternProviderCover extends MECover implements IControllable, IUICover, CoverBehaviorConfigurator {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEPatternProviderCover.class,
            MECover.MANAGED_FIELD_HOLDER);

    @Persisted
    @Getter
    @Setter
    private boolean workingEnabled = true;
    protected final IO io;

    protected CoverSlot coverSlot;

    protected final ConditionalSubscriptionHandler subscriptionHandler;

    public MEPatternProviderCover(@NotNull CoverDefinition definition, @NotNull ICoverable coverHolder, @NotNull Direction attachedSide, Object... args) {
        super(definition, coverHolder, attachedSide);
        this.io = IO.IN;
        subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isSubscriptionActive);
    }

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public void update() {
        if (!this.shouldSyncME()) return;
        this.getItemFilter().update();
    }

    @Override
    public void onRemoved() {
        this.getItemFilter().onRemove(this.getMainNode(), CoverSlot.TYPE_MACHINE);
        super.onRemoved();
        subscriptionHandler.unsubscribe();
    }

    public CoverSlot getItemFilter() {
        if (coverSlot == null) {
            coverSlot = CoverSlot.loadSlot(attachItem);
            if (coverHolder instanceof MachineCoverContainer mcc) {
                var machine = MetaMachine.getMachine(mcc.getLevel(), mcc.getPos());
                if (machine != null) {
                    coverSlot.setMachine(machine);
                    coverSlot.setCover(this);
                }
            }
        }
        return coverSlot;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(3, 5, () -> getMachine().isOnline() ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        group.addWidget(getItemFilter().openConfigurator(0, 20));
        return group;
    }

    @Override
    public @Nullable IFancyConfigurator getRightConfigurator() {
        return new MEPatternProviderCover.MEPatternProviderCoverConfigurator();
    }

    private class MEPatternProviderCoverConfigurator implements IFancyConfigurator {
        @Override
        public Component getTitle() {
            return Component.translatable("item.appmachine.me_input_bus_cover");
        }

        @Override
        public IGuiTexture getIcon() {
            return GuiTextures.ME_PATTERN_PROVIDER_ICON;
        }

        @Override
        public Widget createConfigurator() {
            WidgetGroup group = new WidgetGroup(new Position(0, 0));
            // ME Network status
            group.addWidget(new LabelWidget(3, 5, () -> getMachine().isOnline() ?
                    "gtceu.gui.me_network.online" :
                    "gtceu.gui.me_network.offline"));

            // Config slots
            group.addWidget(getItemFilter().openConfigurator(0, 20));
            return group;
        }
    }


}
