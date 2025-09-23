package com.liser.appmachine.api.cover;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.gregtechceu.gtceu.utils.GTMath;
import com.liser.appmachine.api.cover.slot.CoverSlot;
import com.liser.appmachine.api.cover.slot.ExportOnlyAEItemList;
import com.liser.appmachine.api.cover.trait.CoverBehaviorConfigurator;
import com.liser.appmachine.api.cover.trait.MECover;
import com.liser.appmachine.api.item.MEInputBusSlot;
import com.liser.appmachine.api.machine.gui.GuiTextures;
import com.liser.appmachine.api.machine.trait.AESimpleTieredMachine;
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


public class MEInputBusCover extends MECover implements IControllable, IUICover, CoverBehaviorConfigurator {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEInputBusCover.class,
            MECover.MANAGED_FIELD_HOLDER);

    @Persisted
    @Getter
    @Setter
    private boolean workingEnabled = true;
    protected final IO io;
    protected TickableSubscription subscription;
    public final int maxItemTransferRate;

    @Persisted
    @Getter
    protected int transferRate = 1;
    protected CoverSlot coverSlot;

    protected final ConditionalSubscriptionHandler subscriptionHandler;

    public MEInputBusCover(@NotNull CoverDefinition definition, @NotNull ICoverable coverHolder, @NotNull Direction attachedSide, Object... args) {
        super(definition, coverHolder, attachedSide);
        this.io = IO.IN;
        this.maxItemTransferRate = 64;
        this.transferRate = maxItemTransferRate;
        subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isSubscriptionActive);
    }

    @Override
    public IManagedGridNode getMainNode() {
        return super.getMainNode();
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
    public void onLoad() {
        super.onLoad();
        if (coverHolder instanceof MachineCoverContainer) {
            MetaMachine machine = ((MachineCoverContainer) coverHolder).getMachine();

            if (machine instanceof AESimpleTieredMachine) {
                ((AESimpleTieredMachine) machine).onAttached(EnumSet.of(attachedSide));
            }
        }
        subscriptionHandler.initialize(coverHolder.getLevel());
    }

    @Override
    public void onRemoved() {
        this.getItemFilter().onRemove(this.getMainNode());
        if (subscription != null) {
            subscription.unsubscribe();
        }
        // 先执行物品回退，否则会因为节点离线导致物品无法正确的回到ME库存中
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

        // Config slots
//        group.addWidget(new AEItemCoverConfigWidget(3, 20, this.aeItemHandler));
        group.addWidget(getItemFilter().openConfigurator(3, 20));
        return group;
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
            group.addWidget(getItemFilter().openConfigurator(1, 20));
            return group;
        }
    }

}
