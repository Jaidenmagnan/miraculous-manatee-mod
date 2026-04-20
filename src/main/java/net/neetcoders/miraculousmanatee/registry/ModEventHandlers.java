package net.neetcoders.miraculousmanatee.registry;

import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.entity.Manatee;
import net.neetcoders.miraculousmanatee.entity.Penguin;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

@EventBusSubscriber(modid = MiraculousManateeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventHandlers {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(
                Capabilities.ItemHandler.ENTITY,
                ModEntities.MANATEE.get(),
                (entity, ctx) -> new InvWrapper(((Manatee) entity).inventory));
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntities.MANATEE.get(), Manatee.createAttributes().build());
        event.put(ModEntities.AN_MANATEE.get(), Manatee.createAttributes().build());
        event.put(ModEntities.PENGUIN.get(), Penguin.createAttributes().build());
    }
}
