package net.neetcoders.miraculousmanatee.client.model;

import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.entity.AnManatee;

public class AnManateeModel extends GeoModel<AnManatee> {

    @Override
    public ResourceLocation getModelResource(AnManatee entity) {
        return ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "geo/anmanatee.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AnManatee entity) {
        return ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "textures/entity/anmanatee.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AnManatee entity) {
        return ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID,
                "animations/anmanatee.animation.json");
    }
}
