package net.neetcoders.miraculousmanatee.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * World/gameplay settings, saved per world in {@code serverconfig/miraculousmanatee-server.toml}.
 * <p>
 * Keys are part of the mod's public surface: renaming one silently resets every existing config file, so add
 * new options rather than renaming old ones.
 */
public final class ModServerConfig {
    private static final String TRANSLATION_PREFIX = "config.miraculousmanatee.";

    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final SpawnConfig MANATEE_SPAWN;
    public static final SpawnConfig PENGUIN_SPAWN;

    public static final ModConfigSpec.IntValue BLUBBER_BLASTER_COOLDOWN_TICKS;
    public static final ModConfigSpec.IntValue MANATEE_TAMING_CHANCE_DENOMINATOR;

    public static final ModConfigSpec.IntValue MANATEE_MAX_FAT;
    public static final ModConfigSpec.IntValue MANATEE_FAT_PER_MEAL;
    public static final ModConfigSpec.IntValue MANATEE_GRAZE_COOLDOWN_TICKS;
    public static final ModConfigSpec.BooleanValue MANATEE_EAT_LILY_PADS;

    public static final ModConfigSpec.IntValue EVIL_MANATEE_RAGE_CHANCE_DENOMINATOR;
    public static final ModConfigSpec.IntValue EVIL_MANATEE_RAGE_DURATION_TICKS;
    public static final SpawnConfig EVIL_MANATEE_SPAWN;

    static {
        BUILDER.push("manatee");
        MANATEE_SPAWN = SpawnConfig.define(BUILDER, "manatee", "manatee", 100, 1, 5);
        BUILDER.pop();

        BUILDER.push("penguin");
        PENGUIN_SPAWN = SpawnConfig.define(BUILDER, "penguin", "penguin", 100, 1, 4);
        BUILDER.pop();

        BUILDER.push("blubberBlaster");
        BLUBBER_BLASTER_COOLDOWN_TICKS = BUILDER
                .comment("Cooldown in ticks after firing the blubber blaster.")
                .translation(TRANSLATION_PREFIX + "blubberBlasterCooldownTicks")
                .defineInRange("cooldownTicks", 12, 0, 1200);
        BUILDER.pop();

        BUILDER.push("manateeTaming");
        MANATEE_TAMING_CHANCE_DENOMINATOR = BUILDER
                .comment("1 in N chance to tame a manatee when fed kelp.")
                .translation(TRANSLATION_PREFIX + "manateeTamingChanceDenominator")
                .defineInRange("chanceDenominator", 3, 1, 100);
        BUILDER.pop();

        BUILDER.push("manateeFood");
        MANATEE_MAX_FAT = BUILDER
                .comment("Fat a manatee can accumulate before it stops grazing.")
                .translation(TRANSLATION_PREFIX + "manateeMaxFat")
                .defineInRange("maxFat", 20, 1, 1000);
        MANATEE_FAT_PER_MEAL = BUILDER
                .comment("Fat gained each time a manatee eats a plant or is hand-fed kelp.")
                .translation(TRANSLATION_PREFIX + "manateeFatPerMeal")
                .defineInRange("fatPerMeal", 1, 1, 100);
        MANATEE_GRAZE_COOLDOWN_TICKS = BUILDER
                .comment("Ticks a manatee waits after a meal (or a failed attempt) before looking for food again.")
                .translation(TRANSLATION_PREFIX + "manateeGrazeCooldownTicks")
                .defineInRange("grazeCooldownTicks", 400, 0, 12000);
        MANATEE_EAT_LILY_PADS = BUILDER
                .comment("Whether manatees eat lily pads in addition to kelp.")
                .translation(TRANSLATION_PREFIX + "manateeEatLilyPads")
                .define("eatLilyPads", true);
        BUILDER.pop();

        BUILDER.push("evilManatee");
        EVIL_MANATEE_RAGE_CHANCE_DENOMINATOR = BUILDER
                .comment("While chasing a target, 1 in N chance per tick for an evil manatee to rage (Speed II + Jump Boost II).")
                .translation(TRANSLATION_PREFIX + "evilManateeRageChanceDenominator")
                .defineInRange("rageChanceDenominator", 600, 1, 72000);
        EVIL_MANATEE_RAGE_DURATION_TICKS = BUILDER
                .comment("How long a rage lasts, in ticks.")
                .translation(TRANSLATION_PREFIX + "evilManateeRageDurationTicks")
                .defineInRange("rageDurationTicks", 200, 1, 12000);
        // Evil manatees are night monsters; 25 is deliberately low next to the vanilla zombie's 95.
        EVIL_MANATEE_SPAWN = SpawnConfig.define(BUILDER, "evilManatee", "evil manatee", 25, 1, 2);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private ModServerConfig() {
    }

    /**
     * Natural-spawn settings for one mob in Manatee Springs. Every mob gets the same four keys
     * ({@code naturalSpawnEnabled}, {@code spawnWeight}, {@code minGroupSize}, {@code maxGroupSize}) inside its
     * own config group.
     */
    public static final class SpawnConfig {
        private final ModConfigSpec.BooleanValue enabled;
        private final ModConfigSpec.IntValue weight;
        private final ModConfigSpec.IntValue minGroupSize;
        private final ModConfigSpec.IntValue maxGroupSize;

        private SpawnConfig(ModConfigSpec.BooleanValue enabled, ModConfigSpec.IntValue weight,
                ModConfigSpec.IntValue minGroupSize, ModConfigSpec.IntValue maxGroupSize) {
            this.enabled = enabled;
            this.weight = weight;
            this.minGroupSize = minGroupSize;
            this.maxGroupSize = maxGroupSize;
        }

        /**
         * Defines the four spawn keys in the builder's current group. {@code translationKey} is the camelCase
         * mob name used in {@code config.miraculousmanatee.<translationKey>SpawnWeight} and friends;
         * {@code mobName} is the human-readable name used in the TOML comments.
         */
        private static SpawnConfig define(ModConfigSpec.Builder builder, String translationKey, String mobName,
                int defaultWeight, int defaultMinGroupSize, int defaultMaxGroupSize) {
            String translationBase = TRANSLATION_PREFIX + translationKey;
            ModConfigSpec.BooleanValue enabled = builder
                    .comment("Enable natural " + mobName + " spawning in Manatee Springs.")
                    .translation(translationBase + "NaturalSpawnEnabled")
                    .worldRestart()
                    .define("naturalSpawnEnabled", true);
            ModConfigSpec.IntValue weight = builder
                    .comment("Relative spawn weight for the " + mobName + ".")
                    .translation(translationBase + "SpawnWeight")
                    .worldRestart()
                    .defineInRange("spawnWeight", defaultWeight, 0, 1000);
            ModConfigSpec.IntValue minGroupSize = builder
                    .comment("Minimum " + mobName + " group size per spawn.")
                    .translation(translationBase + "MinGroupSize")
                    .worldRestart()
                    .defineInRange("minGroupSize", defaultMinGroupSize, 1, 64);
            ModConfigSpec.IntValue maxGroupSize = builder
                    .comment("Maximum " + mobName + " group size per spawn.")
                    .translation(translationBase + "MaxGroupSize")
                    .worldRestart()
                    .defineInRange("maxGroupSize", defaultMaxGroupSize, 1, 64);
            return new SpawnConfig(enabled, weight, minGroupSize, maxGroupSize);
        }

        public boolean isEnabled() {
            return enabled.get();
        }

        public int weight() {
            return weight.get();
        }

        /** The configured minimum, capped at the maximum so a min > max typo cannot crash spawning. */
        public int minGroupSize() {
            return Math.min(minGroupSize.get(), maxGroupSize.get());
        }

        /** The configured maximum, raised to the minimum so a min > max typo cannot crash spawning. */
        public int maxGroupSize() {
            return Math.max(maxGroupSize.get(), minGroupSize.get());
        }
    }
}
