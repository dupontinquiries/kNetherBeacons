package net.fabricmc.knb.blocks;

import net.fabricmc.knb.KNB;
import net.fabricmc.knb.entity.NetherBeaconEntity;
import net.fabricmc.knb.entity.VillagerBeaconEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class VillagerBeaconBlock extends BlockWithEntity {
    public VillagerBeaconBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(ACTIVE, false));
    }

    public DyeColor getColor() {
        return DyeColor.RED;
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VillagerBeaconEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, KNB.villagerBeaconEntityType, VillagerBeaconEntity::tick);
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) { // k throwing error on server
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof VillagerBeaconEntity) {
                player.incrementStat(Stats.INTERACT_WITH_BEACON);
//                world.getServer().plater
                player.sendMessage(new LiteralText("This beacon seems to be emanating the effects of " + ((VillagerBeaconEntity)blockEntity).getEffect().getTranslationKey()), false);
            }

            return ActionResult.CONSUME;
        }
    }

    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (itemStack.hasCustomName()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof VillagerBeaconEntity) {
                VillagerBeaconEntity nbe = (VillagerBeaconEntity) blockEntity;
                nbe.setCustomName(itemStack.getName());
            }
        }

    }

    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(ACTIVE);
    }
}
