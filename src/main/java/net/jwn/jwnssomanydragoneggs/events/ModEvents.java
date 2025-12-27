package net.jwn.jwnssomanydragoneggs.events;

import net.jwn.jwnssomanydragoneggs.JWNsDragonEggMod;
import net.jwn.jwnssomanydragoneggs.data.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = JWNsDragonEggMod.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onDragonDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player && event.getEntity() instanceof EnderDragon) {
            Scoreboard scoreboard = player.level().getScoreboard();
            Objective objective = scoreboard.getObjective("dragon_kills");
            if (objective == null) {
                objective = scoreboard.addObjective(
                        "dragon_kills",
                        ObjectiveCriteria.DUMMY,
                        Component.literal("Dragon Kills"),
                        ObjectiveCriteria.RenderType.INTEGER,
                        false,
                        null
                );
            }

            ScoreHolder total = ScoreHolder.forNameOnly("#total");
            ScoreAccess score = scoreboard.getOrCreatePlayerScore(total, objective);
            if (score.get() == 0) player.level().getServer().execute(() -> removeExitPortalEgg(player.level()));
            score.add(1);

            ItemStack egg = new ItemStack(Items.DRAGON_EGG);

            egg.set(ModDataComponents.OWNER_NAME.get(), player.getName().getString());
            egg.set(ModDataComponents.RANK.get(), score.get());

            boolean added = player.getInventory().add(egg);

            if (!added) {
                player.drop(egg, false);
            }
        }
    }

    private static void removeExitPortalEgg(ServerLevel level) {
        int yTop = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0) - 1;
        BlockPos top = new BlockPos(0, yTop, 0);

        if (level.getBlockState(top).is(Blocks.DRAGON_EGG)) {
            level.setBlockAndUpdate(top, Blocks.AIR.defaultBlockState());
            return;
        }

        for (int dy = -5; dy <= 5; dy++) {
            BlockPos p = top.offset(0, dy, 0);
            if (level.getBlockState(p).is(Blocks.DRAGON_EGG)) {
                level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onDragonEggTooltip(ItemTooltipEvent event) {
        if (!event.getItemStack().is(net.minecraft.world.item.Items.DRAGON_EGG)) return;
        ItemStack stack = event.getItemStack();
        String owner = stack.getOrDefault(
                ModDataComponents.OWNER_NAME.get(),
                "Unknown"
        );

        int rank = stack.getOrDefault(
                ModDataComponents.RANK.get(),
                0
        );
        event.getToolTip().addFirst(Component.literal("#" + rank));
        event.getToolTip().add(Component.translatable("tooltip.jwnssomanydragoneegss.dragon_egg.owner").append(": " + owner));
    }
}
