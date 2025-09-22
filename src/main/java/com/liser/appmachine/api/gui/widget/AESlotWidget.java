package com.liser.appmachine.api.gui.widget;

import appeng.api.stacks.GenericStack;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.PhantomSlotWidget;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEConfigSlotWidget.drawSelectionOverlay;

public class AESlotWidget extends PhantomSlotWidget {

    protected final static int REMOVE_ID = 1000;
    protected final static int UPDATE_ID = 1001;
    protected final static int AMOUNT_CHANGE_ID = 1002;
    protected final static int PICK_UP_ID = 1003;

    protected IConfigurableSlot[] displayList;

    @Getter
    protected boolean isAutoPull = false;
    @Getter
    protected boolean isStocking = false;
    private final int index;


    public AESlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition);
        this.index = slotIndex;
    }


    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Position position = getPosition();
        drawSlots(graphics, mouseX, mouseY, position.x, position.y, isAutoPull());
        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (mouseOverConfig(mouseX, mouseY)) {
            drawSelectionOverlay(graphics, stackX, stackY, 16, 16);
        } else if (mouseOverStock(mouseX, mouseY)) {
            drawSelectionOverlay(graphics, stackX, stackY + 18, 16, 16);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void drawSlots(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, boolean autoPull) {
        if (autoPull) {
            GuiTextures.SLOT_DARK.draw(graphics, mouseX, mouseY, x, y, 18, 18);
            GuiTextures.CONFIG_ARROW.draw(graphics, mouseX, mouseY, x, y, 18, 18);
        } else {
            GuiTextures.SLOT.draw(graphics, mouseX, mouseY, x, y, 18, 18);
            GuiTextures.CONFIG_ARROW_DARK.draw(graphics, mouseX, mouseY, x, y, 18, 18);
        }
        GuiTextures.SLOT_DARK.draw(graphics, mouseX, mouseY, x, y + 18, 18, 18);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseOverConfig(mouseX, mouseY)) {
            // don't allow manual interaction with config slots when auto pull is enabled
            if (isAutoPull()) {
                return false;
            }

            if (button == 1) {
                // Right click to clear
                writeClientAction(REMOVE_ID, buf -> {});

            } else if (button == 0) {
                // Left click to set/select
                ItemStack item = this.gui.getModularUIContainer().getCarried();

                if (!item.isEmpty()) {
                    writeClientAction(UPDATE_ID, buf -> buf.writeItem(item));
                }
            }
            return true;
        } else if (mouseOverStock(mouseX, mouseY)) {
            // Left click to pick up
            if (button == 0) {
                if (isStocking()) {
                    return false;
                }
//                GenericStack stack = this.parentWidget.getDisplay(this.index).getStock();
//                if (stack != null) {
//                    writeClientAction(PICK_UP_ID, buf -> {});
//                }
                return true;
            }
        }
        return false;
    }

    protected boolean mouseOverConfig(double mouseX, double mouseY) {
        Position position = getPosition();
        return isMouseOver(position.x, position.y, 18, 18, mouseX, mouseY);
    }

    protected boolean mouseOverStock(double mouseX, double mouseY) {
        Position position = getPosition();
        return isMouseOver(position.x, position.y + 18, 18, 18, mouseX, mouseY);
    }
}
