package dev.eliasnvx.cozychristmas.sleigh;

import dev.eliasnvx.cozychristmas.sleigh.SleighFlight.Phase;
import dev.eliasnvx.cozychristmas.sleigh.SleighFlight.Pose;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SleighFlightTest {

    private static final double CLOSE = 1.0e-6;
    private static final float[] YAWS = {0.0F, 90.0F, 180.0F, -90.0F, 33.0F};

    @Test
    void startsAndEndsWhereItWasParked() {
        for (float yaw : YAWS) {
            assertAt(SleighFlight.pose(Phase.TAKEOFF, 0, yaw), 0.0, 0.0, 0.0);
            assertAt(SleighFlight.pose(Phase.LANDING, Phase.LANDING.ticks(), yaw), 0.0, 0.0, 0.0);
            assertEquals(yaw, SleighFlight.pose(Phase.LANDING, Phase.LANDING.ticks(), yaw).yaw(), CLOSE, "lands facing as parked");
        }
    }

    @Test
    void takesOffAlongItsFacing() {
        Pose end = SleighFlight.pose(Phase.TAKEOFF, Phase.TAKEOFF.ticks(), 0.0F);
        assertAt(end, 0.0, SleighFlight.LIFT, SleighFlight.RUNWAY); // yaw 0 faces +z
        Pose west = SleighFlight.pose(Phase.TAKEOFF, Phase.TAKEOFF.ticks(), 90.0F);
        assertAt(west, -SleighFlight.RUNWAY, SleighFlight.LIFT, 0.0);
    }

    @Test
    void phasesJoinWithoutJumps() {
        for (float yaw : YAWS) {
            assertSame(SleighFlight.pose(Phase.TAKEOFF, Phase.TAKEOFF.ticks(), yaw), SleighFlight.pose(Phase.CLIMB, 0, yaw));
            assertSame(SleighFlight.pose(Phase.CLIMB, Phase.CLIMB.ticks(), yaw), SleighFlight.pose(Phase.AWAY, 0, yaw));
            assertSame(SleighFlight.pose(Phase.DESCENT, Phase.DESCENT.ticks(), yaw), SleighFlight.pose(Phase.LANDING, 0, yaw));
            assertEquals(yaw, wrap(SleighFlight.pose(Phase.CLIMB, 0, yaw).yaw()), 1.0e-3, "the spiral starts on the runway's heading");
            assertEquals(yaw, wrap(SleighFlight.pose(Phase.DESCENT, Phase.DESCENT.ticks(), yaw).yaw()), 1.0e-3,
                    "and the descent ends on it");
        }
    }

    @Test
    void climbsToTheTopAndComesDownFromIt() {
        assertEquals(SleighFlight.TOP, SleighFlight.pose(Phase.AWAY, 0, 0.0F).dy(), CLOSE);
        assertEquals(SleighFlight.TOP, SleighFlight.pose(Phase.DESCENT, 0, 0.0F).dy(), CLOSE);
        double last = -1.0;
        for (int tick = 0; tick <= Phase.CLIMB.ticks(); tick += 20) {
            double dy = SleighFlight.pose(Phase.CLIMB, tick, 0.0F).dy();
            assertTrue(dy > last, "always climbing");
            last = dy;
        }
    }

    @Test
    void staysNearTheWorkshop() {
        double reach = SleighFlight.RUNWAY + 2 * SleighFlight.RADIUS + 1.0;
        for (Phase phase : Phase.values()) {
            for (int tick = 0; tick <= Math.max(1, phase.ticks()); tick += 5) {
                Pose pose = SleighFlight.pose(phase, tick, 45.0F);
                assertTrue(Math.hypot(pose.dx(), pose.dz()) < reach, phase + " " + tick + " stays within loaded chunks");
            }
        }
    }

    @Test
    void movesSmoothly() {
        for (Phase phase : new Phase[]{Phase.TAKEOFF, Phase.CLIMB, Phase.DESCENT, Phase.LANDING}) {
            for (int tick = 0; tick < phase.ticks(); tick++) {
                Pose a = SleighFlight.pose(phase, tick, 10.0F);
                Pose b = SleighFlight.pose(phase, tick + 1, 10.0F);
                double step = Math.sqrt(sq(b.dx() - a.dx()) + sq(b.dy() - a.dy()) + sq(b.dz() - a.dz()));
                assertTrue(step < 1.0, phase + " moves less than a block a tick at " + tick + ": " + step);
            }
        }
    }

    private static void assertAt(Pose pose, double dx, double dy, double dz) {
        assertEquals(dx, pose.dx(), CLOSE, "dx");
        assertEquals(dy, pose.dy(), CLOSE, "dy");
        assertEquals(dz, pose.dz(), CLOSE, "dz");
    }

    private static void assertSame(Pose a, Pose b) {
        assertEquals(a.dx(), b.dx(), 1.0e-4);
        assertEquals(a.dy(), b.dy(), 1.0e-4);
        assertEquals(a.dz(), b.dz(), 1.0e-4);
    }

    private static float wrap(float yaw) {
        float wrapped = yaw % 360.0F;
        if (wrapped > 180.0F) {
            wrapped -= 360.0F;
        } else if (wrapped <= -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    private static double sq(double value) {
        return value * value;
    }
}
