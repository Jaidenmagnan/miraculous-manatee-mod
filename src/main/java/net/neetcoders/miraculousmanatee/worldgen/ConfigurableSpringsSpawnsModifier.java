package net.neetcoders.miraculousmanatee.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neetcoders.miraculousmanatee.config.ModServerConfig;
import net.neetcoders.miraculousmanatee.registry.ModEntities;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

public final class ConfigurableSpringsSpawnsModifier implements BiomeModifier {
    public static final ConfigurableSpringsSpawnsModifier INSTANCE = new ConfigurableSpringsSpawnsModifier();

    private ConfigurableSpringsSpawnsModifier() {
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        boolean isManateeSprings = biome.is(ModBiomes.MANATEE_SPRINGS)
                || biome.unwrapKey()
                        .map(key -> key.location().equals(ModBiomes.MANATEE_SPRINGS.location()))
                        .orElse(false);
        if (phase != Phase.ADD || !isManateeSprings) {
            return;
        }

        if (ModServerConfig.MANATEE_NATURAL_SPAWN_ENABLED.get()) {
            builder.getMobSpawnSettings().addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(
                    ModEntities.MANATEE.get(),
                    ModServerConfig.MANATEE_SPAWN_WEIGHT.get(),
                    ModServerConfig.manateeMinGroupSize(),
                    ModServerConfig.manateeMaxGroupSize()));
        }

        if (ModServerConfig.PENGUIN_NATURAL_SPAWN_ENABLED.get()) {
            builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(
                    ModEntities.PENGUIN.get(),
                    ModServerConfig.PENGUIN_SPAWN_WEIGHT.get(),
                    ModServerConfig.penguinMinGroupSize(),
                    ModServerConfig.penguinMaxGroupSize()));
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ModWorldgen.CONFIGURABLE_SPRINGS_SPAWNS_MODIFIER.get();
    }
}
