package net.fabricmc.knb.effects;

import net.fabricmc.knb.KNB;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class EffectsKNB2 {

    public static InstantStatusEffect reactiveEffect;
    public static Identifier reactiveEffectIdentifier = new Identifier(KNB.modName, "reactive_effect");
    //public static final StatusEffectType reactive; // = new NetherBeaconBlock(FabricBlockSettings.copyOf(Blocks.BEACON));

    static {
        int idNumber = -1;
        reactiveEffect = Registry.register(Registry.STATUS_EFFECT, reactiveEffectIdentifier, new InstantStatusEffect(StatusEffectType.HARMFUL, ++idNumber) {
            @Override
            public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
                if (!target.isSpectator()) {
                    target.getEntityWorld().createExplosion(null, target.getPos().x, target.getPos().y, target.getPos().z, 7f * amplifier, Explosion.DestructionType.DESTROY);
                }
            }
        });
    }

    private class RadiatorEffect extends StatusEffect {

        protected RadiatorEffect() {
            super(StatusEffectType.HARMFUL, 0);
        }

        protected RadiatorEffect(StatusEffectType type, int color) {
            super(type, color);
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {

            /*if (entity instanceof PlayerEntity) {
               PlayerEntity player =  ((PlayerEntity) entity);
               boolean hasEnchant = false;
               player.getArmorItems().forEach((itemStack) -> {
                   itemStack.data
                   itemStack.getEnchantments().forEach((nbt) -> {
                       NbtElement e = ((NbtElement) nbt);
                       if (NbtElement.STRING_TYPE)
                   });
               });
            }*/

            if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity && !((PlayerEntity) entity).canTakeDamage())) {
                LivingEntity le = ((LivingEntity) entity);
                //_x, _y, _z = VelocityHelper.getDirectionalVelocities()
                le.addVelocity(0, 0.04, 0);
                le.damage(DamageSource.IN_FIRE, 3f);
                le.setOnFireFor(7);
            }

            /*if (this == StatusEffects.REGENERATION) {
                if (entity.getHealth() < entity.getMaxHealth()) {
                    entity.heal(1.0F);
                }
            } else if (this == StatusEffects.POISON) {
                if (entity.getHealth() > 1.0F) {
                    entity.damage(DamageSource.MAGIC, 1.0F);
                }
            } else if (this == StatusEffects.WITHER) {
                entity.damage(DamageSource.WITHER, 1.0F);
            } else if (this == StatusEffects.HUNGER && entity instanceof PlayerEntity) {
                ((PlayerEntity)entity).addExhaustion(0.005F * (float)(amplifier + 1));
            } else if (this == StatusEffects.SATURATION && entity instanceof PlayerEntity) {
                if (!entity.world.isClient) {
                    ((PlayerEntity)entity).getHungerManager().add(amplifier + 1, 1.0F);
                }
            } else if ((this != StatusEffects.INSTANT_HEALTH || entity.isUndead()) && (this != StatusEffects.INSTANT_DAMAGE || !entity.isUndead())) {
                if (this == StatusEffects.INSTANT_DAMAGE && !entity.isUndead() || this == StatusEffects.INSTANT_HEALTH && entity.isUndead()) {
                    entity.damage(DamageSource.MAGIC, (float)(6 << amplifier));
                }
            } else {
                entity.heal((float)Math.max(4 << amplifier, 0));
            }
             */

        }

        /*
        @Override
        public boolean canApplyUpdateEffect(int duration, int amplifier) {
            int k;
            if (this == StatusEffects.REGENERATION) {
                k = 50 >> amplifier;
                if (k > 0) {
                    return duration % k == 0;
                } else {
                    return true;
                }
            } else if (this == StatusEffects.POISON) {
                k = 25 >> amplifier;
                if (k > 0) {
                    return duration % k == 0;
                } else {
                    return true;
                }
            } else if (this == StatusEffects.WITHER) {
                k = 40 >> amplifier;
                if (k > 0) {
                    return duration % k == 0;
                } else {
                    return true;
                }
            } else {
                return this == StatusEffects.HUNGER;
            }
        }
         */
    }

}
