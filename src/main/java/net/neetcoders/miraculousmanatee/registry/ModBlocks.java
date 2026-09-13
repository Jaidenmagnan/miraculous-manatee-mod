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
import net.neetcoders.miraculousmanatee.block.ManateeHeadBlock;
import net.neetcoders.miraculousmanatee.block.ManateePortalAltarBlock;
import net.neetcoders.miraculousmanatee.block.ManateePortalBlock;
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

    /** Keystone of the manatee portal frame; has a block item ({@code ModItems.MANATEE_PORTAL_ALTAR}). */
    public static final DeferredBlock<ManateePortalAltarBlock> MANATEE_PORTAL_ALTAR = BLOCKS.registerBlock(
            "manatee_portal_altar", ManateePortalAltarBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.DARK_PRISMARINE)
                    .lightLevel(state -> 7));
    /** The portal's interior block, placed by {@code ManateePortalShape}. Deliberately has no block item; it is never obtained as an item. */
    public static final DeferredBlock<ManateePortalBlock> MANATEE_PORTAL = BLOCKS.registerBlock("manatee_portal",
            ManateePortalBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_PORTAL).noLootTable());

    /** Decorative manatee head; three of them crown the Elder Manatee summoning ritual. */
    public static final DeferredBlock<ManateeHeadBlock> MANATEE_HEAD = BLOCKS.registerBlock("manatee_head",
            ManateeHeadBlock::new, BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .strength(1.0F)
                    .sound(SoundType.WOOL)
                    .noOcclusion());

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
