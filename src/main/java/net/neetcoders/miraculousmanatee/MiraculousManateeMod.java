package net.neetcoders.miraculousmanatee;

import net.minecraft.world.item.CreativeModeTabs;
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
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(MiraculousManateeMod.MOD_ID)
public final class MiraculousManateeMod {
    public static final String MOD_ID = "miraculousmanatee";

    public MiraculousManateeMod(IEventBus modEventBus, ModContainer modContainer) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, ModServerConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ModClientConfig.SPEC);
        if (FMLEnvironment.dist.isClient()) {
            ClientConfigScreenHooks.register(modContainer);
        }

        ModWorldgen.register(modEventBus);

        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.BLUBBER);
            event.accept(ModItems.BLUBBER_BLOCK);
        }
    }
}
