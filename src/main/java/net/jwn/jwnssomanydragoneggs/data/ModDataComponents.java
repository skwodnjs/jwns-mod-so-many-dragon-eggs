package net.jwn.jwnssomanydragoneggs.data;

import com.mojang.serialization.Codec;
import net.jwn.jwnssomanydragoneggs.JWNsDragonEggMod;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, JWNsDragonEggMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> OWNER_NAME =
            DATA_COMPONENTS.register("owner_name",
                    () -> DataComponentType.<String>builder()
                            .persistent(Codec.STRING)
                            .build()
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RANK =
            DATA_COMPONENTS.register("rank",
                    () -> DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .build()
            );
}
