package net.neetcoders.miraculousmanatee.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neetcoders.miraculousmanatee.config.ModServerConfig;
import net.neetcoders.miraculousmanatee.registry.ModEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A boss-tier {@link EvilManatee}. Inherits the dual water/ground navigation, move control and amphibious
 * {@code travel}/{@code aiStep} behaviour from the parent unchanged, and adds on top of it:
 * <ul>
 * <li>A {@link ServerBossEvent} boss bar, wired exactly like vanilla's {@code WitherBoss}.</li>
 * <li>A leap-slam attack, periodically triggered while a target is within range (see {@link #trySlam()}): on
 * land the boss winds up into a visible leap toward its target ({@link SlamState#LEAPING}) and, on landing,
 * damages/knocks back/slows every non-manatee {@link LivingEntity} nearby behind an expanding particle
 * shockwave; in water, where it can't leap, it keeps the original instant slam instead so it still has an
 * attack while swimming. The three phases - idle, leaping, and the slam impact itself - are driven from
 * {@link #aiStep()} and are not persisted to NBT, so a boss reloaded mid-leap simply resumes idle.</li>
 * <li>A one-shot reinforcement summon: the first time its health drops to half, it spawns two regular
 * {@link EvilManatee} allies.</li>
 * </ul>
 */
public class ElderManatee extends EvilManatee {
    /** Entity event byte broadcast when the underwater instant slam lands, so clients can spawn the splash ring. */
    private static final byte EVENT_TIDAL_SLAM = 101;
    private static final String SUMMONED_REINFORCEMENTS_TAG = "SummonedReinforcements";
    /** Radius (blocks) around a slam impact that takes damage/knockback/slowness. */
    private static final double SLAM_RADIUS = 5.0D;
    /** Range (blocks, squared) the target must be within for a slam to trigger. */
    private static final double SLAM_RANGE_SQR = 36.0D;
    /** Knockback strength of the underwater instant slam. */
    private static final double UNDERWATER_SLAM_KNOCKBACK_STRENGTH = 1.2D;
    /** Knockback strength of the leap slam's landing impact. */
    private static final double LEAP_SLAM_KNOCKBACK_STRENGTH = 1.5D;
    private static final int SLAM_SLOWNESS_DURATION_TICKS = 100;
    private static final int SLAM_SLOWNESS_AMPLIFIER = 1;
    /** Offset applied to each reinforcement so it doesn't spawn stacked on the elder manatee. */
    private static final double REINFORCEMENT_SPAWN_OFFSET = 1.5D;
    private static final int REINFORCEMENT_COUNT = 2;
    private static final float REINFORCEMENT_HEALTH_THRESHOLD = 0.5F;
    /** Splash ring drawn on {@link #EVENT_TIDAL_SLAM}: particle count and radius. */
    private static final int SLAM_PARTICLE_COUNT = 24;
    private static final double SLAM_PARTICLE_RADIUS = 4.0D;

    /** Upward velocity (blocks/tick) of the leap; roughly a 6-7 block apex. */
    private static final double LEAP_VERTICAL_VELOCITY = 1.1D;
    /** Horizontal velocity (blocks/tick) of the leap, toward the target. */
    private static final double LEAP_HORIZONTAL_VELOCITY = 0.5D;
    /** Pitch the leap roar plays at; lower than the parent's rage roar for a heavier feel. */
    private static final float LEAP_ROAR_PITCH = 0.6F;
    /** Fallback: a leap that hasn't landed within this many ticks is abandoned so the boss can't get stuck. */
    private static final int LEAP_TIMEOUT_TICKS = 60;

    /** Radius of each shockwave ring, smallest (emitted first) to largest (emitted last). */
    private static final double[] SHOCKWAVE_RING_RADII = {1.5D, 3.0D, 4.5D};
    /** Number of expanding shockwave rings spawned after a leap slam lands, one per tick. */
    private static final int SHOCKWAVE_RING_COUNT = SHOCKWAVE_RING_RADII.length;
    /** Particles emitted per shockwave ring. */
    private static final int SHOCKWAVE_PARTICLES_PER_RING = 24;
    /** Block chip particles kicked up from the ground the boss landed on. */
    private static final int BLOCK_CHIP_PARTICLE_COUNT = 40;

    /** Phases of the leap-slam attack; see the class javadoc for the full lifecycle. */
    private enum SlamState {
        IDLE,
        LEAPING
    }

    private final ServerBossEvent bossEvent = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.PROGRESS);

    private int slamCooldown;
    private boolean summonedReinforcements;

    /** Current phase of the leap-slam state machine. Transient: always resets to {@link SlamState#IDLE} on load. */
    private SlamState slamState = SlamState.IDLE;
    /** Whether {@link #onGround()} was true last tick; used by {@link #tickLeap()} to detect the moment of landing. */
    private boolean wasOnGroundLastTick;
    /** Ticks left before an in-progress leap is abandoned by the {@link #tickLeap()} fallback timeout. */
    private int leapTimeoutTicks;
    /** Ticks of expanding shockwave ring left to emit; see {@link #tickShockwave(ServerLevel)}. */
    private int shockwaveTicksRemaining;
    private double shockwaveX;
    private double shockwaveY;
    private double shockwaveZ;

    public ElderManatee(EntityType<? extends ElderManatee> type, Level level) {
        super(type, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return EvilManatee.createAttributes()
                .add(Attributes.MAX_HEALTH, 80.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.SCALE, 2.5);
    }

    /** A boss this large never despawns for being far from players. */
    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    /** Bosses don't take portals. */
    @Override
    public boolean canChangeDimensions(Level oldLevel, Level newLevel) {
        return false;
    }

    // ---------------------------------------------------------------------------------------------
    // Boss bar (wired exactly like vanilla WitherBoss)
    // ---------------------------------------------------------------------------------------------

    @Override
    public void startSeenByPlayer(@NotNull ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(@NotNull ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        this.bossEvent.setName(this.getDisplayName());
    }

    // ---------------------------------------------------------------------------------------------
    // Leap slam + reinforcements
    // ---------------------------------------------------------------------------------------------

    @Override
    public void aiStep() {
        // super.aiStep() runs the parent's random ground hop (EvilManatee only hops while onGround()).
        // Everything below - including the leap's launch velocity in startLeap() - runs after it, so it is
        // always the last write to delta movement this tick and can never be overwritten by that hop.
        super.aiStep();
        if (this.level().isClientSide() || !this.isAlive()) {
            return;
        }

        if (this.slamCooldown > 0) {
            this.slamCooldown--;
        }

        if (this.shockwaveTicksRemaining > 0 && this.level() instanceof ServerLevel serverLevel) {
            tickShockwave(serverLevel);
        }

        if (this.slamState == SlamState.LEAPING) {
            tickLeap();
        } else {
            trySlam();
        }

        if (!this.summonedReinforcements && this.getHealth() <= this.getMaxHealth() * REINFORCEMENT_HEALTH_THRESHOLD) {
            this.summonedReinforcements = true;
            if (this.level() instanceof ServerLevel serverLevel) {
                summonReinforcements(serverLevel);
            }
        }
    }

    /**
     * Slam trigger, evaluated once per server tick while {@link #slamState} is {@link SlamState#IDLE} (see
     * {@link #aiStep()}). On land the boss winds up into a leap toward its target; in water - where it can't
     * leap - it keeps the original instant slam so it still has an attack while swimming. If it's airborne
     * without a leap in progress (e.g. still falling from an unrelated jump) this does nothing and leaves the
     * cooldown untouched, so the attack is simply retried next tick instead of being wasted.
     */
    private void trySlam() {
        if (this.slamCooldown > 0) {
            return;
        }
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || this.distanceToSqr(target) > SLAM_RANGE_SQR) {
            return;
        }

        if (this.onGround() && !this.isInWater()) {
            startLeap(target);
        } else if (this.isInWater()) {
            this.slamCooldown = ModServerConfig.ELDER_MANATEE_SLAM_COOLDOWN_TICKS.get();
            performInstantSlam();
        }
        // else: airborne and not leaping - do nothing this tick, cooldown untouched, retried next tick.
    }

    /**
     * Launches the boss into the air toward {@code target} and switches to {@link SlamState#LEAPING}. The slam
     * itself fires on landing (see {@link #tickLeap()}); a fallback timeout guarantees a return to
     * {@link SlamState#IDLE} even if the boss never touches down again.
     */
    private void startLeap(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double motionX = 0.0D;
        double motionZ = 0.0D;
        if (horizontalDistance > 1.0E-4D) {
            motionX = dx / horizontalDistance * LEAP_HORIZONTAL_VELOCITY;
            motionZ = dz / horizontalDistance * LEAP_HORIZONTAL_VELOCITY;
        }
        this.setDeltaMovement(motionX, LEAP_VERTICAL_VELOCITY, motionZ);
        this.hasImpulse = true; // required so the new velocity actually syncs to clients

        this.playSound(SoundEvents.RAVAGER_ROAR, 1.0F, LEAP_ROAR_PITCH);

        this.slamState = SlamState.LEAPING;
        this.leapTimeoutTicks = LEAP_TIMEOUT_TICKS;
        // The boss is still onGround() this tick (the leap velocity hasn't been applied to its position yet),
        // so record that explicitly for tickLeap()'s landing check next tick.
        this.wasOnGroundLastTick = true;
    }

    /**
     * Runs each tick while {@link #slamState} is {@link SlamState#LEAPING}. Detects landing - airborne last
     * tick and on the ground now, or splashed into water - and fires the slam, or counts down the fallback
     * timeout and abandons the leap (returning to idle with the cooldown set, no slam) if it expires first.
     */
    private void tickLeap() {
        boolean onGroundNow = this.onGround();
        boolean landed = (!this.wasOnGroundLastTick && onGroundNow) || this.isInWater();

        if (landed) {
            performLeapSlam();
        } else {
            this.leapTimeoutTicks--;
            if (this.leapTimeoutTicks <= 0) {
                this.slamState = SlamState.IDLE;
                this.slamCooldown = ModServerConfig.ELDER_MANATEE_SLAM_COOLDOWN_TICKS.get();
            }
        }

        this.wasOnGroundLastTick = onGroundNow;
    }

    /** Fires the leap slam's landing impact: damage/knockback/slowness, sound and shockwave particles. */
    private void performLeapSlam() {
        applySlamEffects(LEAP_SLAM_KNOCKBACK_STRENGTH);
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), this.getSoundSource(),
                1.0F, 1.0F);
        this.level().playSound(null, this.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, this.getSoundSource(),
                1.0F, 1.0F);

        if (this.level() instanceof ServerLevel serverLevel) {
            spawnLandingParticles(serverLevel);
        }

        this.slamState = SlamState.IDLE;
        this.slamCooldown = ModServerConfig.ELDER_MANATEE_SLAM_COOLDOWN_TICKS.get();
    }

    /** The old instant slam, kept as the boss's underwater attack since it can't leap while swimming. */
    private void performInstantSlam() {
        applySlamEffects(UNDERWATER_SLAM_KNOCKBACK_STRENGTH);
        this.level().playSound(null, this.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, this.getSoundSource(),
                1.0F, 1.0F);
        this.level().broadcastEntityEvent(this, EVENT_TIDAL_SLAM);
    }

    /** Damages, knocks back and slows every nearby non-manatee living entity. Shared by both slam variants. */
    private void applySlamEffects(double knockbackStrength) {
        for (LivingEntity victim : this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(SLAM_RADIUS),
                entity -> entity != this && entity.isAlive() && !(entity instanceof Manatee) && !(entity instanceof EvilManatee))) {
            victim.hurt(this.damageSources().mobAttack(this), ModServerConfig.ELDER_MANATEE_SLAM_DAMAGE.get().floatValue());
            victim.knockback(knockbackStrength, this.getX() - victim.getX(), this.getZ() - victim.getZ());
            victim.hurtMarked = true; // hurtMarked makes the server send the velocity to players
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLAM_SLOWNESS_DURATION_TICKS,
                    SLAM_SLOWNESS_AMPLIFIER));
        }
    }

    /**
     * Spawns the leap slam's landing particles: an immediate explosion burst and block chips from the ground
     * landed on, plus starts the {@link #shockwaveTicksRemaining} countdown so {@link #tickShockwave} spreads
     * the ring particles over the next few ticks instead of popping them all in at once.
     */
    private void spawnLandingParticles(ServerLevel serverLevel) {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);

        BlockState stateBelow = this.level().getBlockState(this.blockPosition().below());
        if (!stateBelow.isAir()) {
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, stateBelow), x, y, z,
                    BLOCK_CHIP_PARTICLE_COUNT, 0.5D, 0.2D, 0.5D, 0.2D);
        }

        this.shockwaveX = x;
        this.shockwaveY = y;
        this.shockwaveZ = z;
        this.shockwaveTicksRemaining = SHOCKWAVE_RING_COUNT;
    }

    /**
     * Emits one ring of the leap slam's expanding shockwave, called once per tick while
     * {@link #shockwaveTicksRemaining} is positive - independently of {@link #slamState}, so it keeps running
     * after the boss is back to idle. Rings grow from {@link #SHOCKWAVE_RING_RADII}'s smallest radius to its
     * largest across the {@link #SHOCKWAVE_RING_COUNT} ticks it's called.
     */
    private void tickShockwave(ServerLevel serverLevel) {
        int ringIndex = SHOCKWAVE_RING_COUNT - this.shockwaveTicksRemaining;
        double radius = SHOCKWAVE_RING_RADII[ringIndex];
        for (int i = 0; i < SHOCKWAVE_PARTICLES_PER_RING; i++) {
            double angle = 2.0D * Math.PI * i / SHOCKWAVE_PARTICLES_PER_RING;
            double x = this.shockwaveX + radius * Math.cos(angle);
            double z = this.shockwaveZ + radius * Math.sin(angle);
            ParticleOptions particle = i % 2 == 0 ? ParticleTypes.SPLASH : ParticleTypes.CLOUD;
            serverLevel.sendParticles(particle, x, this.shockwaveY + 0.2D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        this.shockwaveTicksRemaining--;
    }

    /** Spawns two {@link EvilManatee} allies next to the boss, targeting whatever it is currently targeting. */
    private void summonReinforcements(ServerLevel serverLevel) {
        for (int i = 0; i < REINFORCEMENT_COUNT; i++) {
            EvilManatee reinforcement = ModEntities.EVIL_MANATEE.get().create(serverLevel);
            if (reinforcement == null) {
                continue;
            }
            double offset = i == 0 ? REINFORCEMENT_SPAWN_OFFSET : -REINFORCEMENT_SPAWN_OFFSET;
            reinforcement.moveTo(this.getX() + offset, this.getY(), this.getZ() + offset, this.getYRot(), 0.0F);
            reinforcement.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(this.blockPosition()),
                    MobSpawnType.MOB_SUMMONED, null);
            reinforcement.setTarget(this.getTarget());
            serverLevel.addFreshEntity(reinforcement);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_TIDAL_SLAM) {
            for (int i = 0; i < SLAM_PARTICLE_COUNT; i++) {
                double angle = 2.0D * Math.PI * i / SLAM_PARTICLE_COUNT;
                double x = this.getX() + SLAM_PARTICLE_RADIUS * Math.cos(angle);
                double z = this.getZ() + SLAM_PARTICLE_RADIUS * Math.sin(angle);
                this.level().addParticle(ParticleTypes.SPLASH, x, this.getY() + 0.2D, z, 0.0D, 0.0D, 0.0D);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(SUMMONED_REINFORCEMENTS_TAG, this.summonedReinforcements);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.summonedReinforcements = tag.getBoolean(SUMMONED_REINFORCEMENTS_TAG);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }
}
