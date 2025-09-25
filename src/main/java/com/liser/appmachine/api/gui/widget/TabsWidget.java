package com.liser.appmachine.api.gui.widget;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class TabsWidget extends Widget {

    protected final BiConsumer<Integer, Widget> onTabClick;
    protected List<Widget> subTabs;
    public static Widget emptyPage = new Widget(0, 0, 0, 0);
    @Setter
    protected IGuiTexture tabTexture = new ResourceTexture("gtceu:textures/gui/tab/tabs_top.png").getSubTexture(1 / 3f,
            0, 1 / 3f, 0.5f);
    @Setter
    protected IGuiTexture tabHoverTexture = new ResourceTexture("gtceu:textures/gui/tab/tabs_top.png")
            .getSubTexture(1 / 3f, 0.5f, 1 / 3f, 0.5f);
    @Setter
    protected IGuiTexture tabPressedTexture = tabHoverTexture;
    @Getter
    protected int offset = 0;
    /**
     * (old tab, new tab)
     */
    @Setter
    @Nullable
    protected BiConsumer<Widget, Widget> onTabSwitch;

    @Nullable
    protected Widget selectedTab;

    public TabsWidget(BiConsumer<Integer, Widget> onTabClick) {
        this(onTabClick, 0, 0, -20, 200, 24);
    }

    public TabsWidget(BiConsumer<Integer, Widget> onTabClick, int size, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.subTabs = new ArrayList<>(Collections.nCopies(size, emptyPage));
        this.onTabClick = onTabClick;
    }

    public void clearSubTabs() {
        this.subTabs = new ArrayList<>(Collections.nCopies(subTabs.size(), emptyPage));
    }

    public void attachSubTab(int index, Widget subTab) {
        subTabs.set(index, subTab);
    }

    public void attachRemoveTab(int index) {
        subTabs.set(index, emptyPage);
        int i = index;
        int j = index;

        for (int k = 0; k < subTabs.size(); k++) {
            if (i < subTabs.size()) {
                if (!isEmpty(i)) {
                    attachSelectTab(i);
                    break;
                }
            }

            if (j > -1) {
                if (!isEmpty(j)) {
                    attachSelectTab(j);
                    break;
                }
            }

            if (j < 0 && i >= subTabs.size()) {
                break;
            }

            i++;
            j--;
        }
    }

    public void attachSelectTab(int index) {
        Widget newTab = subTabs.get(index);
        if (newTab == selectedTab) {
            return;
        }
        this.selectedTab = newTab;
        onTabClick.accept(index, selectedTab);
    }

    public Widget attachGetTab(int index) {
        return subTabs.get(index);
    }

    public boolean isEmpty(int index) {
        return subTabs.get(index) == emptyPage;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (subTabs.isEmpty()) {
            selectedTab = emptyPage;
            return;
        }
        if (id == 0) {
            var index = buffer.readVarInt();
            var old = selectedTab;
            if (index < 0) {
                selectedTab = subTabs.get(0);
            } else if (index < subTabs.size()) {
                selectedTab = subTabs.get(index);
            } else {
                return;
            }
            if (onTabSwitch != null) {
                onTabSwitch.accept(old, selectedTab);
            }
            onTabClick.accept(index, selectedTab);
        }
    }

    public int getSubTabsWidth() {
        return getSize().width - 8 - 24 - 4 - 16 - 8 - 16;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            var hoveredTab = getHoveredTab(mouseX, mouseY);
            // click tab
            if (hoveredTab != null && hoveredTab != selectedTab && hoveredTab != emptyPage) {
                if (onTabSwitch != null) {
                    onTabSwitch.accept(selectedTab, hoveredTab);
                }
                selectedTab = hoveredTab;
                writeClientAction(0,
                        buf -> buf.writeVarInt(subTabs.indexOf(selectedTab)));
                onTabClick.accept(subTabs.indexOf(selectedTab), selectedTab);
                playButtonClickSound();
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        var sx = getPosition().x + 8 + 18 + 4 + 16;
        if (isMouseOver(sx, getPosition().y, getSubTabsWidth(), 10, mouseX, mouseY)) {
            offset = Mth.clamp(offset + 5 * (wheelDelta > 0 ? -1 : 1), 0, subTabs.size() * 24 - getSubTabsWidth());
        }
        return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        var position = getPosition();
        var size = getSize();
        var hoveredTab = getHoveredTab(mouseX, mouseY);
        // render sub tabs
        for (int i = 0; i < subTabs.size(); i++) {
            if (subTabs.get(i) == emptyPage) {
                continue;
            }
            drawTab(subTabs.get(i), graphics, mouseX, mouseY, position.x + 18 * i, position.y, 18, 10, hoveredTab);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        var hoveredTab = getHoveredTab(mouseX, mouseY);
        if (hoveredTab != null && gui != null && gui.getModularUIGui() != null) {
//            gui.getModularUIGui().setHoverTooltip(hoveredTab.getTabTooltips(), ItemStack.EMPTY, null,
//                    hoveredTab.getTabTooltipComponent());
        }
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public Widget getHoveredTab(double mouseX, double mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            var position = getPosition();
            var size = getSize();
            // others
            int i = ((int) mouseX - position.x) / 18;
            if (i < subTabs.size()) {
                return subTabs.get(i);
            }
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public void drawTab(Widget tab, @NotNull GuiGraphics graphics, int mouseX, int mouseY, int x, int y,
                        int width, int height, Widget hoveredTab) {
        // render background
        if (tab == selectedTab) {
            tabPressedTexture.draw(graphics, mouseX, mouseY, x, y, width, height);
        } else if (tab == hoveredTab) {
            tabHoverTexture.draw(graphics, mouseX, mouseY, x, y, width, height);
        } else {
            tabTexture.draw(graphics, mouseX, mouseY, x, y, width, height);
        }
        // render icon
//        tab.getTabIcon().draw(graphics, mouseX, mouseY, x + (width - 16) / 2f, y + (height - 16) / 2f, 16, 16);
    }

    public void selectTab(Widget selectedTab) {
        this.selectedTab = selectedTab;
        this.detectAndSendChanges();
    }


}
