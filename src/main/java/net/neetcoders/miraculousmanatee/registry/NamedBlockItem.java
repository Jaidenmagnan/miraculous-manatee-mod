package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;

/**
 * A BlockItem that uses its own item translation key instead of the block key.
 */
public class NamedBlockItem extends BlockItem {
    public NamedBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public String getDescriptionId() {
        return this.getOrCreateDescriptionId();
    }
}
