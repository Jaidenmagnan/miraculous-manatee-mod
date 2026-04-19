package net.neetcoders.miraculousmanatee.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;

public class ModModelLayers {
    public static final ModelLayerLocation MANATEE = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "manatee"), "main");

    public static final ModelLayerLocation PENGUIN = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MiraculousManateeMod.MOD_ID, "penguin"), "main");
}
