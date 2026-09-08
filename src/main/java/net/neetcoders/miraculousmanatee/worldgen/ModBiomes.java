package net.neetcoders.miraculousmanatee.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;

public final class ModBiomes {
    /** Defined in {@code data/miraculousmanatee/worldgen/biome/manatee_springs.json}. */
    public static final ResourceKey<Biome> MANATEE_SPRINGS = ResourceKey.create(Registries.BIOME,
            MiraculousManateeMod.id("manatee_springs"));

    private ModBiomes() {
    }
}
