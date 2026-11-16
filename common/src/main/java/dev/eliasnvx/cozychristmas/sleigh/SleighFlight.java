package dev.eliasnvx.cozychristmas.sleigh;

import net.minecraft.util.Mth;

/**
 * Where Santa's sleigh is during its flight, as a pure function of the phase and the ticks into it, relative to where
 * it was parked and the way it faced ({@code yaw}, Minecraft's: 0 faces +z). It runs down its runway and lifts off,
 * climbs in a wide spiral and is gone ({@link Phase#AWAY}, hidden high above); at dawn it spirals down on the other
 * side and lands on the runway from behind, so it stops where and how it was parked.
 */
public final class SleighFlight {

    /** What the sleigh is doing; saved and synced by its ordinal. */
    public enum Phase {
        PARKED(0), TAKEOFF(80), CLIMB(400), AWAY(0), DESCENT(400), LANDING(80);

        private final int ticks;

        Phase(int ticks) {
            this.ticks = ticks;
        }

        /** How long the phase lasts; 0 for the ones that wait for something (night, dawn). */
        public int ticks() {
            return ticks;
        }

        public boolean isFlying() {
            return this != PARKED;
        }

        public static Phase byOrdinal(int ordinal) {
            Phase[] phases = values();
            return ordinal >= 0 && ordinal < phases.length ? phases[ordinal] : PARKED;
        }
    }

    /** Blocks run along the ground and up before the spiral. */
    public static final double RUNWAY = 18.0;
    /** Height at the end of the runway. */
    public static final double LIFT = 8.0;
    /** The spiral's radius. */
    public static final double RADIUS = 22.0;
    /** Height above the parking spot where the sleigh vanishes and reappears. */
    public static final double TOP = 70.0;
    /** Full turns of the spiral. */
    public static final int TURNS = 2;

    private SleighFlight() {
    }

    /** An offset from the parking spot and the sleigh's yaw there. */
    public record Pose(double dx, double dy, double dz, float yaw) {
    }

    /** The pose {@code tick} ticks into {@code phase} (clamped to the phase); parked and away poses are fixed. */
    public static Pose pose(Phase phase, int tick, float yaw) {
        double t = phase.ticks() == 0 ? 1.0 : Mth.clamp(tick / (double) phase.ticks(), 0.0, 1.0);
        double fx = -Math.sin(Math.toRadians(yaw));
        double fz = Math.cos(Math.toRadians(yaw));
        return switch (phase) {
            case PARKED -> new Pose(0.0, 0.0, 0.0, yaw);
            // speeding up along the runway, lifting off toward its end
            case TAKEOFF -> along(fx, fz, RUNWAY * t * t, LIFT * t * t, yaw);
            case CLIMB -> spiral(fx, fz, RUNWAY, Mth.TWO_PI * TURNS * t, Mth.lerp(t, LIFT, TOP));
            case AWAY -> spiral(fx, fz, RUNWAY, Mth.TWO_PI * TURNS, TOP);
            // the same kind of spiral ending behind the parking spot, then in along the runway, slowing down
            case DESCENT -> spiral(fx, fz, -RUNWAY, Mth.TWO_PI * TURNS * (t - 1.0), Mth.lerp(t, TOP, LIFT));
            case LANDING -> along(fx, fz, -RUNWAY * (1.0 - t) * (1.0 - t), LIFT * (1.0 - t) * (1.0 - t), yaw);
        };
    }

    private static Pose along(double fx, double fz, double distance, double height, float yaw) {
        return new Pose(fx * distance, height, fz * distance, yaw);
    }

    /**
     * On a circle that touches the runway's line at {@code start} blocks ahead, heading the runway's way there:
     * {@code angle} 0 is that point, positive angles run on around the circle (to the left).
     */
    private static Pose spiral(double fx, double fz, double start, double angle, double height) {
        // left of the heading: the circle's center side
        double nx = fz;
        double nz = -fx;
        double centerX = fx * start + nx * RADIUS;
        double centerZ = fz * start + nz * RADIUS;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = centerX + RADIUS * (-nx * cos + fx * sin);
        double z = centerZ + RADIUS * (-nz * cos + fz * sin);
        double vx = nx * sin + fx * cos;
        double vz = nz * sin + fz * cos;
        return new Pose(x, height, z, yawOf(vx, vz));
    }

    /** Minecraft's yaw of a heading (0 = +z, 90 = -x). */
    public static float yawOf(double vx, double vz) {
        return (float) (Mth.atan2(-vx, vz) * Mth.RAD_TO_DEG);
    }
}
