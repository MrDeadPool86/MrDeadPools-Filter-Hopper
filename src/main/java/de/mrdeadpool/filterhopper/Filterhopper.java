package de.mrdeadpool.filterhopper;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredRegister.Blocks;
import net.neoforged.neoforge.registries.DeferredRegister.Items;
import org.slf4j.Logger;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import de.mrdeadpool.filterhopper.block.FilterHopperBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import de.mrdeadpool.filterhopper.item.FilterItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;
import de.mrdeadpool.filterhopper.ModBlockEntities;

@Mod(Filterhopper.MODID)
public class Filterhopper {

    public static final String MODID = "filterhopper";

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Registries
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(MODID);

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MODID);

    public static final DeferredBlock<Block> FILTER_HOPPER =
            BLOCKS.register("filter_hopper",
                    () -> new FilterHopperBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .strength(3.0f)
                                    .requiresCorrectToolForDrops()
                    )
            );



    public static final DeferredItem<BlockItem> FILTER_HOPPER_ITEM =
            ITEMS.register("filter_hopper",
                    () -> new BlockItem(FILTER_HOPPER.get(),
                            new Item.Properties()
                    )
            );

    public static final DeferredItem<FilterItem> FILTER_ITEM =
            ITEMS.register("filter",
                    () -> new FilterItem(new Item.Properties())
            );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FILTER_TAB =
            CREATIVE_MODE_TABS.register("filter_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.literal("Filter Hopper"))
                            .icon(() -> FILTER_HOPPER_ITEM.get().getDefaultInstance())
                            .displayItems((params, output) -> {
                                output.accept(FILTER_HOPPER_ITEM.get());
                                output.accept(FILTER_ITEM.get());
                            })
                            .build()
            );



    public Filterhopper(IEventBus modEventBus, ModContainer modContainer) {

        // Register registries
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModPackets.register(modEventBus);
    }
}