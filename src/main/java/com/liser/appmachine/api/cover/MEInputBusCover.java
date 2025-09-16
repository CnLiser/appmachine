package com.liser.appmachine.api.cover;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
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
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.gregtechceu.gtceu.utils.GTMath;
import com.liser.appmachine.api.cover.slot.CoverSlot;
import com.liser.appmachine.api.cover.trait.CoverBehaviorConfigurator;
import com.liser.appmachine.api.cover.trait.MECover;
import com.liser.appmachine.api.item.MEInputBusSlot;
import com.liser.appmachine.api.machine.gui.GuiTextures;
import com.liser.appmachine.api.machine.gui.widget.AEItemCoverConfigWidget;
import com.liser.appmachine.api.machine.slot.ExportOnlyCoverAEItemList;
import com.liser.appmachine.api.machine.trait.AESimpleTieredMachine;
import com.liser.appmachine.api.machine.trait.NotifiableItemCoverStackHandler;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Consumer;


public class MEInputBusCover extends MECover implements IControllable, IUICover, CoverBehaviorConfigurator {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEInputBusCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);

    protected final static int CONFIG_SIZE = 16;

    @Getter
    protected ExportOnlyAEItemList aeItemHandler;
    @Getter
    @Persisted
    private NotifiableItemStackHandler inventory;
    @Persisted
    @Getter
    @Setter
    private boolean workingEnabled = true;
    protected final IO io;
    protected TickableSubscription subscription;
    public final int maxItemTransferRate;
    protected int itemsLeftToTransferLastSecond = 1;
    @Persisted
    @Getter
    protected int transferRate = 1;
    protected CoverSlot coverSlot;

    public MEInputBusCover(@NotNull CoverDefinition definition, @NotNull ICoverable coverHolder, @NotNull Direction attachedSide, Object... args) {
        super(definition, coverHolder, attachedSide);
        this.io = IO.IN;
        this.maxItemTransferRate = 64;
        this.transferRate = maxItemTransferRate;
        if(this.canAttach()) {
            this.inventory = createInventory();
        }
    }

    @Override
    public IManagedGridNode getMainNode() {
        if(this.holder == null) return super.getMainNode();
        return this.holder.getMainNode();
    }


    protected boolean isSubscriptionActive() {
        return isWorkingEnabled() && getMachine().isOnline();
    }

    private boolean shouldSyncME() {
        return getMachine().shouldSyncME();
    }

    private boolean updateMEStatus() {
        return getMachine().updateMEStatus();
    }

    protected void update() {
        if(this.aeItemHandler == null) return;
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

    /// //////////////////////////////

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

    protected NotifiableItemStackHandler createInventory(Object... args) {
        this.aeItemHandler = new ExportOnlyAEItemList((MetaMachine) this.getMachine(), CONFIG_SIZE);
        return this.aeItemHandler;
    }

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (coverHolder instanceof MachineCoverContainer) {
            MetaMachine machine = ((MachineCoverContainer) coverHolder).getMachine();

            if (machine instanceof AESimpleTieredMachine) {
                ((AESimpleTieredMachine) machine).onAttached(EnumSet.of(attachedSide));
            }
        }
        subscription = coverHolder.subscribeServerTick(subscription, this::update);
    }

    @Override
    public void onRemoved() {
        IGrid grid = this.getMachine().getMainNode().getGrid();
        if(grid != null) {
            for (ExportOnlyAEItemSlot aeSlot : this.aeItemHandler.getInventory()) {
                // return item to AE network
                GenericStack stock = aeSlot.getStock();
                if (stock != null) {
                    long total = stock.amount();
                    long inserted = grid.getStorageService().getInventory().insert(stock.what(), stock.amount(), Actionable.MODULATE,
                            this.actionSource);
                    if (inserted > 0) {
                        aeSlot.extractItem(0, GTMath.saturatedCast(inserted), false);
                    } else {
                        // 如果节点离线，则将物品扔到主世界中
                        ItemStack sourceStack = aeSlot.extractItem(0, GTMath.saturatedCast(total), true);
                        Level level = this.coverHolder.getLevel();
                        BlockPos pos = this.coverHolder.getPos();
                        ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), sourceStack);
                        itemEntity.setDeltaMovement(0, 0.1, 0);
                        itemEntity.setPickUpDelay(10);
                        level.addFreshEntity(itemEntity);
                        aeSlot.extractItem(0, GTMath.saturatedCast(total), false);
                    }
                }
            }
        }

        if (subscription != null) {
            subscription.unsubscribe();
        }
        // 先执行物品回退，否则会因为节点离线导致物品无法正确的回到ME库存中
        super.onRemoved();
    }

    public CoverSlot getItemFilter() {
        if (coverSlot == null) {
                coverSlot = CoverSlot.loadSlot(attachItem);
            if (coverHolder instanceof MachineCoverContainer mcc) {
                var machine = MetaMachine.getMachine(mcc.getLevel(), mcc.getPos());
                if (machine != null) {
                    coverSlot.setMachine(machine);
                    coverSlot.setCover(this);
                    this.holder = (AESimpleTieredMachine) machine;
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

        // Config slots
//        group.addWidget(new AEItemCoverConfigWidget(3, 20, this.aeItemHandler));
        group.addWidget(getItemFilter().openConfigurator(62, 25));
        return group;
    }

    private AESimpleTieredMachine getMachine() {
        if (this.holder != null) {
            return this.holder;
        }
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
            if (stock != null) {
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
    public @Nullable IFancyConfigurator getRightConfigurator() {
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
//            group.addWidget(new AEItemCoverConfigWidget(3, 20, aeItemHandler));
            group.addWidget(getItemFilter().openConfigurator(62, 25));
            return group;
        }
    }

}
