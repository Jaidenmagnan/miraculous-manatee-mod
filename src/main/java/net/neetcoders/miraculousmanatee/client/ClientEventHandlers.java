package net.neetcoders.miraculousmanatee.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.client.renderer.ManateeRenderer;
import net.neetcoders.miraculousmanatee.client.renderer.PenguinRenderer;
import net.neetcoders.miraculousmanatee.config.ModClientConfig;
import net.neetcoders.miraculousmanatee.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

@EventBusSubscriber(modid = MiraculousManateeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEventHandlers {
    private ClientEventHandlers() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MANATEE.get(), ManateeRenderer::new);
        event.registerEntityRenderer(ModEntities.PENGUIN.get(), PenguinRenderer::new);
        if (ModClientConfig.RENDER_BLUBBER_PROJECTILE.get()) {
            event.registerEntityRenderer(ModEntities.BLUBBER_PROJECTILE.get(), ThrownItemRenderer::new);
        }
    }
}
