package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.entity.BlubberProjectile;
import net.neetcoders.miraculousmanatee.entity.Manatee;
import net.neetcoders.miraculousmanatee.entity.Penguin;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    private ModEntities() {
    }

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE,
            MiraculousManateeMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Manatee>> MANATEE = ENTITY_TYPES
            .register("manatee", () -> EntityType.Builder.of(Manatee::new, MobCategory.WATER_CREATURE)
                    .sized(1.8f, 0.9f)
                    .build("manatee"));

    public static final DeferredHolder<EntityType<?>, EntityType<Penguin>> PENGUIN = ENTITY_TYPES
            .register("penguin", () -> EntityType.Builder.of(Penguin::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.9f)
                    .build("penguin"));

    public static final DeferredHolder<EntityType<?>, EntityType<BlubberProjectile>> BLUBBER_PROJECTILE = ENTITY_TYPES
            .register("blubber_projectile", () -> EntityType.Builder
                    .<BlubberProjectile>of(BlubberProjectile::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("blubber_projectile"));
}
