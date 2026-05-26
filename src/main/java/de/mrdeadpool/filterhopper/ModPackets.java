package de.mrdeadpool.filterhopper;

import de.mrdeadpool.filterhopper.network.GhostSlotClickPacket;
import de.mrdeadpool.filterhopper.network.FilterHopperSyncPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModPackets {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModPackets::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Filterhopper.MODID);

        registrar.playToServer(
                GhostSlotClickPacket.TYPE,
                GhostSlotClickPacket.STREAM_CODEC,
                GhostSlotClickPacket::handle);

        registrar.playToClient(
                FilterHopperSyncPacket.TYPE,
                FilterHopperSyncPacket.STREAM_CODEC,
                FilterHopperSyncPacket::handle);
    }
}