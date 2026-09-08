package net.neetcoders.miraculousmanatee.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Visual-only settings, saved in {@code config/miraculousmanatee-client.toml}. */
public final class ModClientConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue RENDER_BLUBBER_PROJECTILE;

    static {
        BUILDER.push("client");
        RENDER_BLUBBER_PROJECTILE = BUILDER
                .comment("Render the visible model for blubber projectiles.")
                .translation("config.miraculousmanatee.renderBlubberProjectile")
                .gameRestart()
                .define("renderBlubberProjectile", true);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private ModClientConfig() {
    }
}
