package net.neetcoders.miraculousmanatee.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;
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
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.SimpleContainer;

public class Manatee extends TamableAnimal implements GeoEntity {

    // CONSTANTS

    static final Item TAMING_FOOD = Items.SEAGRASS;

    // BOILERPLATE FUNCTIONS

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public final SimpleContainer inventory = new SimpleContainer(27);

    public Manatee(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        // make it not want to go on land
        this.setPathfindingMalus(PathType.WALKABLE, 8.0F);
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

    // need to implement water behavior manually because
    // the manatee can't extend TamableAnimal and WaterAnimal

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    public void baseTick() {
        super.baseTick();

        // doesn't drown in water
        if (this.isInWaterOrBubble()) {
            this.setAirSupply(this.getMaxAirSupply());
        }
    }

    // crazy water movement stuff from chatgpt :sob:
    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.isInSittingPose()) {
            this.setDeltaMovement(0, 0, 0);
            return;
        }
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), travelVector);
            this.setDeltaMovement(this.getDeltaMovement().add(0, travelVector.y * this.getSpeed(), 0));
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1,
                new TemptGoal(this, 1.2D, Ingredient.of(Items.SEAGRASS), false));

        this.goalSelector.addGoal(2, new TryFindWaterGoal(this));
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

    // make tamed manatees not despawn
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isTame();
    }

    // TAMING STUFF

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.KELP);
    }

    private PlayState handle(AnimationState<Manatee> state) {
        if (this.isInSittingPose()) {
            System.out.println("IN SITTING POSE");
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.manatee.sit"));
        }

        if (state.isMoving()) {
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.manatee.swim"));
        }

        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.manatee.idle"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Inventory", inventory.createTag(this.registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Inventory")) {
            inventory.fromTag(tag.getList("Inventory", 10), this.registryAccess());
        }
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return ModEntities.MANATEE.get().create(serverLevel);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (!this.level().isClientSide()) {

            // Taming with seagrass
            if (!this.isTame() && heldItem.is(Items.SEAGRASS)) {
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
                return InteractionResult.SUCCESS;
            }

            // Owner interactions
            if (this.isTame() && this.isOwnedBy(player) && hand == InteractionHand.MAIN_HAND) {
                if (!this.level().isClientSide()) {
                    if (player.isShiftKeyDown()) {
                        player.openMenu(new SimpleMenuProvider(
                                (containerId, playerInv, p) -> ChestMenu.threeRows(containerId, playerInv,
                                        this.inventory),
                                Component.literal("Manatee Storage")));
                    } else {
                        boolean pose = !this.isOrderedToSit();
                        this.setInSittingPose(pose);
                        this.setOrderedToSit(pose);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

}
