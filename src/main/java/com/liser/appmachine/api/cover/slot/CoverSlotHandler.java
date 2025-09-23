package com.liser.appmachine.api.cover.slot;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.liser.appmachine.api.cover.MEInputBusCover;
import com.liser.appmachine.api.cover.trait.MECover;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CoverSlotHandler<T, F extends Slot<T, F>> implements IEnhancedManaged {

    private final IEnhancedManaged container;

    public final static int LOADED_ID = 1000;
    public final static int REMOVE_ID = 1100;
    public final static int UPDATE_ID = 1200;

    @Persisted
    @DescSynced
    @Getter
    private @NotNull ItemStack slotItem = ItemStack.EMPTY;

    private @Nullable F slot;
    private @Nullable CustomItemStackHandler coverSlot;
    private @Nullable WidgetGroup filterGroup;

    private @NotNull Consumer<F> onSlotLoaded = (slot) -> {
    };
    private @NotNull Consumer<F> onSlotRemoved = (slot) -> {
    };
    private @NotNull Consumer<F> onSlotUpdated = (slot) -> {
    };

    public CoverSlotHandler(IEnhancedManaged container) {
        this.container = container;
    }

    protected abstract F loadSlot(ItemStack slotItem);

    protected abstract F getEmptySlot();

    protected abstract boolean canInsertSlotItem(ItemStack slotItem);

    //////////////////////////////////
    // ***** PUBLIC API ******//

    /// ///////////////////////////////

    public Widget createCoverSlotUI(int xPos, int yPos) {
        return new SlotWidget(getCoverSlot(), 0, xPos, yPos)
                .setChangeListener(this::updateSlot)
                .setBackgroundTexture(new GuiTextureGroup(GuiTextures.SLOT, GuiTextures.FILTER_SLOT_OVERLAY));
    }

    public F getSlot() {
        if (this.slot == null) {
            if (this.slotItem.isEmpty()) {
                return getEmptySlot();
            } else {
                loadSlotFromItem();
            }
        }

        return this.slot;
    }

    public CoverSlotHandler<T, F> onSlotLoaded(Consumer<F> onSlotLoaded) {
        this.onSlotLoaded = onSlotLoaded;
        return this;
    }

    public CoverSlotHandler<T, F> onSlotRemoved(Consumer<F> onSlotRemoved) {
        this.onSlotRemoved = onSlotRemoved;
        return this;
    }

    public CoverSlotHandler<T, F> onSlotUpdated(Consumer<F> onSlotRemoved) {
        this.onSlotUpdated = onSlotRemoved;
        return this;
    }

    ///////////////////////////////////////
    // ***** FILTER HANDLING ******//

    /// ////////////////////////////////////
    private CustomItemStackHandler getCoverSlot() {
        if (this.coverSlot == null) {
            this.coverSlot = new CustomItemStackHandler(this.slotItem) {

                @Override
                public int getSlotLimit(int slot) {
                    return 1;
                }
            };

            this.coverSlot.setFilter(this::canInsertSlotItem);
        }

        return this.coverSlot;
    }


    public Widget createFilterConfigUI(int xPos, int yPos, int width, int height) {
        this.filterGroup = new WidgetGroup(xPos, yPos, width, height);
        if (!this.slotItem.isEmpty()) {
            this.filterGroup.addWidget(getSlot().openConfigurator(0, 18));
        }
        return this.filterGroup;
    }

    private void updateSlot() {
        var filterContainer = getCoverSlot();

        if (GTCEu.isClientThread()) {
            if (!filterContainer.getStackInSlot(0).isEmpty() && !this.slotItem.isEmpty()) {
                return;
            }
        }

        this.slotItem = filterContainer.getStackInSlot(0);

        if (this.slot != null) {
            this.onSlotRemoved.accept(this.slot);
            this.slot = null;
        }

        loadSlotFromItem();
    }

    private void loadSlotFromItem() {
        if (!this.slotItem.isEmpty()) {
            this.slot = loadSlot(this.slotItem);
            slot.setOnUpdated(this.onSlotUpdated);
            if (container instanceof CoverBehavior cover &&
                    cover.coverHolder instanceof MachineCoverContainer mcc) {
                var machine = MetaMachine.getMachine(mcc.getLevel(), mcc.getPos());
                slot.setMachine(machine);
                slot.setCover((MECover) cover);
            }

            this.onSlotLoaded.accept(this.slot);
        }
        updateSlotGroupUI();
    }

    private void updateSlotGroupUI() {
        if (this.filterGroup == null)
            return;

        this.filterGroup.clearAllWidgets();

        if (!this.slotItem.isEmpty() && this.slot != null) {
            this.filterGroup.addWidget(this.slot.openConfigurator(0, 18));
        }
    }

    //////////////////////////////////////
    // ***** LDLib SyncData ******//
    /// ///////////////////////////////////

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CoverSlotHandler.class);

    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        this.container.onChanged();
    }

    @Override
    public void scheduleRenderUpdate() {
        this.container.scheduleRenderUpdate();
    }

}
