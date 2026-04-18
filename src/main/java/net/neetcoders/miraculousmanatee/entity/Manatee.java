package net.neetcoders.miraculousmanatee.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neetcoders.miraculousmanatee.registry.ModEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.TamableAnimal;

import net.minecraft.world.entity.ai.attributes.Attributes;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.PathType;

public class Manatee extends TamableAnimal implements GeoEntity {

    // CONSTANTS

    static final Item TAMING_FOOD = Items.SEAGRASS;

    // BOILERPLATE FUNCTIONS

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Manatee(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "swim_controller", 5, this::handle));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // MOB BEHAVIOR

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 3F, 5.0F, 2.0F));
        this.goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.0, 10));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }


    public static AttributeSupplier.Builder createAttributes() {
        return WaterAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.SCALE, 1.5);
    }

    // TAMING STUFF

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.KELP);
    }

    private PlayState handle(AnimationState<Manatee> state) {
        if (this.isInSittingPose()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.manatee.idle"));
        }

        if (state.isMoving()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.manatee.swim"));
        }

        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.manatee.idle"));
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return ModEntities.MANATEE.get().create(serverLevel);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // taming food is seagrass, configurable at top
        if(!this.isTame() && itemStack.is(TAMING_FOOD)) {
            // is this checking if the player is in creative?
            if(!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }

            if(!this.level().isClientSide) {
                // 1 in 3 chance to tame
                if(this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.setOrderedToSit(true);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }

                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        // owner toggle sit
        if(this.isTame() && this.isOwnedBy(player)) {
            if(!this.level().isClientSide) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.setInSittingPose(this.isOrderedToSit());
                this.navigation.stop();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }
}
