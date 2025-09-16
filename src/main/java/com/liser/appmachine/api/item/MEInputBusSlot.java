package com.liser.appmachine.api.item;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEItemConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.liser.appmachine.api.cover.MEInputBusCover;
import com.liser.appmachine.api.cover.slot.CoverSlot;
import com.liser.appmachine.api.cover.trait.MECover;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEInputBusSlot implements CoverSlot {

    @Getter
    protected ExportOnlyAEItemList aeItemHandler;
    @Getter
    @Persisted
    private NotifiableItemStackHandler inventory;

    protected Consumer<CoverSlot> itemWriter =coverSlot -> {};
    protected Consumer<CoverSlot> onUpdated = coverSlot -> itemWriter.accept(coverSlot);

    private MetaMachine holder;
    private MECover cover;

    @Getter
    protected int maxStackSize;

    protected final static int CONFIG_SIZE = 16;

    protected MEInputBusSlot() {
        maxStackSize = 1;
    }

    public static MEInputBusSlot loadCoverSlot(ItemStack itemStack) {
        return loadCoverSlot(itemStack.getOrCreateTag(), coverSlot -> itemStack.setTag(coverSlot.saveSlot()));
    }

    public static MEInputBusSlot loadCoverSlot(CompoundTag tag, Consumer<CoverSlot> itemWriter) {
        var handler = new MEInputBusSlot();
        handler.itemWriter = itemWriter;
        return handler;
    }

    @Override
    public void setOnUpdated(Consumer<CoverSlot> onUpdated) {
        this.onUpdated =coverSlot -> {
            this.itemWriter.accept(coverSlot);
            onUpdated.accept(coverSlot);
        };
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
        this.cover = cover;
        this.inventory = createInventory();
    }

    @Override
    public CompoundTag saveSlot() {
        if (isBlank()) {
            return null;
        }
        var tag = new CompoundTag();
        return tag;
    }

    public WidgetGroup openConfigurator(int x, int y) {
//        WidgetGroup group = new WidgetGroup(x, y, 18 * 3 + 25, 18 * 3); // 80 55
        WidgetGroup group = new WidgetGroup(new Position(0, 0));
        // Config slots
        group.addWidget(new AEItemConfigWidget(3, 20, aeItemHandler));

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

    protected NotifiableItemStackHandler createInventory(Object... args) {
        if(aeItemHandler == null) {
            if(this.cover instanceof MEInputBusCover mcc) {
                this.aeItemHandler = mcc.getAeItemHandler();
                return this.aeItemHandler;
            }
            this.aeItemHandler = new ExportOnlyAEItemList(this.holder, CONFIG_SIZE);
        }
        return this.aeItemHandler;
    }

}
