package com.liser.appmachine.api.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.liser.appmachine.api.cover.slot.CoverSlotHandler;
import com.liser.appmachine.api.cover.trait.MECover;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MENetworkCover extends MECover implements IUICover {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MENetworkCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    protected final static int CONFIG_SIZE = 16;

    @Persisted
    @Getter
    @Setter
    private boolean workingEnabled = true;
    private final List<CoverSlotHandler> coverSlotHandlers;


    public MENetworkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        this.coverSlotHandlers = new ArrayList<>(CONFIG_SIZE);
    }

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(3, 5, () -> isOnline() ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));


        return group;
    }
}
