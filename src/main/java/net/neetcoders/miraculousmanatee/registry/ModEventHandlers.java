package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.entity.EvilManatee;
import net.neetcoders.miraculousmanatee.entity.Manatee;
import net.neetcoders.miraculousmanatee.entity.Penguin;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

/** Mod-bus listeners for registration-time setup on both sides. Client-only ones live in {@code client}. */
@EventBusSubscriber(modid = MiraculousManateeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEventHandlers {
    private ModEventHandlers() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Exposes the manatee belly to hoppers, pipes and other item handlers.
        event.registerEntity(
                Capabilities.ItemHandler.ENTITY,
                ModEntities.MANATEE.get(),
                (entity, ctx) -> new InvWrapper(entity.getInventory()));
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.MANATEE.get(), Manatee.createAttributes().build());
        event.put(ModEntities.PENGUIN.get(), Penguin.createAttributes().build());
        event.put(ModEntities.EVIL_MANATEE.get(), EvilManatee.createAttributes().build());
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        // Which biomes spawn these mobs (and how often) is decided by ConfigurableSpringsSpawnsModifier; this
        // only says where inside a chunk each mob may be placed.
        event.register(ModEntities.MANATEE.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Manatee::checkManateeSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.PENGUIN.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Penguin::checkPenguinSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // Standard night-monster rules: on the surface, in the dark, not on peaceful.
        event.register(ModEntities.EVIL_MANATEE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        ModCreativeTabs.addToVanillaTabs(event);
    }
}
