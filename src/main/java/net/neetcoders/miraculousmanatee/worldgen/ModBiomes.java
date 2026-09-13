package net.neetcoders.miraculousmanatee.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;

public final class ModBiomes {
    /** Defined in {@code data/miraculousmanatee/worldgen/biome/manatee_springs.json}. */
    public static final ResourceKey<Biome> MANATEE_SPRINGS = ResourceKey.create(Registries.BIOME,
            MiraculousManateeMod.id("manatee_springs"));

    /** Defined in {@code data/miraculousmanatee/worldgen/biome/manatee_plains.json}. */
    public static final ResourceKey<Biome> MANATEE_PLAINS = ResourceKey.create(Registries.BIOME,
            MiraculousManateeMod.id("manatee_plains"));

    private ModBiomes() {
    }
}
