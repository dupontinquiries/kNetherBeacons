package net.fabricmc.example.blocks;

import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/***
 * The nether beacon has different status effects
 * Its menu is a dark bastion stone texture
 * Its beam travels downwards, not upwards
 * It is powered only when it sits at the bottom of a gold tetrahedron
 * It sounds different compared to the regular beacon
 */

public class NetherBeaconBlock extends BlockWithEntity implements Stainable {
    public NetherBeaconBlock(Settings settings) {
        super(settings);
    }

    public DyeColor getColor() {
        return DyeColor.WHITE;
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BeaconBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, BlockEntityType.BEACON, BeaconBlockEntity::tick);
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BeaconBlockEntity) {
                player.openHandledScreen((BeaconBlockEntity)blockEntity);
                player.incrementStat(Stats.INTERACT_WITH_BEACON);
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
            if (blockEntity instanceof BeaconBlockEntity) {
                ((BeaconBlockEntity)blockEntity).setCustomName(itemStack.getName());
            }
        }

    }
}
