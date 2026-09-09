package net.neetcoders.miraculousmanatee.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neetcoders.miraculousmanatee.config.ModServerConfig;
import net.neetcoders.miraculousmanatee.config.ModServerConfig.SpawnConfig;
import net.neetcoders.miraculousmanatee.registry.ModEntities;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

/**
 * Adds the mod's natural spawns to Manatee Springs from the server config, so weights and group sizes can be
 * tuned without a datapack. Attached to the biome by
 * {@code data/miraculousmanatee/neoforge/biome_modifier/configurable_springs_spawns.json}.
 */
public final class ConfigurableSpringsSpawnsModifier implements BiomeModifier {
    public static final ConfigurableSpringsSpawnsModifier INSTANCE = new ConfigurableSpringsSpawnsModifier();

    private ConfigurableSpringsSpawnsModifier() {
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !biome.is(ModBiomes.MANATEE_SPRINGS)) {
            return;
        }
        addSpawn(builder, MobCategory.WATER_CREATURE, ModEntities.MANATEE.get(), ModServerConfig.MANATEE_SPAWN);
        addSpawn(builder, MobCategory.CREATURE, ModEntities.PENGUIN.get(), ModServerConfig.PENGUIN_SPAWN);
        addSpawn(builder, MobCategory.MONSTER, ModEntities.EVIL_MANATEE.get(), ModServerConfig.EVIL_MANATEE_SPAWN);
    }

    private static void addSpawn(ModifiableBiomeInfo.BiomeInfo.Builder builder, MobCategory category,
            EntityType<?> entityType, SpawnConfig config) {
        if (!config.isEnabled()) {
            return;
        }
        builder.getMobSpawnSettings().addSpawn(category, new MobSpawnSettings.SpawnerData(
                entityType, config.weight(), config.minGroupSize(), config.maxGroupSize()));
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return ModWorldgen.CONFIGURABLE_SPRINGS_SPAWNS_MODIFIER.get();
    }
}
