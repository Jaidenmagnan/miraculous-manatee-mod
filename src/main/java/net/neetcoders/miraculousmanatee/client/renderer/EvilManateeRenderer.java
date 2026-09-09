package net.neetcoders.miraculousmanatee.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.entity.EvilManatee;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** Shares the manatee geometry and animations; only the texture differs (dark body, red eyes + glow mask). */
public class EvilManateeRenderer extends GeoEntityRenderer<EvilManatee> {
    public EvilManateeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DefaultedEntityGeoModel<EvilManatee>(MiraculousManateeMod.id("manatee"))
                .withAltTexture(MiraculousManateeMod.id("evil_manatee")));
        // Renders textures/entity/evil_manatee_glowmask.png at full brightness: the red eyes glow in the dark.
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
