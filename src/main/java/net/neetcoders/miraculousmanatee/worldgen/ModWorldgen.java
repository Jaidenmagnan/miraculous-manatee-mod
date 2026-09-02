package net.neetcoders.miraculousmanatee.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.worldgen.feature.SpringPoolFeature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import terrablender.api.Regions;

public final class ModWorldgen {
    private static final int REGION_WEIGHT = 3;
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister
            .create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, MiraculousManateeMod.MOD_ID);
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<ConfigurableSpringsSpawnsModifier>> CONFIGURABLE_SPRINGS_SPAWNS_MODIFIER = BIOME_MODIFIER_SERIALIZERS
            .register("configurable_springs_spawns", () -> MapCodec.unit(ConfigurableSpringsSpawnsModifier.INSTANCE));

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE,
            MiraculousManateeMod.MOD_ID);
    /** Spring-head pool; configured/placed in {@code data/miraculousmanatee/worldgen/.../spring_pool.json}. */
    public static final DeferredHolder<Feature<?>, SpringPoolFeature> SPRING_POOL = FEATURES.register("spring_pool",
            () -> new SpringPoolFeature(NoneFeatureConfiguration.CODEC));

    private ModWorldgen() {
    }

    public static void register(IEventBus modEventBus) {
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        FEATURES.register(modEventBus);
        Regions.register(ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "overworld"),
                new ModOverworldRegion(REGION_WEIGHT));
        modEventBus.addListener(ModWorldgen::onCommonSetup);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // TerraBlender wants surface rules registered during common setup, on the main thread.
        event.enqueueWork(ModSurfaceRules::register);
    }
}
