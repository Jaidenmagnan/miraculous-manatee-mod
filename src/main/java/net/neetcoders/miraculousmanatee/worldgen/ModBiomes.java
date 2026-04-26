package net.neetcoders.miraculousmanatee.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;

public final class ModBiomes {
    private ModBiomes() {
    }

    public static final ResourceKey<Biome> MANATEE_SPRINGS = ResourceKey.create(Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "manatee_springs"));
}
