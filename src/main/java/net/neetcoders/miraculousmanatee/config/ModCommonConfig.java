package net.neetcoders.miraculousmanatee.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Settings shared by client and server that are not gameplay, saved in {@code config/miraculousmanatee-common.toml}. */
public final class ModCommonConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** Reserved for future troubleshooting output; nothing reads it yet. */
    public static final ModConfigSpec.BooleanValue ENABLE_VERBOSE_LOGGING;

    static {
        BUILDER.push("common");
        ENABLE_VERBOSE_LOGGING = BUILDER
                .comment("Enable extra mod logging for troubleshooting.")
                .translation("config.miraculousmanatee.enableVerboseLogging")
                .gameRestart()
                .define("enableVerboseLogging", false);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private ModCommonConfig() {
    }
}
