package net.neetcoders.miraculousmanatee.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neetcoders.miraculousmanatee.config.ModServerConfig;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * The manatee's hostile, amphibious cousin. Same model and stats as {@link Manatee} except 10 hearts and an
 * attack, but it is a {@link Monster}: it hunts players on land and in water, hops after them, shrugs off fall
 * damage, and occasionally flies into a rage (Speed II + Jump Boost II).
 *
 * <h2>Pathfinding</h2>
 * Modelled on the vanilla Drowned, which is the only vanilla mob that both walks and swims with purpose:
 * <ul>
 * <li>Two navigations are kept and swapped every tick in {@link #updateSwimming()}: {@link WaterBoundPathNavigation}
 * (A* over water nodes) while swimming after something in the water, {@link GroundPathNavigation} otherwise.
 * The ground navigation is allowed to float, so it can wade through rivers toward a player on the far bank.</li>
 * <li>{@link EvilManateeMoveControl} steers in 3D while swimming (adds velocity straight toward the next node) and
 * falls back to the ordinary ground control on land, which is what triggers jumps over obstacles.</li>
 * <li>{@link #travel} uses the water recipe (thrust, move, 0.9 drag) while swimming and the normal land physics
 * otherwise, so gravity, jumping and fall handling all work on land.</li>
 * </ul>
 */
public class EvilManatee extends Monster implements GeoEntity {
    /** Ticks of Speed II / Jump Boost II granted by a rage. */
    private static final int RAGE_EFFECT_AMPLIFIER = 1;
    /** While chasing on land, 1-in-N chance per tick to hop (roughly one hop per half second). */
    private static final int HOP_CHANCE = 6;
    private static final double HOP_MIN_DISTANCE_SQR = 4.0D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final WaterBoundPathNavigation waterNavigation;
    private final GroundPathNavigation groundNavigation;

    public EvilManatee(EntityType<? extends EvilManatee> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.moveControl = new EvilManateeMoveControl(this);
        this.waterNavigation = new WaterBoundPathNavigation(this, level);
        this.groundNavigation = new GroundPathNavigation(this, level);
        this.groundNavigation.setCanFloat(true);
        this.navigation = this.groundNavigation;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 4)
                .add(Attributes.SCALE, 1.5);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SurfaceWhenWalkingGoal(this));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move_controller", 5, this::handle));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private PlayState handle(AnimationState<EvilManatee> state) {
        if (state.isMoving()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.manatee.swim"));
        }
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.manatee.idle"));
    }

    // ---------------------------------------------------------------------------------------------
    // Amphibious movement
    // ---------------------------------------------------------------------------------------------

    /** Swim (3D water pathing) when in water with nothing to chase, or when the target is in the water too. */
    boolean wantsToSwim() {
        if (!this.isInWater()) {
            return false;
        }
        LivingEntity target = this.getTarget();
        return target == null || target.isInWater();
    }

    @Override
    public void updateSwimming() {
        if (!this.level().isClientSide()) {
            if (this.isEffectiveAi() && this.wantsToSwim()) {
                this.navigation = this.waterNavigation;
                this.setSwimming(true);
            } else {
                this.navigation = this.groundNavigation;
                this.setSwimming(false);
            }
        }
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.isControlledByLocalInstance() && this.wantsToSwim()) {
            this.moveRelative(0.01F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (this.isInWaterOrBubble()) {
            this.setAirSupply(this.getMaxAirSupply());
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide() || !this.isAlive()) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }

        // Random rage: Speed II + Jump Boost II for a while.
        if (!this.hasEffect(MobEffects.MOVEMENT_SPEED)
                && this.random.nextInt(ModServerConfig.EVIL_MANATEE_RAGE_CHANCE_DENOMINATOR.get()) == 0) {
            int duration = ModServerConfig.EVIL_MANATEE_RAGE_DURATION_TICKS.get();
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, RAGE_EFFECT_AMPLIFIER));
            this.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, RAGE_EFFECT_AMPLIFIER));
            this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, 1.4F);
        }

        // Bounding pursuit: keep hopping while closing in on land.
        if (this.onGround() && !this.isInWater()
                && this.distanceToSqr(target) > HOP_MIN_DISTANCE_SQR
                && this.random.nextInt(HOP_CHANCE) == 0) {
            this.getJumpControl().jump();
        }
    }

    /** Immune to fall damage: it lands from every hop without a scratch. */
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return !this.isSwimming();
    }

    /**
     * Drowned-style move control. Swimming: add velocity straight toward the wanted node (true 3D steering).
     * Otherwise defer to the stock ground control, which handles walking and obstacle jumps.
     */
    private static class EvilManateeMoveControl extends MoveControl {
        private final EvilManatee evilManatee;

        EvilManateeMoveControl(EvilManatee evilManatee) {
            super(evilManatee);
            this.evilManatee = evilManatee;
        }

        @Override
        public void tick() {
            if (evilManatee.wantsToSwim()) {
                if (this.operation != Operation.MOVE_TO || evilManatee.getNavigation().isDone()) {
                    evilManatee.setSpeed(0.0F);
                    return;
                }

                double dx = this.wantedX - evilManatee.getX();
                double dy = this.wantedY - evilManatee.getY();
                double dz = this.wantedZ - evilManatee.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                dy /= distance;
                float yaw = (float) (Mth.atan2(dz, dx) * 180.0F / (float) Math.PI) - 90.0F;
                evilManatee.setYRot(this.rotlerp(evilManatee.getYRot(), yaw, 90.0F));
                evilManatee.yBodyRot = evilManatee.getYRot();
                float wantedSpeed = (float) (this.speedModifier * evilManatee.getAttributeValue(Attributes.MOVEMENT_SPEED));
                float speed = Mth.lerp(0.125F, evilManatee.getSpeed(), wantedSpeed);
                evilManatee.setSpeed(speed);
                evilManatee.setDeltaMovement(evilManatee.getDeltaMovement()
                        .add(speed * dx * 0.005D, speed * dy * 0.1D, speed * dz * 0.005D));
            } else {
                if (!evilManatee.onGround()) {
                    evilManatee.setDeltaMovement(evilManatee.getDeltaMovement().add(0.0D, -0.008D, 0.0D));
                }
                super.tick();
            }
        }
    }

    /**
     * {@link FloatGoal} that only kicks in while using the ground navigation (chasing something on land), so
     * the evil manatee bobs up and wades out instead of walking along the river bed forever. While it genuinely
     * wants to swim the water navigation handles depth itself.
     */
    private static class SurfaceWhenWalkingGoal extends FloatGoal {
        private final EvilManatee evilManatee;

        SurfaceWhenWalkingGoal(EvilManatee evilManatee) {
            super(evilManatee);
            this.evilManatee = evilManatee;
        }

        @Override
        public boolean canUse() {
            return !evilManatee.wantsToSwim() && super.canUse();
        }
    }
}
