package net.neetcoders.miraculousmanatee.registry;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import net.neetcoders.miraculousmanatee.block.BlubberBlock;
import net.neetcoders.miraculousmanatee.block.BlubberPileBlock;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MiraculousManateeMod.MOD_ID);

    public static final DeferredBlock<BlubberPileBlock> BLUBBER_PILE = BLOCKS.registerBlock("blubber_pile",
            BlubberPileBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.SAND)
                    .strength(0.2f)
                    .noOcclusion());

    public static final DeferredBlock<BlubberBlock> BLUBBER_BLOCK = BLOCKS.registerBlock("blubber_block",
            BlubberBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.SLIME_BLOCK).noOcclusion());

    // Magical spring plants. Like vanilla flowers, the effect is what suspicious stew brewed from them grants.
    public static final DeferredBlock<FlowerBlock> LUMINOUS_CATTAIL = registerSpringPlant("luminous_cattail",
            MobEffects.NIGHT_VISION, 5.0f, MapColor.COLOR_YELLOW);
    public static final DeferredBlock<FlowerBlock> MISTVEIL_FERN = registerSpringPlant("mistveil_fern",
            MobEffects.INVISIBILITY, 4.0f, MapColor.PLANT);
    public static final DeferredBlock<FlowerBlock> SPRINGHEART_BLOOM = registerSpringPlant("springheart_bloom",
            MobEffects.REGENERATION, 5.0f, MapColor.COLOR_PINK);
    public static final DeferredBlock<FlowerBlock> AZURE_DEWCAP = registerSpringPlant("azure_dewcap",
            MobEffects.WATER_BREATHING, 6.0f, MapColor.COLOR_LIGHT_BLUE);
    public static final DeferredBlock<FlowerBlock> MOONLIT_LOTUS = registerSpringPlant("moonlit_lotus",
            MobEffects.LUCK, 6.0f, MapColor.TERRACOTTA_WHITE);

    private ModBlocks() {
    }

    private static DeferredBlock<FlowerBlock> registerSpringPlant(String name, Holder<MobEffect> stewEffect,
            float stewEffectSeconds, MapColor mapColor) {
        return BLOCKS.registerBlock(name, properties -> new FlowerBlock(stewEffect, stewEffectSeconds, properties),
                BlockBehaviour.Properties.of()
                        .mapColor(mapColor)
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.GRASS)
                        .offsetType(BlockBehaviour.OffsetType.XZ)
                        .ignitedByLava());
    }
}
