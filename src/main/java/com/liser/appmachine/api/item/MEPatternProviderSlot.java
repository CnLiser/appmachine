package com.liser.appmachine.api.item;

import appeng.api.networking.IManagedGridNode;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.liser.appmachine.api.cover.slot.CoverSlot;
import com.liser.appmachine.api.cover.trait.MECover;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class MEPatternProviderSlot implements CoverSlot {

    protected final static int CONFIG_SIZE = 18;

    protected Consumer<CoverSlot> itemWriter = coverSlot -> {
    };
    protected Consumer<CoverSlot> onUpdated = coverSlot -> itemWriter.accept(coverSlot);

    private MetaMachine holder;
    @Getter
    private MECover cover;

    protected MEPatternProviderSlot() {

    }

    public static MEPatternProviderSlot loadCoverSlot(ItemStack itemStack) {
        return loadCoverSlot(itemStack, coverSlot -> itemStack.setTag(coverSlot.saveSlot()));
    }

    public static MEPatternProviderSlot loadCoverSlot(ItemStack itemStack, Consumer<CoverSlot> itemWriter) {
        CompoundTag tag = itemStack.getOrCreateTag();
        var handler = new MEPatternProviderSlot();

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
    public void onRemove(IManagedGridNode mainNode, int type) {
        if (subscriptionHandler != null) {
            subscriptionHandler.unsubscribe();
        }
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
        var list = new ListTag();
        return tag;
    }

    public WidgetGroup openConfigurator(int x, int y) {
        WidgetGroup group = new WidgetGroup(0, 0, 146, 98); // 80 55
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

    }




}
