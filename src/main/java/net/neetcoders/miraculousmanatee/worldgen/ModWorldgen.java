package net.neetcoders.miraculousmanatee.worldgen;

import net.minecraft.resources.ResourceLocation;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import terrablender.api.Regions;

public final class ModWorldgen {
    private static final int REGION_WEIGHT = 3;

    private ModWorldgen() {
    }

    public static void register() {
        Regions.register(ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "overworld"),
                new ModOverworldRegion(REGION_WEIGHT));
    }
}
