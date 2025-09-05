package com.liser.appmachine.api.cover;

import appeng.api.config.Actionable;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.gregtechceu.gtceu.utils.GTMath;
import com.liser.appmachine.api.machine.gui.GuiTextures;
import com.liser.appmachine.api.machine.gui.widget.AEItemCoverConfigWidget;
import com.liser.appmachine.api.machine.slot.ExportOnlyCoverAEItemList;
import com.liser.appmachine.api.machine.trait.AESimpleTieredMachine;
import com.liser.appmachine.api.machine.trait.NotifiableItemCoverStackHandler;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;


public class MEInputBusCover extends CoverBehavior implements IControllable, IUICover {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEInputBusCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    protected final static int CONFIG_SIZE = 16;

    protected ExportOnlyCoverAEItemList aeItemHandler;
    @Getter
    @Persisted
    private final NotifiableItemCoverStackHandler inventory;
    @Persisted
    @Getter
    @Setter
    private boolean workingEnabled = true;
    @DescSynced
    @Getter
    @Setter
    private boolean isOnline = false;
    @Getter
    @Setter
    protected int tier = 9;
    protected final IO io;
    protected TickableSubscription subscription;
    protected final IActionSource actionSource;
    public final int maxItemTransferRate;
    protected int itemsLeftToTransferLastSecond = 1;
    @Persisted
    @Getter
    protected int transferRate = 1;

    public MEInputBusCover(@NotNull CoverDefinition definition, @NotNull ICoverable coverHolder, @NotNull Direction attachedSide, Object... args) {
        super(definition, coverHolder, attachedSide);
        this.io = IO.IN;
        this.inventory = createInventory();
        this.maxItemTransferRate = 64;
        this.transferRate = maxItemTransferRate;
        if(getMainNode() != null) {
            this.actionSource = IActionSource.ofMachine(getMainNode()::getNode);
        }else {
            this.actionSource = null;
        }
    }

    protected boolean isSubscriptionActive() {
        return isWorkingEnabled() && getMachine().isOnline();
    }

    public IManagedGridNode getMainNode() {
        if(!(coverHolder instanceof MachineCoverContainer machineCoverContainer)) return null;
        MetaMachine machine = machineCoverContainer.getMachine();
        if(!(machine instanceof IGridConnectedMachine)) return null;
        return ((IGridConnectedMachine) machine).getMainNode();
    }

    private boolean shouldSyncME() {
        return getMachine().shouldSyncME();
    }

    private boolean updateMEStatus() {
        return getMachine().updateMEStatus();
    }

    protected void update() {
        if (!this.isWorkingEnabled()) return;
        if (!this.shouldSyncME()) return;

        if (this.updateMEStatus()) {
            this.syncME();
        }

        long timer = this.coverHolder.getOffsetTimer();
        if (timer % 5 == 0) {
            var self = getOwnItemHandler();
            int totalTransferred = doTransferItems(this.aeItemHandler, self, this.itemsLeftToTransferLastSecond);
            this.itemsLeftToTransferLastSecond -= totalTransferred;
            if (timer % 20 == 0) {
                this.itemsLeftToTransferLastSecond = transferRate;
            }
        }
    }

    protected @Nullable IItemHandlerModifiable getOwnItemHandler() {
        return coverHolder.getItemHandlerCap(attachedSide, false);
    }

    /////////////////////////////////
    // ********** Sync ME *********//
    /////////////////////////////////

    protected void syncME() {
        MEStorage networkInv = this.getMachine().getMainNode().getGrid().getStorageService().getInventory();
        for (ExportOnlyAEItemSlot aeSlot : this.aeItemHandler.getInventory()) {
            // Try to clear the wrong item
            GenericStack exceedItem = aeSlot.exceedStack();
            if (exceedItem != null) {
                long total = exceedItem.amount();
                long inserted = networkInv.insert(exceedItem.what(), exceedItem.amount(), Actionable.MODULATE,
                        this.actionSource);
                if (inserted > 0) {
                    aeSlot.extractItem(0, GTMath.saturatedCast(inserted), false);
                    continue;
                } else {
                    aeSlot.extractItem(0, GTMath.saturatedCast(total), false);
                }
            }
            // Fill it
            GenericStack reqItem = aeSlot.requestStack();
            if (reqItem != null) {
                long extracted = networkInv.extract(reqItem.what(), reqItem.amount(), Actionable.MODULATE,
                        this.actionSource);
                if (extracted != 0) {
                    aeSlot.addStack(new GenericStack(reqItem.what(), extracted));
                }
            }
        }
    }

    protected NotifiableItemCoverStackHandler createInventory(Object... args) {
        this.aeItemHandler = new ExportOnlyCoverAEItemList(coverDefinition, coverHolder, attachedSide, CONFIG_SIZE);
        return this.aeItemHandler;
    }

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean canAttach() {
        if (!(coverHolder instanceof MachineCoverContainer)) return false;

        MetaMachine machine = ((MachineCoverContainer) coverHolder).getMachine();

        if(!(machine instanceof AESimpleTieredMachine)) return false;

        for (var dir : Direction.values()) {
            if (coverHolder.hasCover(dir) && coverHolder.getCoverAtSide(dir) instanceof MEInputBusCover) {
                return false;
            }
        }
        return super.canAttach();
    }

    @Override
    public void onAttached(@NotNull ItemStack itemStack, @NotNull ServerPlayer player) {
        super.onAttached(itemStack, player);
        if(coverHolder instanceof MachineCoverContainer) {
            MetaMachine machine = ((MachineCoverContainer) coverHolder).getMachine();

            if(machine instanceof AESimpleTieredMachine) {
                ((AESimpleTieredMachine) machine).onAttached(EnumSet.of(attachedSide));
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if(coverHolder instanceof MachineCoverContainer) {
            MetaMachine machine = ((MachineCoverContainer) coverHolder).getMachine();

            if(machine instanceof AESimpleTieredMachine) {
                ((AESimpleTieredMachine) machine).onAttached(EnumSet.of(attachedSide));
            }
        }
        subscription = coverHolder.subscribeServerTick(subscription, this::update);
    }

    @Override
    public void onRemoved() {
        MEStorage networkInv = this.getMachine().getMainNode().getGrid().getStorageService().getInventory();
        for (ExportOnlyAEItemSlot aeSlot : this.aeItemHandler.getInventory()) {
            // return item to AE network
            GenericStack stock = aeSlot.getStock();
            if(stock != null) {
                long total = stock.amount();
                long inserted = networkInv.insert(stock.what(), stock.amount(), Actionable.MODULATE,
                        this.actionSource);
                if (inserted > 0) {
                    aeSlot.extractItem(0, GTMath.saturatedCast(inserted), false);
                } else {
                    aeSlot.extractItem(0, GTMath.saturatedCast(total), false);
                }
            }
        }
        super.onRemoved();
        getMachine().onCoverRemove();
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }



    @Override
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // ME Network status
        group.addWidget(new LabelWidget(3, 5, () -> getMachine().isOnline() ?
                "gtceu.gui.me_network.online" :
                "gtceu.gui.me_network.offline"));

        // Config slots
        group.addWidget(new AEItemCoverConfigWidget(3, 20, this.aeItemHandler));

        return group;
    }

    private AESimpleTieredMachine getMachine() {
        return (AESimpleTieredMachine) ((MachineCoverContainer) coverHolder).getMachine();
    }

    @Override
    public void markAsDirty() {

    }

    protected int doTransferItems(IItemHandler sourceInventory, IItemHandler targetInventory, int maxTransferAmount) {
        return moveInventoryItems(sourceInventory, targetInventory, maxTransferAmount);
    }

    protected int moveInventoryItems(IItemHandler sourceInventory, IItemHandler targetInventory,
                                     int maxTransferAmount) {
        int itemsLeftToTransfer = maxTransferAmount;

        for (ExportOnlyAEItemSlot aeSlot : this.aeItemHandler.getInventory()) {

            GenericStack stock = aeSlot.getStock();
            if(stock != null) {
                ItemStack sourceStack = aeSlot.extractItem(0, GTMath.saturatedCast(itemsLeftToTransfer), true);

                if (sourceStack.isEmpty()) {
                    continue;
                }

                ItemStack remainder = ItemHandlerHelper.insertItem(targetInventory, sourceStack, true);
                int amountToInsert = sourceStack.getCount() - remainder.getCount();

                if (amountToInsert > 0) {
                    sourceStack = aeSlot.extractItem(0, amountToInsert, false);
                    if (!sourceStack.isEmpty()) {
                        ItemHandlerHelper.insertItem(targetInventory, sourceStack, false);
                        itemsLeftToTransfer -= sourceStack.getCount();

                        if (itemsLeftToTransfer == 0) {
                            break;
                        }
                    }
                }
            }

        }
        return maxTransferAmount - itemsLeftToTransfer;
    }


    private String getUITitle() {
        return "item.appmachine.me_input_bus_cover";
    }

    @Override
    public @Nullable IFancyConfigurator getConfigurator() {
        return new MEInputBusCoverConfigurator();
    }


    private class MEInputBusCoverConfigurator implements IFancyConfigurator {

        @Override
        public Component getTitle() {
            return Component.translatable("item.appmachine.me_input_bus_cover");
        }

        @Override
        public IGuiTexture getIcon() {
            return GuiTextures.ME_INPUT_BUS_ICON;
        }

        @Override
        public Widget createConfigurator() {
            WidgetGroup group = new WidgetGroup(new Position(0, 0));
            // ME Network status
            group.addWidget(new LabelWidget(3, 5, () -> getMachine().isOnline() ?
                    "gtceu.gui.me_network.online" :
                    "gtceu.gui.me_network.offline"));

            // Config slots
            group.addWidget(new AEItemCoverConfigWidget(3, 20, aeItemHandler));

            return group;
        }
    }

}
