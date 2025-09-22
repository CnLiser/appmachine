package com.liser.appmachine.api.cover;

import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.transfer.item.ItemHandlerDelegate;
import com.liser.appmachine.api.cover.slot.CoverSlot;
import com.liser.appmachine.api.cover.slot.CoverSlotHandler;
import com.liser.appmachine.api.cover.slot.CoverSlotHandlers;
import com.liser.appmachine.api.cover.trait.MECover;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MENetworkCover extends MECover implements IUICover {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MENetworkCover.class,
            MECover.MANAGED_FIELD_HOLDER);

    protected final static int CONFIG_SIZE = 9;

    @Persisted
    @Getter
    @Setter
    private boolean workingEnabled = true;

    @Persisted
    @DescSynced
    @Getter
    private final CoverSlotHandler[] coverSlotHandler = new CoverSlotHandler[CONFIG_SIZE];



    public MENetworkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        for (int i = 0; i < CONFIG_SIZE; i++) {
            CoverSlotHandler<ItemStack, CoverSlot> handler = CoverSlotHandlers.slot(this)
                    .onSlotLoaded(f -> configureSlot(f, CoverSlotHandler.LOADED_ID))
                    .onSlotRemoved(f -> configureSlot(f, CoverSlotHandler.REMOVE_ID))
                    .onSlotUpdated(f -> configureSlot(f, CoverSlotHandler.UPDATE_ID));
            coverSlotHandler[i] = (handler);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
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

        for (int i = 0; i < coverSlotHandler.length; i++) {
            CoverSlotHandler handler = coverSlotHandler[i];
            group.addWidget(handler.createCoverSlotUI((i * 18), 20));
            group.addWidget(handler.createFilterConfigUI(6, 34, 0, 96));
        }

        buildAdditionalUI(group);

        return group;
    }


    protected void buildAdditionalUI(WidgetGroup group) {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    protected void configureSlot(CoverSlot slot, int type) {
        switch (type) {
            case CoverSlotHandler.LOADED_ID:
                slot.onLoad();
                break;
            case CoverSlotHandler.REMOVE_ID:
                slot.onRemove(this.getMainNode());
                break;
            case CoverSlotHandler.UPDATE_ID:
                slot.onUpdate();
                break;
        }
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        for (CoverSlotHandler handler : coverSlotHandler) {
            if(!handler.getSlotItem().isEmpty()) {
                list.add(handler.getSlotItem());
            }
        }
        return list;
    }


    /////////////////////////////////////
    // *** CAPABILITY OVERRIDE ***//
    /////////////////////////////////////

    private CoverableItemHandlerWrapper itemHandlerWrapper;

    @Nullable
    @Override
    public IItemHandlerModifiable getItemHandlerCap(@Nullable IItemHandlerModifiable defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        if (itemHandlerWrapper == null || itemHandlerWrapper.delegate != defaultValue) {
            this.itemHandlerWrapper = new CoverableItemHandlerWrapper(defaultValue);
        }
        return itemHandlerWrapper;
    }

    private class CoverableItemHandlerWrapper extends ItemHandlerDelegate {

        public CoverableItemHandlerWrapper(IItemHandlerModifiable delegate) {
            super(delegate);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return super.insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return super.extractItem(slot, amount, simulate);
        }
    }
}
