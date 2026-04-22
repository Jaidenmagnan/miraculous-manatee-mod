package net.neetcoders.miraculousmanatee.client.armor;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import net.neetcoders.miraculousmanatee.item.BlubberArmorItem;

public class BlubberArmorModel extends GeoModel<BlubberArmorItem> {

    @Override
    public ResourceLocation getModelResource(BlubberArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("miraculousmanatee", "geo/blubber_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BlubberArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("miraculousmanatee", "textures/armor/blubber_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BlubberArmorItem animatable) {
        return ResourceLocation.fromNamespaceAndPath("miraculousmanatee", "animations/blubber_armor.animation.json");
    }
}
