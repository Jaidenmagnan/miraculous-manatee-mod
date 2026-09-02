package net.neetcoders.miraculousmanatee.client.model;

import net.minecraft.resources.ResourceLocation;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.entity.EvilManatee;
import software.bernie.geckolib.model.GeoModel;

/** Shares the manatee geometry and animations; only the texture differs (dark body, red eyes + glow mask). */
public class EvilManateeModel extends GeoModel<EvilManatee> {

    @Override
    public ResourceLocation getModelResource(EvilManatee entity) {
        return ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "geo/manatee.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EvilManatee entity) {
        return ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "textures/entity/evil_manatee.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EvilManatee entity) {
        return ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "animations/manatee.animation.json");
    }
}
