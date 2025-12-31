package net.jwn.jwnssomanydragoneggs.egg;

import net.jwn.jwnssomanydragoneggs.JWNsDragonEggMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JWNsDragonEggMod.MOD_ID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
