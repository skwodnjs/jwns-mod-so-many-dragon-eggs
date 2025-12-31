package net.jwn.jwnmanyegg.egg;

import net.jwn.jwnmanyegg.JWNsDragonEggMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(JWNsDragonEggMod.MOD_ID);

    public static final DeferredBlock<Block> EGG_RANKED_BLOCK = registerBlock("dragon_egg_ranked", EggRankedBlock::new);

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> function) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, function);
        ModItems.ITEMS.registerItem(
                name,
                (properties) -> new BlockItem(toReturn.get(),
                        properties.useBlockDescriptionPrefix().rarity(Rarity.EPIC))
        );
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
