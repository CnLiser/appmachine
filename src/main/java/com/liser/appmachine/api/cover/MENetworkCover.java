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
import com.liser.appmachine.api.gui.widget.TabsWidget;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
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
import java.util.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MENetworkCover extends MECover implements IUICover {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MENetworkCover.class,
            MECover.MANAGED_FIELD_HOLDER);

    protected final static int CONFIG_SIZE = 8;

    @Persisted
    @Getter
    @Setter
    private boolean workingEnabled = true;

    @Persisted
    @DescSynced
    @Getter
    private final CoverSlotHandler[] coverSlotHandler = new CoverSlotHandler[CONFIG_SIZE];

    protected TabsWidget tabBar;
    @Persisted
    protected int tabBarIndex = 0;
    protected boolean isLoad = false;

    public MENetworkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        for (int i = 0; i < CONFIG_SIZE; i++) {
            int index = i;
            CoverSlotHandler<ItemStack, CoverSlot> handler = CoverSlotHandlers.slot(this)
                    .onSlotLoaded(f -> configureSlot(f, CoverSlotHandler.LOADED_ID, index))
                    .onSlotRemoved(f -> configureSlot(f, CoverSlotHandler.REMOVE_ID, index))
                    .onSlotUpdated(f -> configureSlot(f, CoverSlotHandler.UPDATE_ID, index));
            coverSlotHandler[i] = (handler);
        }
        creteWidgetGroup();
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

    private void creteWidgetGroup() {
        tabBar = new TabsWidget((index, widget) -> {
            this.tabBarIndex = index;
            this.currentPage = widget;
            this.markAsDirty();
            this.createUIWidget();
        }, coverSlotHandler.length, 1, 40, 146, 10);
    }

    protected Widget currentPage;
    protected WidgetGroup mainPage = new WidgetGroup(new Position(0, 0), new Size(146, 130));

    @Override
    public Widget createUIWidget() {
        mainPage.clearAllWidgets();
        mainPage.addWidget(tabBar);
        // ME Network status
        mainPage.addWidget(new LabelWidget(3, 5, () -> isOnline() ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        for (int i = 0; i < coverSlotHandler.length; i++) {
            CoverSlotHandler handler = coverSlotHandler[i];
            mainPage.addWidget(handler.createCoverSlotUI((i * 18) + 1, 20));
            if (!handler.getSlotItem().isEmpty()) {
                this.createSlotConfigUi(i);
            }
        }

        mainPage.addWidget(currentPage);
        buildAdditionalUI(mainPage);

        return mainPage;
    }


    protected void buildAdditionalUI(WidgetGroup group) {
        // Do nothing in the base implementation. This is intended to be overridden by subclasses.
    }

    protected void configureSlot(CoverSlot slot, int type, int index) {
        switch (type) {
            case CoverSlotHandler.LOADED_ID:
                // 首次加载时会每个插槽都执行一次，不初始化ui
                this.createSlotConfigUi(index);
                if (this.isLoad) {
                    this.tabBarIndex = index;
                    tabBar.attachSelectTab(tabBarIndex);
                }
                slot.onLoad();
                break;
            case CoverSlotHandler.REMOVE_ID:
                slot.onRemove(this.getMainNode());
                tabBar.attachRemoveTab(index);
                break;
            case CoverSlotHandler.UPDATE_ID:
                slot.onUpdate();
                break;
        }
    }

    protected void createSlotConfigUi(int index) {
        if (tabBar.isEmpty(index)) {
            CoverSlotHandler handler = coverSlotHandler[index];
            if (handler.getSlotItem().isEmpty()) {
                tabBar.attachSubTab(index, TabsWidget.emptyPage);
                return;
            }
            WidgetGroup groupConfig = new WidgetGroup(new Position(0, 0));
            groupConfig.addWidget(handler.createFilterConfigUI(1, 35, 0, 96));
            tabBar.attachSubTab(index, groupConfig);
        }
    }

    @Override
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        for (CoverSlotHandler handler : coverSlotHandler) {
            if (!handler.getSlotItem().isEmpty()) {
                list.add(handler.getSlotItem());
            }
        }
        return list;
    }


    /////////////////////////////////////
    // *** CAPABILITY OVERRIDE ***//
    /// //////////////////////////////////

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
