package net.neetcoders.miraculousmanatee.client.model;

import software.bernie.geckolib.model.GeoModel;
import net.minecraft.resources.ResourceLocation;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.entity.Penguin;

public class PenguinModel extends GeoModel<Penguin> {

    @Override
    public ResourceLocation getModelResource(Penguin entity) {
        return ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "geo/penguin.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Penguin entity) {
        return ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "textures/entity/penguin.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Penguin entity) {
        return ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "animations/penguin.animation.json");
    }
}
