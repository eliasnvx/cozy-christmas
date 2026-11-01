package dev.eliasnvx.cozychristmas.config;

import dev.architectury.platform.Platform;

/** Access to the config files. Common is loaded in {@code CozyChristmas.init}. */
public final class CozyConfig {

    private static final ConfigFile<CommonConfig> COMMON = new ConfigFile<>(
            Platform.getConfigFolder().resolve("cozychristmas-common.json"), CommonConfig.CODEC, CommonConfig.DEFAULTS);

    private CozyConfig() {
    }

    public static CommonConfig common() {
        return COMMON.get();
    }

    public static void loadCommon() {
        COMMON.load();
    }
}
