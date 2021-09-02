package net.fabricmc.knb.effects;

import net.fabricmc.knb.KNB;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class EffectsKNB {

    public static InstantStatusEffect reactiveEffect;
    //public static final StatusEffectType reactive; // = new NetherBeaconBlock(FabricBlockSettings.copyOf(Blocks.BEACON));

    static {
        reactiveEffect = Registry.register(Registry.STATUS_EFFECT, new Identifier(KNB.modName, "reactive_effect"), new InstantStatusEffect(StatusEffectType.HARMFUL, 0) {
            @Override
            public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
                if (!target.isSpectator()) {
                    target.world.createExplosion(null, target.getPos().x, target.getPos().y, target.getPos().z, 7f, Explosion.DestructionType.DESTROY);
                }
            }
        });
    }

}
