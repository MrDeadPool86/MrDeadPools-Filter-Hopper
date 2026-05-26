package de.mrdeadpool.filterhopper;

import de.mrdeadpool.filterhopper.menu.FilterHopperMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, Filterhopper.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<FilterHopperMenu>> FILTER_HOPPER_MENU =
            MENU_TYPES.register("filter_hopper",
                    () -> IMenuTypeExtension.create(FilterHopperMenu::new));
}