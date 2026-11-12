package dev.eliasnvx.cozychristmas.fabric.gametest;

/** Switches for {@code mixin.LevelExtractorMixin}; set from the render thread by client tests only. */
public final class TestCamera {

    /** Draw the local player even while the camera is another entity. */
    public static volatile boolean showPlayer;

    private TestCamera() {
    }
}
