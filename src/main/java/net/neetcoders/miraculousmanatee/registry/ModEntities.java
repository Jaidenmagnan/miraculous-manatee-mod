package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
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
            .register("manatee", () -> EntityType.Builder.of(Manatee::new, MobCategory.CREATURE)
                    .sized(1.8f, 0.9f)
                    .build("manatee"));

    public static final DeferredHolder<EntityType<?>, EntityType<Penguin>> PENGUIN = ENTITY_TYPES
            .register("penguin", () -> EntityType.Builder.of(Penguin::new, MobCategory.CREATURE)
                    .sized(0.6f, 0.9f)
                    .build("penguin"));
}
