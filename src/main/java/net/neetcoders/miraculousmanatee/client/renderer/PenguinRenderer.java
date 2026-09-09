package net.neetcoders.miraculousmanatee.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.entity.Penguin;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Assets are resolved by name: {@code geo/entity/penguin.geo.json}, {@code textures/entity/penguin.png} and
 * {@code animations/entity/penguin.animation.json}.
 */
public class PenguinRenderer extends GeoEntityRenderer<Penguin> {
    public PenguinRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new DefaultedEntityGeoModel<>(MiraculousManateeMod.id("penguin")));
    }
}
