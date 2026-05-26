package de.mrdeadpool.filterhopper.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import de.mrdeadpool.filterhopper.Filterhopper;
import de.mrdeadpool.filterhopper.menu.FilterHopperMenu;
import de.mrdeadpool.filterhopper.network.GhostSlotClickPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class FilterHopperScreen extends AbstractContainerScreen<FilterHopperMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Filterhopper.MODID,
                    "textures/gui/filter_hopper.png");

    private static final int GHOST_COUNT = 5;
    private static final int SLOT_SIZE   = 16;
    private static final int GRID_COLOR  = 0x55FFFFFF; // weiß ~33% Alpha
    private static final int EMPTY_TINT  = 0x44000000; // dunkler Schimmer

    public FilterHopperScreen(FilterHopperMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = 176;
        this.imageHeight = 133;
    }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        // Vanilla Hopper Textur als Hintergrund
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // Gitter über alle 5 Ghost-Slots
        for (int i = 0; i < GHOST_COUNT; i++) {
            Slot slot = menu.slots.get(i);
            renderGhostGrid(g, leftPos + slot.x, topPos + slot.y);
        }
    }

    // Schachbrett-Gitter: 2×2 Pixel alternierend
    private void renderGhostGrid(GuiGraphics g, int x, int y) {
        for (int px = 0; px < SLOT_SIZE; px++) {
            for (int py = 0; py < SLOT_SIZE; py++) {
                if (((px / 2) + (py / 2)) % 2 == 0) {
                    g.fill(x + px, y + py, x + px + 1, y + py + 1, GRID_COLOR);
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        renderTooltip(g, mx, my);

        // Ghost-Items rendern + Gitter drüber
        for (int i = 0; i < GHOST_COUNT; i++) {
            Slot slot = menu.slots.get(i);
            int x = leftPos + slot.x;
            int y = topPos  + slot.y;
            ItemStack stack = slot.getItem();

            if (!stack.isEmpty()) {
                g.renderItem(stack, x, y);
                // Dunkel-Overlay → verblasst
                g.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, 0x66000000);
                // Gitter bleibt sichtbar
                renderGhostGrid(g, x, y);
            } else {
                // Leerer Slot: leichter Schimmer
                g.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, EMPTY_TINT);
            }
        }
    }

    // Ghost-Slot Klick abfangen — Packet senden statt Vanilla-Logik
    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseBtn, ClickType type) {
        if (slotId >= 0 && slotId < GHOST_COUNT) {
            ItemStack cursor = this.menu.getCarried();
            if (!cursor.isEmpty()) {
                // Item in Ghost-Slot setzen
                PacketDistributor.sendToServer(
                        new GhostSlotClickPacket(slotId, cursor));
            } else if (mouseBtn == 1) {
                // Rechtsklick + leerer Cursor = Template löschen
                PacketDistributor.sendToServer(
                        new GhostSlotClickPacket(slotId, ItemStack.EMPTY));
            }
            return; // Vanilla-Klick unterdrücken!
        }
        super.slotClicked(slot, slotId, mouseBtn, type);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        g.drawString(font, title, 8, 6, 0x404040, false);
        g.drawString(font, playerInventoryTitle, 8, imageHeight - 94, 0x404040, false);
    }
}