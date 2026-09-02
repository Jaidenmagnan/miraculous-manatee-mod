package net.neetcoders.miraculousmanatee.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neetcoders.miraculousmanatee.client.model.EvilManateeModel;
import net.neetcoders.miraculousmanatee.entity.EvilManatee;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class EvilManateeRenderer extends GeoEntityRenderer<EvilManatee> {

    public EvilManateeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new EvilManateeModel());
        // Renders textures/entity/evil_manatee_glowmask.png at full brightness: the red eyes glow in the dark.
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
