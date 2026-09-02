package net.neetcoders.miraculousmanatee.worldgen;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.SurfaceRules.ConditionSource;
import net.minecraft.world.level.levelgen.SurfaceRules.RuleSource;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.neetcoders.miraculousmanatee.MiraculousManateeMod;
import terrablender.api.SurfaceRuleManager;

/**
 * Surface rules for Manatee Springs. Vanilla's swamp rule (mud, grass, and noise-placed puddles at sea level)
 * never applies here because the biome has its own key, so without this the biome would just be plains dirt.
 * <p>
 * Rules are evaluated top to bottom for each column; the first {@code ifTrue} whose conditions all hold wins.
 * <ul>
 * <li>Under water: a spring bed of white sand with gravel bars and the odd clay patch (axolotls spawn on clay).</li>
 * <li>At the waterline (y 63-64): sand with moss and mossy cobblestone creeping over it.</li>
 * <li>Higher ground: grass with occasional moss and mossy stone outcrops.</li>
 * <li>Below the surface block: sand under water and at the shore, dirt inland.</li>
 * </ul>
 * Registered with TerraBlender, which splices mod rules in ahead of the vanilla overworld rules.
 */
public final class ModSurfaceRules {
    private static final int SEA_LEVEL = 63;
    private static final int SHORELINE_TOP = SEA_LEVEL + 1;

    private ModSurfaceRules() {
    }

    public static void register() {
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, MiraculousManateeMod.MOD_ID,
                makeRules());
    }

    private static RuleSource makeRules() {
        RuleSource grass = state(Blocks.GRASS_BLOCK);
        RuleSource dirt = state(Blocks.DIRT);
        RuleSource sand = state(Blocks.SAND);
        RuleSource gravel = state(Blocks.GRAVEL);
        RuleSource clay = state(Blocks.CLAY);
        RuleSource moss = state(Blocks.MOSS_BLOCK);
        RuleSource mossyCobblestone = state(Blocks.MOSSY_COBBLESTONE);

        // True when the column's surface block is NOT covered by water (same condition vanilla uses for grass).
        ConditionSource aboveWater = SurfaceRules.waterBlockCheck(-1, 0);
        // True for blocks at or above y 65, i.e. clearly inland rather than at the waterline.
        ConditionSource aboveShoreline = SurfaceRules.yBlockCheck(VerticalAnchor.absolute(SHORELINE_TOP + 1), 0);

        RuleSource springBed = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.GRAVEL, 0.3), gravel),
                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.9), clay),
                sand);

        RuleSource shoreline = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.55), mossyCobblestone),
                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.0), moss),
                sand);

        RuleSource upland = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 1.3), mossyCobblestone),
                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE, 0.8), moss),
                grass);

        RuleSource onFloor = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.not(aboveWater), springBed),
                SurfaceRules.ifTrue(SurfaceRules.not(aboveShoreline), shoreline),
                upland);

        RuleSource underFloor = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.not(aboveWater), sand),
                SurfaceRules.ifTrue(SurfaceRules.not(aboveShoreline), sand),
                dirt);

        return SurfaceRules.ifTrue(SurfaceRules.abovePreliminarySurface(),
                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.MANATEE_SPRINGS),
                        SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, onFloor),
                                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, underFloor))));
    }

    private static RuleSource state(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }
}
