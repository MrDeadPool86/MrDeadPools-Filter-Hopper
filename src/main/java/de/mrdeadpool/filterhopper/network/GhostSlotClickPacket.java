package de.mrdeadpool.filterhopper.network;

import de.mrdeadpool.filterhopper.menu.FilterHopperMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record GhostSlotClickPacket(int slotIndex, ItemStack stack)
        implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("filterhopper", "ghost_slot_click");

    public static final Type<GhostSlotClickPacket> TYPE =
            new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, GhostSlotClickPacket>
            STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,                GhostSlotClickPacket::slotIndex,
            ItemStack.OPTIONAL_STREAM_CODEC,  GhostSlotClickPacket::stack,
            GhostSlotClickPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Server-Handler: Template setzen, Item beim Spieler lassen
    public static void handle(GhostSlotClickPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var player = ctx.player();
            if (player.containerMenu instanceof FilterHopperMenu menu) {
                int idx = packet.slotIndex();
                if (idx >= 0 && idx < 5) {
                    var be = menu.getBlockEntity();

                    // Template setzen
                    ItemStack template = packet.stack().copy();
                    if (!template.isEmpty()) template.setCount(1);
                    be.getFilterItems().setStackInSlot(idx, template);

                    // Alle Filter-Items sammeln
                    java.util.List<net.minecraft.world.item.ItemStack> filterList =
                            new java.util.ArrayList<>();
                    for (int i = 0; i < be.getFilterItems().getSlots(); i++) {
                        filterList.add(be.getFilterItems().getStackInSlot(i).copy());
                    }

                    // Sync-Packet an alle Spieler schicken die die GUI offen haben
                    if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                        var syncPacket = new de.mrdeadpool.filterhopper.network
                                .FilterHopperSyncPacket(be.getBlockPos(), filterList);

                        serverLevel.getServer().getPlayerList().getPlayers()
                                .stream()
                                .filter(p -> p.containerMenu instanceof FilterHopperMenu m
                                        && m.getBlockEntity().getBlockPos()
                                        .equals(be.getBlockPos()))
                                .forEach(p -> net.neoforged.neoforge.network
                                        .PacketDistributor.sendToPlayer(p, syncPacket));
                    }
                }
            }
        });
    }
}