package net.neetcoders.miraculousmanatee.worldgen;

import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import terrablender.api.Region;
import terrablender.api.RegionType;

public class ModOverworldRegion extends Region {
    public ModOverworldRegion(int weight) {
        super(ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "overworld"), RegionType.OVERWORLD,
                weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
            Consumer<Pair<Climate.ParameterPoint, net.minecraft.resources.ResourceKey<Biome>>> mapper) {
        addModifiedVanillaOverworldBiomes(mapper, builder -> builder.replaceBiome(Biomes.SWAMP,
                ModBiomes.MANATEE_SPRINGS));
    }
}
