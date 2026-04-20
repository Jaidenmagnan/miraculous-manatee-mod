package net.neetcoders.miraculousmanatee.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;

public class AnManatee extends Manatee {
    public AnManatee(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "swim_controller", 5, this::handle));
    }

    private PlayState handle(AnimationState<Manatee> state) {
        // if (this.isInSittingPose()) {
        // return
        // state.setAndContinue(RawAnimation.begin().thenLoop("animation.an.manatee.sit"));
        // }

        // if (state.isMoving()) {
        return state.setAndContinue(RawAnimation.begin().thenLoop("animation.an.manatee.swim"));
        // }

        // return
        // state.setAndContinue(RawAnimation.begin().thenLoop("animation.an.manatee.idle"));
    }

}
