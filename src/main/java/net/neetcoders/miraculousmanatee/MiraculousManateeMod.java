package net.neetcoders.miraculousmanatee;

import net.minecraft.world.item.CreativeModeTabs;
import net.neetcoders.miraculousmanatee.registry.ModBlocks;
import net.neetcoders.miraculousmanatee.registry.ModCreativeTabs;
import net.neetcoders.miraculousmanatee.registry.ModEntities;
import net.neetcoders.miraculousmanatee.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(MiraculousManateeMod.MOD_ID)
public final class MiraculousManateeMod {
    public static final String MOD_ID = "miraculousmanatee";

    public MiraculousManateeMod(IEventBus modEventBus) {
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);

        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.BLUBBER);
            event.accept(ModItems.BLUBBER_BLOCK);
        }
    }
}
