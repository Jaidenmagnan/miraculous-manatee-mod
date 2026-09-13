package net.neetcoders.miraculousmanatee.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;

public final class ModDimensions {
    /** Defined in {@code data/miraculousmanatee/dimension/manatee_dimension.json}. */
    public static final ResourceKey<Level> MANATEE_DIMENSION = ResourceKey.create(Registries.DIMENSION,
            MiraculousManateeMod.id("manatee_dimension"));

    /** Defined in {@code data/miraculousmanatee/dimension_type/manatee_dimension.json}. */
    public static final ResourceKey<DimensionType> MANATEE_DIMENSION_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            MiraculousManateeMod.id("manatee_dimension"));

    private ModDimensions() {
    }
}
