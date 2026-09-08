package net.neetcoders.miraculousmanatee;

import net.minecraft.resources.ResourceLocation;
import net.neetcoders.miraculousmanatee.client.ClientConfigScreenHooks;
import net.neetcoders.miraculousmanatee.config.ModClientConfig;
import net.neetcoders.miraculousmanatee.config.ModCommonConfig;
import net.neetcoders.miraculousmanatee.config.ModServerConfig;
import net.neetcoders.miraculousmanatee.registry.ModBlocks;
import net.neetcoders.miraculousmanatee.registry.ModCreativeTabs;
import net.neetcoders.miraculousmanatee.registry.ModEntities;
import net.neetcoders.miraculousmanatee.registry.ModItems;
import net.neetcoders.miraculousmanatee.worldgen.ModWorldgen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Mod entry point. Wires the deferred registers and configs; everything else lives in the package that owns it
 * ({@code registry}, {@code entity}, {@code worldgen}, {@code client}, {@code config}).
 */
@Mod(MiraculousManateeMod.MOD_ID)
public final class MiraculousManateeMod {
    public static final String MOD_ID = "miraculousmanatee";

    public MiraculousManateeMod(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModWorldgen.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, ModServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ModClientConfig.SPEC);
        if (FMLEnvironment.dist.isClient()) {
            ClientConfigScreenHooks.register(modContainer);
        }
    }

    /** Builds a {@link ResourceLocation} in this mod's namespace, e.g. {@code miraculousmanatee:manatee}. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
