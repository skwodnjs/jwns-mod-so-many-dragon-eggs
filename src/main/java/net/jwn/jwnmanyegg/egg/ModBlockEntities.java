package net.jwn.jwnmanyegg.egg;

import net.jwn.jwnmanyegg.JWNsDragonEggMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, JWNsDragonEggMod.MOD_ID);

    public static final Supplier<BlockEntityType<EggRankedBlockEntity>> EggRankedBlockEntity =
            BLOCK_ENTITIES.register("dragon_egg_ranked_be", () -> new BlockEntityType<>(
                    EggRankedBlockEntity::new, ModBlocks.EGG_RANKED_BLOCK.get()));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
