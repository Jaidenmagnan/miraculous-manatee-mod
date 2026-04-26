package net.neetcoders.miraculousmanatee.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neetcoders.miraculousmanatee.registry.ModEntities;
import net.neetcoders.miraculousmanatee.registry.ModItems;

public class BlubberProjectile extends ThrowableItemProjectile {
    private static final float DAMAGE = 4.0f;

    public BlubberProjectile(EntityType<? extends BlubberProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public BlubberProjectile(Level level, LivingEntity owner) {
        super(ModEntities.BLUBBER_PROJECTILE.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BLUBBER.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        DamageSource damageSource = this.damageSources().thrown(this, this.getOwner());
        result.getEntity().hurt(damageSource, DAMAGE);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide()) {
            this.discard();
        }
    }
}
