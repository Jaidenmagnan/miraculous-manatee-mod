package net.neetcoders.miraculousmanatee.client.renderer;

import net.neetcoders.miraculousmanatee.entity.Manatee;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neetcoders.miraculousmanatee.client.model.ManateeModel;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ManateeRenderer extends GeoEntityRenderer<Manatee> {

    public ManateeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new ManateeModel());
    }
}
