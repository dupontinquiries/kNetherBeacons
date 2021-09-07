package net.fabricmc.knb.mixin;

import net.fabricmc.knb.ui.NetherBeaconScreenHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.entity.Entity;

@Mixin(ServerPlayNetworkHandler.class)
abstract class C2SInject {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "onUpdateBeacon(Lnet/minecraft/network/packet/c2s/play/UpdateBeaconC2SPacket;)V", at = @At(value = "TAIL"), cancellable = true)
    private void inject(UpdateBeaconC2SPacket packet, CallbackInfo info) {
        if (this.player.currentScreenHandler instanceof NetherBeaconScreenHandler) {
            ((NetherBeaconScreenHandler)this.player.currentScreenHandler).setEffects(packet.getPrimaryEffectId(), packet.getSecondaryEffectId());
        }
        info.cancel();
    }
}
