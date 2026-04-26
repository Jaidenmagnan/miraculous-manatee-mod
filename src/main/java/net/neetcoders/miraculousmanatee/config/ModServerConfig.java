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
}
