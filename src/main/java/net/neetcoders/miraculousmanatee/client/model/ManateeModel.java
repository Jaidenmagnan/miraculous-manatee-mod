package net.neetcoders.miraculousmanatee.client.model;

import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.entity.Manatee;

public class ManateeModel extends GeoModel<Manatee> {

    @Override
    public ResourceLocation getModelResource(Manatee entity) {
        return ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "geo/manatee.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Manatee entity) {
        return ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "textures/entity/manatee.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Manatee entity) {
        return ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "animations/manatee.animation.json");
    }
}
