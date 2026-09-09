package net.neetcoders.miraculousmanatee.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.entity.Manatee;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Assets are resolved by name: {@code geo/entity/manatee.geo.json}, {@code textures/entity/manatee.png} and
 * {@code animations/entity/manatee.animation.json}.
 */
public class ManateeRenderer extends GeoEntityRenderer<Manatee> {
    public ManateeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DefaultedEntityGeoModel<>(MiraculousManateeMod.id("manatee")));
    }
}
