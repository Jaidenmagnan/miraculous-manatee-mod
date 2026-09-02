package net.neetcoders.miraculousmanatee.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ModServerConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue MANATEE_NATURAL_SPAWN_ENABLED;
    public static final ModConfigSpec.IntValue MANATEE_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue MANATEE_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue MANATEE_MAX_GROUP_SIZE;

    public static final ModConfigSpec.BooleanValue PENGUIN_NATURAL_SPAWN_ENABLED;
    public static final ModConfigSpec.IntValue PENGUIN_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue PENGUIN_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue PENGUIN_MAX_GROUP_SIZE;

    public static final ModConfigSpec.IntValue BLUBBER_BLASTER_COOLDOWN_TICKS;
    public static final ModConfigSpec.IntValue MANATEE_TAMING_CHANCE_DENOMINATOR;

    public static final ModConfigSpec.IntValue MANATEE_MAX_FAT;
    public static final ModConfigSpec.IntValue MANATEE_FAT_PER_MEAL;
    public static final ModConfigSpec.IntValue MANATEE_GRAZE_COOLDOWN_TICKS;
    public static final ModConfigSpec.BooleanValue MANATEE_EAT_LILY_PADS;

    public static final ModConfigSpec.IntValue EVIL_MANATEE_RAGE_CHANCE_DENOMINATOR;
    public static final ModConfigSpec.IntValue EVIL_MANATEE_RAGE_DURATION_TICKS;
    public static final ModConfigSpec.BooleanValue EVIL_MANATEE_NATURAL_SPAWN_ENABLED;
    public static final ModConfigSpec.IntValue EVIL_MANATEE_SPAWN_WEIGHT;
    public static final ModConfigSpec.IntValue EVIL_MANATEE_MIN_GROUP_SIZE;
    public static final ModConfigSpec.IntValue EVIL_MANATEE_MAX_GROUP_SIZE;

    static {
        BUILDER.push("manatee");
        MANATEE_NATURAL_SPAWN_ENABLED = BUILDER
                .comment("Enable natural manatee spawning in mod biomes.")
                .translation("config.miraculousmanatee.manateeNaturalSpawnEnabled")
                .worldRestart()
                .define("naturalSpawnEnabled", true);
        MANATEE_SPAWN_WEIGHT = BUILDER
                .comment("Relative spawn weight for manatees.")
                .translation("config.miraculousmanatee.manateeSpawnWeight")
                .worldRestart()
                .defineInRange("spawnWeight", 100, 0, 1000);
        MANATEE_MIN_GROUP_SIZE = BUILDER
                .comment("Minimum manatee group size per spawn.")
                .translation("config.miraculousmanatee.manateeMinGroupSize")
                .worldRestart()
                .defineInRange("minGroupSize", 1, 1, 64);
        MANATEE_MAX_GROUP_SIZE = BUILDER
                .comment("Maximum manatee group size per spawn.")
                .translation("config.miraculousmanatee.manateeMaxGroupSize")
                .worldRestart()
                .defineInRange("maxGroupSize", 5, 1, 64);
        BUILDER.pop();

        BUILDER.push("penguin");
        PENGUIN_NATURAL_SPAWN_ENABLED = BUILDER
                .comment("Enable natural penguin spawning in mod biomes.")
                .translation("config.miraculousmanatee.penguinNaturalSpawnEnabled")
                .worldRestart()
                .define("naturalSpawnEnabled", true);
        PENGUIN_SPAWN_WEIGHT = BUILDER
                .comment("Relative spawn weight for penguins.")
                .translation("config.miraculousmanatee.penguinSpawnWeight")
                .worldRestart()
                .defineInRange("spawnWeight", 100, 0, 1000);
        PENGUIN_MIN_GROUP_SIZE = BUILDER
                .comment("Minimum penguin group size per spawn.")
                .translation("config.miraculousmanatee.penguinMinGroupSize")
                .worldRestart()
                .defineInRange("minGroupSize", 1, 1, 64);
        PENGUIN_MAX_GROUP_SIZE = BUILDER
                .comment("Maximum penguin group size per spawn.")
                .translation("config.miraculousmanatee.penguinMaxGroupSize")
                .worldRestart()
                .defineInRange("maxGroupSize", 4, 1, 64);
        BUILDER.pop();

        BUILDER.push("blubberBlaster");
        BLUBBER_BLASTER_COOLDOWN_TICKS = BUILDER
                .comment("Cooldown in ticks after firing the blubber blaster.")
                .translation("config.miraculousmanatee.blubberBlasterCooldownTicks")
                .defineInRange("cooldownTicks", 12, 0, 1200);
        BUILDER.pop();

        BUILDER.push("manateeTaming");
        MANATEE_TAMING_CHANCE_DENOMINATOR = BUILDER
                .comment("1 in N chance to tame a manatee when fed kelp.")
                .translation("config.miraculousmanatee.manateeTamingChanceDenominator")
                .defineInRange("chanceDenominator", 3, 1, 100);
        BUILDER.pop();

        BUILDER.push("manateeFood");
        MANATEE_MAX_FAT = BUILDER
                .comment("Fat a manatee can accumulate before it stops grazing.")
                .translation("config.miraculousmanatee.manateeMaxFat")
                .defineInRange("maxFat", 20, 1, 1000);
        MANATEE_FAT_PER_MEAL = BUILDER
                .comment("Fat gained each time a manatee eats a plant or is hand-fed kelp.")
                .translation("config.miraculousmanatee.manateeFatPerMeal")
                .defineInRange("fatPerMeal", 1, 1, 100);
        MANATEE_GRAZE_COOLDOWN_TICKS = BUILDER
                .comment("Ticks a manatee waits after a meal (or a failed attempt) before looking for food again.")
                .translation("config.miraculousmanatee.manateeGrazeCooldownTicks")
                .defineInRange("grazeCooldownTicks", 400, 0, 12000);
        MANATEE_EAT_LILY_PADS = BUILDER
                .comment("Whether manatees eat lily pads in addition to kelp.")
                .translation("config.miraculousmanatee.manateeEatLilyPads")
                .define("eatLilyPads", true);
        BUILDER.pop();

        BUILDER.push("evilManatee");
        EVIL_MANATEE_RAGE_CHANCE_DENOMINATOR = BUILDER
                .comment("While chasing a target, 1 in N chance per tick for an evil manatee to rage (Speed II + Jump Boost II).")
                .translation("config.miraculousmanatee.evilManateeRageChanceDenominator")
                .defineInRange("rageChanceDenominator", 600, 1, 72000);
        EVIL_MANATEE_RAGE_DURATION_TICKS = BUILDER
                .comment("How long a rage lasts, in ticks.")
                .translation("config.miraculousmanatee.evilManateeRageDurationTicks")
                .defineInRange("rageDurationTicks", 200, 1, 12000);
        EVIL_MANATEE_NATURAL_SPAWN_ENABLED = BUILDER
                .comment("Enable natural evil manatee spawning at night in Manatee Springs.")
                .translation("config.miraculousmanatee.evilManateeNaturalSpawnEnabled")
                .worldRestart()
                .define("naturalSpawnEnabled", true);
        EVIL_MANATEE_SPAWN_WEIGHT = BUILDER
                .comment("Relative spawn weight for evil manatees (vanilla zombies are 95).")
                .translation("config.miraculousmanatee.evilManateeSpawnWeight")
                .worldRestart()
                .defineInRange("spawnWeight", 25, 0, 1000);
        EVIL_MANATEE_MIN_GROUP_SIZE = BUILDER
                .comment("Minimum evil manatee group size per spawn.")
                .translation("config.miraculousmanatee.evilManateeMinGroupSize")
                .worldRestart()
                .defineInRange("minGroupSize", 1, 1, 64);
        EVIL_MANATEE_MAX_GROUP_SIZE = BUILDER
                .comment("Maximum evil manatee group size per spawn.")
                .translation("config.miraculousmanatee.evilManateeMaxGroupSize")
                .worldRestart()
                .defineInRange("maxGroupSize", 2, 1, 64);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private ModServerConfig() {
    }

    public static int manateeMinGroupSize() {
        int min = MANATEE_MIN_GROUP_SIZE.get();
        return Math.min(min, MANATEE_MAX_GROUP_SIZE.get());
    }

    public static int manateeMaxGroupSize() {
        int max = MANATEE_MAX_GROUP_SIZE.get();
        return Math.max(max, MANATEE_MIN_GROUP_SIZE.get());
    }

    public static int penguinMinGroupSize() {
        int min = PENGUIN_MIN_GROUP_SIZE.get();
        return Math.min(min, PENGUIN_MAX_GROUP_SIZE.get());
    }

    public static int penguinMaxGroupSize() {
        int max = PENGUIN_MAX_GROUP_SIZE.get();
        return Math.max(max, PENGUIN_MIN_GROUP_SIZE.get());
    }

    public static int evilManateeMinGroupSize() {
        int min = EVIL_MANATEE_MIN_GROUP_SIZE.get();
        return Math.min(min, EVIL_MANATEE_MAX_GROUP_SIZE.get());
    }

    public static int evilManateeMaxGroupSize() {
        int max = EVIL_MANATEE_MAX_GROUP_SIZE.get();
        return Math.max(max, EVIL_MANATEE_MIN_GROUP_SIZE.get());
    }
}
