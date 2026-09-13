package net.neetcoders.miraculousmanatee.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * The keystone block of the manatee portal frame. It behaves like a plain block; it exists as its own class
 * solely so that {@code ManateeKeyItem} can {@code instanceof}-check for it when looking for the keystone of
 * a portal frame to activate.
 */
public class ManateePortalAltarBlock extends Block {
    public ManateePortalAltarBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
