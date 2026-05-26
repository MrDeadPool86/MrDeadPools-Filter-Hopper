package de.mrdeadpool.filterhopper;

import de.mrdeadpool.filterhopper.block.entity.FilterHopperBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Filterhopper.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FilterHopperBlockEntity>> FILTER_HOPPER_BE =
            BLOCK_ENTITIES.register("filter_hopper",
                    () -> BlockEntityType.Builder.of(
                            FilterHopperBlockEntity::new,
                            Filterhopper.FILTER_HOPPER.get()
                    ).build(null)
            );
}