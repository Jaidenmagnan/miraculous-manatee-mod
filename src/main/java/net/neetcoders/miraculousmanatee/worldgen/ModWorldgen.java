package net.neetcoders.miraculousmanatee.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neoforged.bus.api.IEventBus;
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

    private ModWorldgen() {
    }

    public static void register(IEventBus modEventBus) {
        BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        Regions.register(ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "overworld"),
                new ModOverworldRegion(REGION_WEIGHT));
    }
}
