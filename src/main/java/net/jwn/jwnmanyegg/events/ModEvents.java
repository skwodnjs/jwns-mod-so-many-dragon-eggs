package net.jwn.jwnmanyegg.events;

import net.jwn.jwnmanyegg.JWNsDragonEggMod;
import net.jwn.jwnmanyegg.data.ModDataComponents;
import net.jwn.jwnmanyegg.egg.EggRankedBlockEntity;
import net.jwn.jwnmanyegg.egg.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = JWNsDragonEggMod.MOD_ID)
public class ModEvents {
    @SubscribeEvent
    public static void onTest(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && serverLevel.dimension() == Level.END) {
            BlockPos pos = event.getPos();
            if (pos.getX() == 0 && pos.getZ() == 0) {
                Scoreboard scoreboard = serverLevel.getScoreboard();
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
                if (score.get() == 0) removeExitPortalEgg(serverLevel);
            }
        }
    }

    @SubscribeEvent
    public static void onDragonDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player){// && event.getEntity() instanceof EnderDragon) {
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
            score.add(1);

            ItemStack egg = new ItemStack(ModBlocks.EGG_RANKED_BLOCK.asItem());

            egg.set(ModDataComponents.OWNER.get(), player.getName().getString());
            egg.set(ModDataComponents.RANK.get(), score.get());

            boolean added = player.getInventory().add(egg);

            if (!added) {
                player.drop(egg, false);
            }
        }
    }

    private static void removeExitPortalEgg(ServerLevel level) {
        System.out.println("START");
        int maxY = level.getMaxY(); // 월드의 최상단 Y 좌표
        int minY = level.getMinY(); // 월드의 최하단 Y 좌표

        // x=0, z=0 좌표에서 위에서 아래로 수직 탐색
        for (int y = maxY; y >= minY; y--) {
            BlockPos p = new BlockPos(0, y, 0);

            if (level.getBlockState(p).is(Blocks.DRAGON_EGG)) {
                System.out.println("IFOUNDIT");
                level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onDragonEggTooltip(ItemTooltipEvent event) {
        if (!event.getItemStack().is(ModBlocks.EGG_RANKED_BLOCK.asItem()) && !event.getItemStack().is(Items.DRAGON_EGG)) return;
        ItemStack stack = event.getItemStack();
        String owner = stack.getOrDefault(
                ModDataComponents.OWNER.get(),
                "Unknown"
        );

        int rank = stack.getOrDefault(
                ModDataComponents.RANK.get(),
                0
        );
        if (rank == 1) event.getToolTip().addFirst(Component.literal("#" + rank)
                .withStyle(style -> style.withColor(0xFFD700)));
        else if (rank <= 3) event.getToolTip().addFirst(Component.literal("#" + rank)
                .withStyle(style -> style.withColor(0xC0C0C0)));
        else if (rank <= 10) event.getToolTip().addFirst(Component.literal("#" + rank)
                .withStyle(style -> style.withColor(0xCD7F32)));
        else event.getToolTip().addFirst(Component.literal("#" + rank)
                    .withStyle(style -> style.withColor(0x444444)));
        event.getToolTip().add(Component.translatable("tooltip.jwnmanyegg.dragon_egg.owner").append(": " + owner));
    }

    @SubscribeEvent
    public static void onFallingEggSpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof FallingBlockEntity fallingBlock) {
            if (fallingBlock.getBlockState().is(ModBlocks.EGG_RANKED_BLOCK.get())) {
                Level level = event.getLevel();
                BlockPos pos = fallingBlock.blockPosition();

                if (level.getBlockEntity(pos) instanceof EggRankedBlockEntity eggBe) {
                    CompoundTag tag = new CompoundTag();
                    tag.putInt("rank", eggBe.getRank());
                    tag.putString("owner", eggBe.getOwner());

                    fallingBlock.blockData = tag;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onFallingEggDespawn(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof FallingBlockEntity fallingBlock) {
            if (fallingBlock.getBlockState().is(ModBlocks.EGG_RANKED_BLOCK.get())) {
                BlockPos blockPos = fallingBlock.blockPosition();
                BlockState currentState = event.getLevel().getBlockState(blockPos);
                if (currentState.is(fallingBlock.getBlockState().getBlock())) return;

                CompoundTag nbt = fallingBlock.blockData;
                if (nbt != null && !nbt.isEmpty()) {
                    ItemStack stack = new ItemStack(ModBlocks.EGG_RANKED_BLOCK.get());
                    if (nbt.contains("egg_rank")) nbt.getInt("egg_rank").ifPresent(i -> stack.set(ModDataComponents.RANK.get(), i));
                    if (nbt.contains("egg_owner")) nbt.getString("egg_owner").ifPresent(s -> stack.set(ModDataComponents.OWNER.get(), s));

                    BlockPos pos = event.getEntity().blockPosition();
                    ItemEntity itemEntity = new ItemEntity(event.getLevel(),
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                    itemEntity.setDefaultPickUpDelay();
                    event.getLevel().addFreshEntity(itemEntity);
                }
            }
        }
    }
}
