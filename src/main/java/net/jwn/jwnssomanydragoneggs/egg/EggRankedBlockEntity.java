package net.jwn.jwnssomanydragoneggs.egg;

import net.jwn.jwnssomanydragoneggs.data.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class EggRankedBlockEntity extends BlockEntity {
    private int rank = 0;
    private String owner = "unknown";

    public EggRankedBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.EggRankedBlockEntity.get(), pos, blockState);
    }

    // save Item data to BlockEntity
    @Override
    protected void applyImplicitComponents(DataComponentGetter componentGetter) {
        super.applyImplicitComponents(componentGetter);
        Integer rank1 = componentGetter.get(ModDataComponents.RANK);
        if (rank1 != null) this.rank = rank1;
        String owner1 = componentGetter.get(ModDataComponents.OWNER_NAME);
        if (owner1 != null) this.owner = owner1;
    }


    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(ModDataComponents.RANK.get(), this.rank);
        components.set(ModDataComponents.OWNER_NAME.get(), this.owner);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("egg_rank", this.rank);
        output.putString("egg_owner", this.owner);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.rank = input.getIntOr("egg_rank", 0);
        this.owner = input.getStringOr("egg_owner", "unknown");
        input.child("BlockEntityTag").ifPresent(subInput -> {
            this.rank = subInput.getIntOr("egg_rank", this.rank);
            this.owner = subInput.getStringOr("egg_owner", this.owner);
        });
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
}
