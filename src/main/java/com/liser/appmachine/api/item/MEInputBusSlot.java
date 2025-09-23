package com.liser.appmachine.api.item;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.gregtechceu.gtceu.utils.GTMath;
import com.liser.appmachine.api.cover.slot.CoverSlot;
import com.liser.appmachine.api.cover.slot.ExportOnlyAEItemList;
import com.liser.appmachine.api.cover.trait.MECover;
import com.liser.appmachine.api.gui.widget.AEItemConfigWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;


@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEInputBusSlot implements CoverSlot {

    @Getter
    protected ExportOnlyAEItemList aeItemHandler;

    protected final static int CONFIG_SIZE = 16;

    protected Consumer<CoverSlot> itemWriter = coverSlot -> {
    };
    protected Consumer<CoverSlot> onUpdated = coverSlot -> itemWriter.accept(coverSlot);

    private MetaMachine holder;
    @Persisted
    @Getter
    private MECover cover;

    @Getter
    protected int maxStackSize;

    @Getter
    protected int transferRate = 1;
    protected int itemsLeftToTransferLastSecond = 1;


    protected MEInputBusSlot() {
        maxStackSize = 1;
    }

    public static MEInputBusSlot loadCoverSlot(ItemStack itemStack) {
        return loadCoverSlot(itemStack, coverSlot -> itemStack.setTag(coverSlot.saveSlot()));
    }

    public static MEInputBusSlot loadCoverSlot(ItemStack itemStack, Consumer<CoverSlot> itemWriter) {
        CompoundTag tag = itemStack.getOrCreateTag();
        var handler = new MEInputBusSlot();
        handler.aeItemHandler = new ExportOnlyAEItemList(CONFIG_SIZE);
        handler.itemWriter = itemWriter;
        var list = tag.getList("itemHandler", Tag.TAG_COMPOUND);
        ExportOnlyAEItemSlot[] inventory = handler.aeItemHandler.getInventory();
        for (int i = 0; i < list.size(); i++) {
            inventory[i].deserializeNBT((CompoundTag) list.get(i));
        }
        return handler;
    }

    @Override
    public void setOnUpdated(Consumer<CoverSlot> onUpdated) {
        this.onUpdated = coverSlot -> {
            this.itemWriter.accept(coverSlot);
            onUpdated.accept(coverSlot);
        };
    }

    protected ConditionalSubscriptionHandler subscriptionHandler = null;

    @Override
    public void onLoad() {
        if (subscriptionHandler == null) {
            subscriptionHandler = new ConditionalSubscriptionHandler(getCoverHolder(), this::update, this::isSubscriptionActive);
        }
        subscriptionHandler.initialize(getCoverHolder().getLevel());
    }

    @Override
    public void onRemove(IManagedGridNode mainNode) {
        if (subscriptionHandler != null) {
            subscriptionHandler.unsubscribe();
        }
        // 作为插槽时，无法拿到机器的AE节点
        IGrid grid = mainNode.getGrid();
        for (ExportOnlyAEItemSlot aeSlot : this.aeItemHandler.getInventory()) {
            // return item to AE network
            GenericStack stock = aeSlot.getStock();
            if (stock != null) {
                if (grid == null) {
                    outputItems(aeSlot);
                }else {
                    long inserted = grid.getStorageService().getInventory().insert(stock.what(), stock.amount(), Actionable.MODULATE,
                            this.cover.getActionSource());
                    if (inserted > 0) {
                        aeSlot.extractItem(0, GTMath.saturatedCast(inserted), false);
                    } else {
                        outputItems(aeSlot);
                    }
                }
            }
        }
    }

    /**
     * 将物品扔到主世界中
     * @param aeSlot
     */
    protected void outputItems(ExportOnlyAEItemSlot aeSlot) {
        GenericStack stock = aeSlot.getStock();
        // 已经在之前的循环中判断null了，所以不用再次进行非空判断
        ItemStack sourceStack = aeSlot.extractItem(0, GTMath.saturatedCast(stock.amount()), true);
        Level level = this.getCoverHolder().getLevel();
        BlockPos pos = this.getCoverHolder().getPos();
        ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), sourceStack);
        itemEntity.setDeltaMovement(0, 0.1, 0);
        itemEntity.setPickUpDelay(10);
        level.addFreshEntity(itemEntity);
        aeSlot.extractItem(0, GTMath.saturatedCast(stock.amount()), false);
    }

    protected boolean isSubscriptionActive() {
        return this.getCover().isWorkingEnabled();
    }

    protected ICoverable getCoverHolder() {
        return this.cover.coverHolder;
    }

    @Override
    public boolean isBlank() {
        return false;
    }

    @Override
    public void setMachine(MetaMachine machine) {
        this.holder = machine;
    }

    @Override
    public void setCover(MECover cover) {
        if (this.cover == null) {
            this.cover = cover;
            if (!GTCEu.isClientThread()) {
                // only save on server
                this.aeItemHandler.setChangeListener(() -> {
                    onUpdated.accept(this);
                });
                saveSlot();
            }
        }
    }

    @Override
    public CompoundTag saveSlot() {
        if (isBlank()) {
            return null;
        }
        var tag = new CompoundTag();
        ExportOnlyAEItemSlot[] inventory = this.aeItemHandler.getInventory();
        var list = new ListTag();
        for (var match : inventory) {
            list.add(match.serializeNBT());
        }
        tag.put("itemHandler", list);
        return tag;
    }

    public WidgetGroup openConfigurator(int x, int y) {
        WidgetGroup group = new WidgetGroup(0, 0, 146, 98); // 80 55
        AEItemConfigWidget aeItemConfigWidget = new AEItemConfigWidget(x, y, this.aeItemHandler);
        aeItemConfigWidget.setChangeListener(() -> {
            onUpdated.accept(this);
        });
        group.addWidget(aeItemConfigWidget);
        return group;
    }

    @Override
    public boolean test(ItemStack itemStack) {
        return testItemCount(itemStack) > 0;
    }

    @Override
    public int testItemCount(ItemStack itemStack) {
        return 1;
    }

    @Override
    public void update() {
        if (!this.getCover().shouldSyncME()) return;
        this.syncME();

        long timer = this.getCoverHolder().getOffsetTimer();
        if (timer % 5 == 0) {
            var self = this.getCover().getOwnItemHandler();
            int totalTransferred = doTransferItems(self, this.itemsLeftToTransferLastSecond);
            this.itemsLeftToTransferLastSecond -= totalTransferred;
            if (timer % 20 == 0) {
                this.itemsLeftToTransferLastSecond = transferRate;
            }
        }
    }

    protected int doTransferItems(IItemHandler targetInventory, int maxTransferAmount) {
        return moveInventoryItems(targetInventory, maxTransferAmount);
    }

    protected int moveInventoryItems(IItemHandler targetInventory,
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

    protected void syncME() {
        MEStorage networkInv = this.cover.getMainNode().getGrid().getStorageService().getInventory();
        for (ExportOnlyAEItemSlot aeSlot : this.aeItemHandler.getInventory()) {
            // Try to clear the wrong item
            GenericStack exceedItem = aeSlot.exceedStack();
            if (exceedItem != null) {
                long total = exceedItem.amount();
                long inserted = networkInv.insert(exceedItem.what(), exceedItem.amount(), Actionable.MODULATE,
                        this.cover.getActionSource());
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
                        this.cover.getActionSource());
                if (extracted != 0) {
                    aeSlot.addStack(new GenericStack(reqItem.what(), extracted));
                }
            }
        }
    }

}
