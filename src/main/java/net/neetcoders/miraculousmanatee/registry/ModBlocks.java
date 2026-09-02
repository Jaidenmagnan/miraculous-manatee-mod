package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.effect.MobEffects;
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

    public static final DeferredBlock<Block> LUMINOUS_CATTAIL = BLOCKS.register("luminous_cattail",
            () -> new FlowerBlock(MobEffects.NIGHT_VISION, 5.0f, springPlantProperties(MapColor.COLOR_YELLOW)));

    public static final DeferredBlock<Block> MISTVEIL_FERN = BLOCKS.register("mistveil_fern",
            () -> new FlowerBlock(MobEffects.INVISIBILITY, 4.0f, springPlantProperties(MapColor.PLANT)));

    public static final DeferredBlock<Block> SPRINGHEART_BLOOM = BLOCKS.register("springheart_bloom",
            () -> new FlowerBlock(MobEffects.REGENERATION, 5.0f, springPlantProperties(MapColor.COLOR_PINK)));

    public static final DeferredBlock<Block> AZURE_DEWCAP = BLOCKS.register("azure_dewcap",
            () -> new FlowerBlock(MobEffects.WATER_BREATHING, 6.0f, springPlantProperties(MapColor.COLOR_LIGHT_BLUE)));

    public static final DeferredBlock<Block> MOONLIT_LOTUS = BLOCKS.register("moonlit_lotus",
            () -> new FlowerBlock(MobEffects.LUCK, 6.0f, springPlantProperties(MapColor.TERRACOTTA_WHITE)));

    private static BlockBehaviour.Properties springPlantProperties(MapColor mapColor) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .ignitedByLava();
    }
}
