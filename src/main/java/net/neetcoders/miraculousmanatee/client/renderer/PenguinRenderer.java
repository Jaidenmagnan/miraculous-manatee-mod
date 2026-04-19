package net.neetcoders.miraculousmanatee.client.renderer;

import net.neetcoders.miraculousmanatee.entity.Penguin;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neetcoders.miraculousmanatee.client.model.PenguinModel;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PenguinRenderer extends GeoEntityRenderer<Penguin> {

    public PenguinRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new PenguinModel());
    }
}
