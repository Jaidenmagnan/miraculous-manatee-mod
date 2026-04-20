package net.neetcoders.miraculousmanatee.client.renderer;

import net.neetcoders.miraculousmanatee.entity.AnManatee;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.neetcoders.miraculousmanatee.client.model.AnManateeModel;

import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class AnManateeRenderer extends GeoEntityRenderer<AnManatee> {

    public AnManateeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new AnManateeModel());
    }
}
