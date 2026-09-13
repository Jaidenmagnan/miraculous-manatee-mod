package net.neetcoders.miraculousmanatee.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.entity.ElderManatee;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** Shares the manatee geometry and animations; only the texture differs (boss coloring + glow mask). */
public class ElderManateeRenderer extends GeoEntityRenderer<ElderManatee> {
    public ElderManateeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DefaultedEntityGeoModel<ElderManatee>(MiraculousManateeMod.id("manatee"))
                .withAltTexture(MiraculousManateeMod.id("elder_manatee")));
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
