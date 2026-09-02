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
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

@EventBusSubscriber(modid = MiraculousManateeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModEventHandlers {
    private ModEventHandlers() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
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
        event.register(ModEntities.MANATEE.get(), SpawnPlacementTypes.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Manatee::checkManateeSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.PENGUIN.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Penguin::checkPenguinSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
        // No biome spawns yet; this just makes it a well-behaved night monster if a biome modifier adds it later.
        event.register(ModEntities.EVIL_MANATEE.get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Monster::checkMonsterSpawnRules,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}
