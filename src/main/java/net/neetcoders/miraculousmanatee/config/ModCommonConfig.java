package net.neetcoders.miraculousmanatee.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ModCommonConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_VERBOSE_LOGGING;

    static {
        ENABLE_VERBOSE_LOGGING = BUILDER
                .comment("Enable extra mod logging for troubleshooting.")
                .translation("config.miraculousmanatee.enableVerboseLogging")
                .gameRestart()
                .define("common.enableVerboseLogging", false);
        SPEC = BUILDER.build();
    }

    private ModCommonConfig() {
    }
}
