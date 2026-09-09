package andrews.online_detector.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ODConfigs {
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec.IntValue ONLINE_CHECK_FREQUENCY;
    public static final ModConfigSpec.BooleanValue REDSTONE_PARTICLES;
    public static final ModConfigSpec.BooleanValue PORTAL_PARTICLES;
    static {
        var common = new ModConfigSpec.Builder();
        common.push("Frequency");
        ONLINE_CHECK_FREQUENCY = common.comment("Interval in game ticks. 20 ticks = 1 second at normal server speed.")
                .defineInRange("onlineCheckFrequency", 20, 1, 1200);
        common.pop();
        COMMON_SPEC = common.build();
        var client = new ModConfigSpec.Builder();
        client.push("Particles");
        REDSTONE_PARTICLES = client.comment("Show redstone particles when output is enabled.")
                .define("shouldShowRedstoneParticles", true);
        PORTAL_PARTICLES = client.comment("Show portal particles when the tracked player is online.")
                .define("shouldShowPortalParticles", true);
        client.pop();
        CLIENT_SPEC = client.build();
    }
    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, COMMON_SPEC, "online_detector-common.toml");
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC, "online_detector-client.toml");
    }
    private ODConfigs() {}
}
