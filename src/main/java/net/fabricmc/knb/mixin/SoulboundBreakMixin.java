package net.fabricmc.knb.mixin;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(PlayerEntity.class)
public class SoulboundBreakMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isBlockBreakingRestricted()V"), method = "isBlockBreakingRestricted", cancellable = true)
    public boolean isBlockBreakingRestricted(World world, BlockPos pos, GameMode gameMode) {
        if (!gameMode.isBlockBreakingRestricted()) {
            return false;
        } else if (gameMode == GameMode.SPECTATOR) {
            return true;
        } else if (this.canModifyBlocks()) {
            return false;
        } else {
            ItemStack itemStack = this.getMainHandStack();
            return itemStack.isEmpty() || !itemStack.canDestroy(world.getTagManager(), new CachedBlockPosition(world, pos, false));
        }
    }
    private void onBreak(final PlayerEntity player) {
        ActionResult result = SheepShearCallback.EVENT.invoker().interact(player, (SheepEntity) (Object) this);

        if(result == ActionResult.FAIL) {
            info.cancel();
        }
    }
}