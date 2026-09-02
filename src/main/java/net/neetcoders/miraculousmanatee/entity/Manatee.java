package net.neetcoders.miraculousmanatee.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.config.ModServerConfig;
import net.neetcoders.miraculousmanatee.entity.goal.ManateeGrazeGoal;
import net.neetcoders.miraculousmanatee.registry.ModEntities;
import net.neetcoders.miraculousmanatee.registry.ModItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Manatee extends TamableAnimal implements GeoEntity {
    private static final Ingredient TEMPTATION_ITEMS = Ingredient.of(Items.KELP);
    private static final String INVENTORY_TAG = "Inventory";
    private static final String FAT_TAG = "Fat";
    private static final Component INVENTORY_TITLE = Component.literal("Manatee's Belly");

    /** Entity event byte broadcast when the manatee eats something (kelp crumbs on the client). Vanilla uses 0-65. */
    private static final byte EVENT_ATE = 100;

    // Fat is synced to the client so the renderer can react to it (see refreshFatScale), and saved to NBT.
    private static final EntityDataAccessor<Integer> DATA_FAT = SynchedEntityData.defineId(Manatee.class,
            EntityDataSerializers.INT);
    // Eating is synced so the client-side animation controller can play the eat clip.
    private static final EntityDataAccessor<Boolean> DATA_EATING = SynchedEntityData.defineId(Manatee.class,
            EntityDataSerializers.BOOLEAN);

    /** Each point of fat grows the manatee by this fraction of its base scale (1% per point). */
    private static final double FAT_SCALE_PER_POINT = 0.01D;
    private static final ResourceLocation FAT_SCALE_MODIFIER_ID = ResourceLocation
            .fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "fat_scale");

    // ---------------------------------------------------------------------------------------------
    // Movement tuning. These feed the swimming move control below; tweak them in-game if the manatee
    // circles path nodes (turn speeds too low) or overshoots them (in-water speed too high).
    // ---------------------------------------------------------------------------------------------
    /** Max body pitch (degrees) the manatee will tilt to reach a node above/below it. Manatees don't dive steeply. */
    private static final int MAX_PITCH_DEGREES = 45;
    /** Max yaw change per tick (degrees). Dolphins use 10; a lumbering manatee turns a bit slower. */
    private static final int YAW_TURN_SPEED = 8;
    /**
     * Multiplied with MOVEMENT_SPEED while swimming. The move control feeds forward input of MOVEMENT_SPEED (0.2)
     * and travel() scales it by getSpeed() = 0.2 * this, so thrust is 0.2 * 0.2 * 0.35 = 0.014 blocks/tick.
     * With the 0.9 drag in travel() that settles at ~0.14 blocks/tick, roughly 2.8 blocks/s.
     */
    private static final float IN_WATER_SPEED_MODIFIER = 0.35F;
    /** Multiplied with MOVEMENT_SPEED while crawling on land back toward water. */
    private static final float ON_LAND_SPEED_MODIFIER = 0.1F;
    /** Gentle sink applied each tick while idle so a resting manatee settles instead of hovering mid-water. */
    private static final double IDLE_SINK_PER_TICK = 0.002D;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final SimpleContainer inventory = new SimpleContainer(27);

    /*
     * =============================================================================================
     * PATHFINDING OVERVIEW
     * =============================================================================================
     * Minecraft mob movement is three cooperating layers. Understanding them explains every choice
     * in this class:
     *
     * 1. PathNavigation (createNavigation) owns a PathFinder, which runs A* over "nodes" produced by
     *    a NodeEvaluator. We use WaterBoundPathNavigation, whose SwimNodeEvaluator only emits nodes
     *    that are water (6-connected plus horizontal diagonals). Kelp counts as water because its
     *    fluid state is water, so the manatee can path straight into a kelp bed. The alternative,
     *    AmphibiousPathNavigation (axolotl/frog/turtle), also emits walkable land nodes, which we do
     *    NOT want: a manatee that paths onto a beach strands itself.
     *
     * 2. MoveControl receives the next path node each tick (setWantedPosition) and turns it into
     *    steering input (yaw, pitch, forward/vertical thrust). The default ground MoveControl only
     *    sets yaw and forward speed and never produces vertical input, so the old manatee could not
     *    swim DOWN to kelp below it, only bob upward via the jump hack. SmoothSwimmingMoveControl
     *    (dolphin/frog/axolotl) pitches the body toward the node and splits thrust into forward and
     *    vertical components, giving true 3D steering. ManateeMoveControl adds a land fallback.
     *
     * 3. travel() converts that steering input into velocity. The water branch below mirrors the
     *    vanilla WaterAnimal recipe: apply thrust relative to facing, move, then damp by 0.9.
     *
     * Pathfinding maluses: WATER = 0 (free), WALKABLE = 8 (strongly discouraged). SwimNodeEvaluator
     * never emits WALKABLE nodes, so the second value only matters for goals that use the generic
     * WalkNodeEvaluator checks (e.g. random-position validation).
     * =============================================================================================
     */
    public Manatee(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.setPathfindingMalus(PathType.WALKABLE, 8.0f);
        this.moveControl = new ManateeMoveControl(this);
        // Keeps head and body aligned and levels the pitch back to 0 once the path is done.
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "swim_controller", 5, this::handle));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FAT, 0);
        builder.define(DATA_EATING, false);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_FAT.equals(key)) {
            refreshFatScale();
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();

        if (this.isInWaterOrBubble()) {
            this.setAirSupply(this.getMaxAirSupply());
        }
    }

    /**
     * Water movement. {@code travelVector} is (strafe, vertical, forward) input written by the move control.
     * {@link #moveRelative} rotates that input by the body yaw and scales it by {@link #getSpeed()}, so the
     * vertical component from {@link SmoothSwimmingMoveControl} is already included. We then damp velocity
     * by 0.9 per tick like every vanilla water mob, which caps top speed at thrust / 0.1.
     */
    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.isInSittingPose()) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
            if (this.getNavigation().isDone() && !this.isEating()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -IDLE_SINK_PER_TICK, 0.0D));
            }
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    // Small head-turn limits (like Dolphin) so look goals rotate the whole body slowly instead of
    // snapping; the GeckoLib model has no independent head bone rotation anyway.
    @Override
    public int getMaxHeadXRot() {
        return 5;
    }

    @Override
    public int getMaxHeadYRot() {
        return 10;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new TemptGoal(this, 1.2D, TEMPTATION_ITEMS, false));
        this.goalSelector.addGoal(2, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(3, new ManateeGrazeGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0, 10));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return WaterAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.SCALE, 1.5);
    }

    public static boolean checkManateeSpawnRules(EntityType<Manatee> entityType, LevelAccessor level,
            MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.WATER);
    }

    @Override
    public boolean checkSpawnObstruction(@NotNull LevelReader level) {
        return level.getFluidState(this.blockPosition()).is(FluidTags.WATER);
    }

    // make tamed manatees not despawn
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isTame();
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) {
        return itemStack.is(Items.KELP);
    }

    private PlayState handle(AnimationState<Manatee> state) {
        if (this.isInSittingPose()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.manatee.sit"));
        }

        if (this.isEating()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.manatee.eat"));
        }

        if (state.isMoving()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.manatee.swim"));
        }

        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.manatee.idle"));
    }

    // ---------------------------------------------------------------------------------------------
    // Fat + eating
    // ---------------------------------------------------------------------------------------------

    public int getFat() {
        return this.entityData.get(DATA_FAT);
    }

    public void setFat(int fat) {
        this.entityData.set(DATA_FAT, Mth.clamp(fat, 0, getMaxFat()));
    }

    public void addFat(int amount) {
        setFat(getFat() + amount);
    }

    public int getMaxFat() {
        return ModServerConfig.MANATEE_MAX_FAT.get();
    }

    /** True once the manatee has hit the configured fat cap and should stop grazing. */
    public boolean isFull() {
        return getFat() >= getMaxFat();
    }

    public boolean isEating() {
        return this.entityData.get(DATA_EATING);
    }

    public void setEating(boolean eating) {
        this.entityData.set(DATA_EATING, eating);
    }

    /**
     * Called whenever the manatee finishes a meal (grazed plant or hand-fed kelp). Gains fat, and if there
     * is room in the belly also produces a piece of blubber. A full belly no longer blocks eating; fat is
     * the limiter.
     */
    public void onAte() {
        addFat(ModServerConfig.MANATEE_FAT_PER_MEAL.get());
        if (canStoreBlubber()) {
            addBlubberToBelly();
        }
        this.level().broadcastEntityEvent(this, EVENT_ATE);
    }

    /** Applies the fat-based size bonus as a transient SCALE modifier (recomputed from synced data, never saved). */
    private void refreshFatScale() {
        AttributeInstance scale = this.getAttribute(Attributes.SCALE);
        if (scale == null) {
            return;
        }
        double bonus = getFat() * FAT_SCALE_PER_POINT;
        if (bonus <= 0.0D) {
            scale.removeModifier(FAT_SCALE_MODIFIER_ID);
        } else {
            scale.addOrUpdateTransientModifier(new AttributeModifier(FAT_SCALE_MODIFIER_ID, bonus,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATE) {
            // Kelp crumbs drifting out of the mouth (LivingEntity's helper for this is private).
            ItemParticleOption crumbs = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.KELP));
            for (int i = 0; i < 8; i++) {
                Vec3 velocity = new Vec3((this.random.nextDouble() - 0.5D) * 0.1D, this.random.nextDouble() * 0.1D,
                        (this.random.nextDouble() - 0.5D) * 0.1D);
                Vec3 mouth = this.getEyePosition().add(this.getLookAngle().scale(this.getBbWidth() * 0.5D));
                this.level().addParticle(crumbs, mouth.x, mouth.y, mouth.z, velocity.x, velocity.y + 0.05D, velocity.z);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put(INVENTORY_TAG, inventory.createTag(this.registryAccess()));
        tag.putInt(FAT_TAG, getFat());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(INVENTORY_TAG)) {
            inventory.fromTag(tag.getList(INVENTORY_TAG, 10), this.registryAccess());
        }
        setFat(tag.getInt(FAT_TAG));
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return ModEntities.MANATEE.get().create(serverLevel);
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (!this.level().isClientSide()) {
            if (!this.isTame() && TEMPTATION_ITEMS.test(heldItem)) {
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
                if (this.random.nextInt(ModServerConfig.MANATEE_TAMING_CHANCE_DENOMINATOR.get()) == 0) {
                    this.tame(player);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
                return InteractionResult.SUCCESS;
            }

            if (this.isTame() && this.isOwnedBy(player)) {
                // Hand feeding: a tamed manatee accepts kelp from its owner until it is full.
                if (TEMPTATION_ITEMS.test(heldItem) && !this.isFull()) {
                    this.usePlayerItem(player, hand, heldItem);
                    this.onAte();
                    return InteractionResult.SUCCESS;
                }

                if (hand == InteractionHand.MAIN_HAND) {
                    if (player.isShiftKeyDown()) {
                        player.openMenu(new SimpleMenuProvider(
                                (containerId, playerInv, p) -> ChestMenu.threeRows(containerId, playerInv, inventory),
                                INVENTORY_TITLE));
                    } else {
                        boolean shouldSit = !this.isOrderedToSit();
                        this.setInSittingPose(shouldSit);
                        this.setOrderedToSit(shouldSit);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    public Container getInventory() {
        return inventory;
    }

    public boolean canStoreBlubber() {
        ItemStack blubber = new ItemStack(ModItems.BLUBBER.get());
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack.isEmpty() || (ItemStack.isSameItemSameComponents(itemStack, blubber)
                    && itemStack.getCount() < itemStack.getMaxStackSize())) {
                return true;
            }
        }

        return false;
    }

    public boolean addBlubberToBelly() {
        ItemStack remainder = inventory.addItem(new ItemStack(ModItems.BLUBBER.get()));
        return remainder.isEmpty();
    }

    /**
     * {@link SmoothSwimmingMoveControl} with a land fallback.
     * <p>
     * The smooth-swimming control only steers while the navigation has a path in progress, and
     * {@link WaterBoundPathNavigation} refuses to build paths unless the mob is in liquid. That leaves a beached
     * manatee unable to move at all, even though {@link TryFindWaterGoal} sets a wanted position directly on the
     * move control. When out of water with no path, fall back to the plain ground behaviour: face the wanted
     * position and push forward, so the manatee can crawl the last couple of blocks back into the water.
     */
    private static class ManateeMoveControl extends SmoothSwimmingMoveControl {
        private final Manatee manatee;

        ManateeMoveControl(Manatee manatee) {
            super(manatee, MAX_PITCH_DEGREES, YAW_TURN_SPEED, IN_WATER_SPEED_MODIFIER, ON_LAND_SPEED_MODIFIER, false);
            this.manatee = manatee;
        }

        @Override
        public void tick() {
            if (!manatee.isInWater() && this.operation == Operation.MOVE_TO && manatee.getNavigation().isDone()) {
                // Mirror vanilla MoveControl: consume the request this tick; the goal re-issues it while needed.
                this.operation = Operation.WAIT;
                double dx = this.wantedX - manatee.getX();
                double dz = this.wantedZ - manatee.getZ();
                if (dx * dx + dz * dz < MIN_SPEED_SQR) {
                    manatee.setZza(0.0F);
                    return;
                }
                float yaw = (float) (Mth.atan2(dz, dx) * (180.0F / (float) Math.PI)) - 90.0F;
                manatee.setYRot(this.rotlerp(manatee.getYRot(), yaw, 90.0F));
                manatee.yBodyRot = manatee.getYRot();
                // Mob#setSpeed also writes the forward input (zza) that LivingEntity#travel consumes.
                manatee.setSpeed((float) (this.speedModifier * manatee.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                return;
            }
            super.tick();
        }
    }
}
