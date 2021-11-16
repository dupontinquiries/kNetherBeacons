package net.fabricmc.knb.effects;

import net.fabricmc.knb.KNB;
import net.fabricmc.knb.helper.VelocityHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

public class EffectsKNB {

    /*
    public static ReactiveEffect reactiveEffect;
    public static Identifier reactiveEffectIdentifier = new Identifier(KNB.modName, "reactive_effect");

    public static PreserverEffect preserverEffect;
    public static Identifier preserverEffectIdentifier = new Identifier(KNB.modName, "preserver_effect");

    public static RestorerEffect restorerEffect;
    public static Identifier restorerEffectIdentifier = new Identifier(KNB.modName, "restorer_effect");

    public static StatusEffect soulboundEffect;
    public static Identifier soulboundEffectIdentifier = new Identifier(KNB.modName, "soulbound_effect");
     */

    public static StatusEffect guardianEffect;
    public static Identifier guardianEffectIdentifier = new Identifier(KNB.modName, "guardian_effect");

    static {
        /*
        int idNumber = -1;
        reactiveEffect = Registry.register(Registry.STATUS_EFFECT, reactiveEffectIdentifier, new ReactiveEffect());
        preserverEffect = Registry.register(Registry.STATUS_EFFECT, preserverEffectIdentifier, new PreserverEffect());
        restorerEffect = Registry.register(Registry.STATUS_EFFECT, restorerEffectIdentifier, new RestorerEffect());

        soulboundEffect = Registry.register(Registry.STATUS_EFFECT, soulboundEffectIdentifier, (new SoulboundEffect(StatusEffectType.HARMFUL, 4866583))
                //.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", -0.10000000149011612D, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "55FCED67-E92A-486E-9800-B47F202C4386", -.6, EntityAttributeModifier.Operation.MULTIPLY_TOTAL) );
                //.addAttributeModifier(EntityAttributes.GENERIC_MAX_HEALTH, "55FCED67-E92A-486E-9800-B47F202C4386", -.6, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
                //.addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED,"55FCED67-E92A-486E-9800-B47F202C4386", -.8, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
                //.addAttributeModifier(EntityAttributes.GENERIC_FLYING_SPEED,"55FCED67-E92A-486E-9800-B47F202C4386", -.8, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
                //.addAttributeModifier(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, "55FCED67-E92A-486E-9800-B47F202C4386", -.8, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));\
         */



        // amount = -0.10000000149011612D


        /*
        reactiveEffect = Registry.register(Registry.STATUS_EFFECT, reactiveEffectIdentifier, new InstantStatusEffect(StatusEffectType.HARMFUL, ++idNumber) {
            @Override
            public void applyInstantEffect(@Nullable Entity source, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
                if (!target.isSpectator()) {
                    target.getEntityWorld().createExplosion(null, target.getPos().x, target.getPos().y, target.getPos().z, 7f * amplifier, Explosion.DestructionType.DESTROY);
                }
            }
        });
         */
    }

    public static void reg() {

        guardianEffect = Registry.register(Registry.STATUS_EFFECT, guardianEffectIdentifier, (new BaseEffect(StatusEffectType.BENEFICIAL, 4866583))
                .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, "55FCED67-E92A-486E-9800-B47F202C4386", 2, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
                //.addAttributeModifier(EntityAttributes.GENERIC_ARMOR, "55FCED67-E92A-486E-9800-B47F202C4386", 1, EntityAttributeModifier.Operation.ADDITION)
                .addAttributeModifier(EntityAttributes.GENERIC_ARMOR, "55FCED67-E92A-486E-9800-B47F202C4386", 2, EntityAttributeModifier.Operation.MULTIPLY_TOTAL)
                .addAttributeModifier(EntityAttributes.GENERIC_ATTACK_SPEED, "55FCED67-E92A-486E-9800-B47F202C4386", .5, EntityAttributeModifier.Operation.MULTIPLY_TOTAL));

//        guardianEffect = Registry.register(Registry.STATUS_EFFECT, guardianEffectIdentifier, guardianEffect);
    }

    private static class ReactiveEffect extends StatusEffect {

        protected ReactiveEffect() {
            super(StatusEffectType.HARMFUL, 0);
        }

        protected ReactiveEffect(StatusEffectType type, int color) {
            super(type, color);
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {

            if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity && !((PlayerEntity) entity).canTakeDamage())) {
                LivingEntity le = ((LivingEntity) entity);
                //_x, _y, _z = VelocityHelper.getDirectionalVelocities()
                le.addVelocity(0, 0.04, 0);
                le.damage(DamageSource.IN_FIRE, 3f);
                le.setOnFireFor(7);
            }

        }

    }

    private static class PreserverEffect extends StatusEffect {

        protected PreserverEffect() {
            super(StatusEffectType.NEUTRAL, 0);
        }

        protected PreserverEffect(StatusEffectType type, int color) {
            super(type, color);
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {

        }

    }

    private static class RestorerEffect extends StatusEffect {

        protected RestorerEffect() {
            super(StatusEffectType.NEUTRAL, 0);
        }

        protected RestorerEffect(StatusEffectType type, int color) {
            super(type, color);
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {

        }

    }

    private static class BaseEffect extends StatusEffect {

        protected BaseEffect(StatusEffectType type, int color) {
            super(type, color);
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            entity.addVelocity(0, .2, 0);
//           if (entity instanceof PlayerEntity) {
//                PlayerEntity p = (PlayerEntity) entity;
//            }
            super.applyUpdateEffect(entity, amplifier);
        }

    }

    private static class SoulboundEffect extends StatusEffect {

        protected SoulboundEffect(StatusEffectType type, int color) {
            super(type, color);
        }

        @Override
        public void applyUpdateEffect(LivingEntity entity, int amplifier) {
            if (entity instanceof PlayerEntity) {
                PlayerEntity p = (PlayerEntity) entity;
            }
        }

    }

}
