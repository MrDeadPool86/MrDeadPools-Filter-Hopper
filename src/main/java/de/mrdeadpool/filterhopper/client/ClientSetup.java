package de.mrdeadpool.filterhopper.client;

import de.mrdeadpool.filterhopper.ModMenuTypes;
import de.mrdeadpool.filterhopper.client.screen.FilterHopperScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(
        modid = de.mrdeadpool.filterhopper.Filterhopper.MODID,
        value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(
                ModMenuTypes.FILTER_HOPPER_MENU.get(),
                FilterHopperScreen::new);
    }
}