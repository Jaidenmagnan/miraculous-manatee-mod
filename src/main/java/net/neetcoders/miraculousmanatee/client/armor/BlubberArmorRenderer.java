package net.neetcoders.miraculousmanatee.client.armor;

import software.bernie.geckolib.renderer.GeoArmorRenderer;
import net.neetcoders.miraculousmanatee.item.BlubberArmorItem;

public class BlubberArmorRenderer extends GeoArmorRenderer<BlubberArmorItem> {

    public BlubberArmorRenderer() {
        super(new BlubberArmorModel());
    }
}
