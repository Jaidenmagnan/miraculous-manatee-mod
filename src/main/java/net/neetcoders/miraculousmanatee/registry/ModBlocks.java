package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.block.BlubberBlock;
import net.neetcoders.miraculousmanatee.block.BlubberPileBlock;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private ModBlocks() {
    }

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MiraculousManateeMod.MOD_ID);

    public static final DeferredBlock<Block> BLUBBER_PILE = BLOCKS.register("blubber_pile", () -> new BlubberPileBlock(
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.2f)
                    .noOcclusion()));

    public static final DeferredBlock<Block> BLUBBER_BLOCK = BLOCKS.register("blubber_block", () -> new BlubberBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.SLIME_BLOCK).noOcclusion()));
}
