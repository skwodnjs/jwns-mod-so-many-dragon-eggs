package net.jwn.jwnmanyegg.egg;

import com.google.gson.JsonElement;
import net.jwn.jwnmanyegg.data.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

public class EggRankedBlockEntity extends BlockEntity {
    private int rank = 0;
    private String owner = "unknown";
    private UUID nameTagUUID;

    public EggRankedBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.EggRankedBlockEntity.get(), pos, blockState);
    }

    // save Item data to BlockEntity
    @Override
    protected void applyImplicitComponents(DataComponentGetter componentGetter) {
        super.applyImplicitComponents(componentGetter);
        Integer rank1 = componentGetter.get(ModDataComponents.RANK);
        if (rank1 != null) this.rank = rank1;
        String owner1 = componentGetter.get(ModDataComponents.OWNER);
        if (owner1 != null) this.owner = owner1;
    }


    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(ModDataComponents.RANK.get(), this.rank);
        components.set(ModDataComponents.OWNER.get(), this.owner);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("egg_rank", this.rank);
        output.putString("egg_owner", this.owner);
        if (this.nameTagUUID != null) {
            output.store("NameTagUUID", UUIDUtil.CODEC, this.nameTagUUID);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.rank = input.getIntOr("egg_rank", 0);
        this.owner = input.getStringOr("egg_owner", "unknown");
        this.nameTagUUID = input.read("NameTagUUID", UUIDUtil.CODEC).orElse(null);
    }

    public void setRank(int rank) {
        this.rank = rank;
        this.setChanged();
    }

    public void setOwner(String owner) {
        this.owner = owner;
        this.setChanged();
    }

    public int getRank() {
        return rank;
    }

    public String getOwner() {
        return owner;
    }

    private Display.TextDisplay nameTag;

    public void updateNameTag() {
        if (this.nameTag != null) {
            this.nameTag.discard();
        }

        if (this.level instanceof ServerLevel serverLevel) {
            this.nameTag = EntityType.TEXT_DISPLAY.spawn(
                    serverLevel,
                    (display) -> {
                        Component text = Component.literal("#" + this.rank).withStyle(s -> s.withColor(getRankColor(rank)))
                                .append(" ")
                                .append(Component.literal(this.owner).withStyle(ChatFormatting.WHITE));
                        display.setCustomName(text);

//                        JsonElement jsonElement = ComponentSerialization.CODEC.encodeStart(
//                                serverLevel.registryAccess().createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE),
//                                text
//                        ).getOrThrow();
//                        String jsonString = jsonElement.toString();

                        CompoundTag tag = new CompoundTag();
                        tag.putString("text", text.getString());
                        tag.putString("billboard", "center");
                        tag.putInt("background", 0);

                        ProblemReporter reporter = ProblemReporter.DISCARDING;
                        ValueInput input = TagValueInput.create(reporter, serverLevel.registryAccess(), tag);

                        display.load(input);
                        display.move(MoverType.SELF, this.worldPosition.above().getBottomCenter().add(0, 0.2, 0));
                    },
                    this.worldPosition.above(), // 위치
                    EntitySpawnReason.LOAD,     // 스폰 이유
                    true,
                    false
            );
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null && this.level instanceof ServerLevel serverLevel) {
            if (this.nameTagUUID != null) {
                this.nameTag = (Display.TextDisplay) serverLevel.getEntity(this.nameTagUUID);
            }
            this.updateNameTag();
        }
    }

    @Override
    public void setRemoved() {
        if (this.nameTag != null) {
            this.nameTag.discard();
            this.nameTag = null;
        }
        super.setRemoved();
    }

    public static int getRankColor(int rank) {
        if (rank == 1) return 0xFFD700;
        else if (rank <= 3) return 0xC0C0C0;
        else if (rank <= 10) return 0x805A3B;
        else return 0x444444;
    }
}
