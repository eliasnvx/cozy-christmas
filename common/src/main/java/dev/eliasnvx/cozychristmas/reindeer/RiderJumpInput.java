package dev.eliasnvx.cozychristmas.reindeer;

/**
 * Whether a living entity holds jump. 26.3 has {@code LivingEntity#isJumping()}; 1.20.1 keeps the flag in the protected
 * field {@code jumping} without a getter, so a mixin on {@code LivingEntity} implements this. A flying reindeer climbs
 * while its rider holds jump.
 */
public interface RiderJumpInput {

    boolean cozychristmas$isJumping();

    /** False if the mixin isn't there (the rider can still climb by looking up). */
    static boolean isJumping(Object entity) {
        return entity instanceof RiderJumpInput input && input.cozychristmas$isJumping();
    }
}
