package de.mrdeadpool.filterhopper.network;

import de.mrdeadpool.filterhopper.block.entity.FilterHopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record FilterHopperSyncPacket(BlockPos pos, List<ItemStack> filterItems)
        implements CustomPacketPayload {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("filterhopper", "filter_hopper_sync");

    public static final Type<FilterHopperSyncPacket> TYPE =
            new Type<>(ID);

    public static final StreamCodec<RegistryFriendlyByteBuf, FilterHopperSyncPacket>
            STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            FilterHopperSyncPacket::pos,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()),
            FilterHopperSyncPacket::filterItems,
            FilterHopperSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Client-Handler: Filter-Items im BlockEntity aktualisieren
    public static void handle(FilterHopperSyncPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var level = ctx.player().level();
            if (level.getBlockEntity(packet.pos())
                    instanceof FilterHopperBlockEntity be) {
                for (int i = 0; i < packet.filterItems().size(); i++) {
                    be.getFilterItems().setStackInSlot(i, packet.filterItems().get(i));
                }
            }
        });
    }
}