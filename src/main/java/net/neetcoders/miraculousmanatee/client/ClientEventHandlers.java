package net.neetcoders.miraculousmanatee.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.registry.ModEntities;
import net.neetcoders.miraculousmanatee.client.renderer.ManateeRenderer;
import net.neetcoders.miraculousmanatee.client.renderer.PenguinRenderer;
import net.neetcoders.miraculousmanatee.client.renderer.AnManateeRenderer;

@EventBusSubscriber(modid = MiraculousManateeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandlers {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MANATEE.get(), ManateeRenderer::new);
        event.registerEntityRenderer(ModEntities.AN_MANATEE.get(), AnManateeRenderer::new);
        event.registerEntityRenderer(ModEntities.PENGUIN.get(), PenguinRenderer::new);
    }

}
