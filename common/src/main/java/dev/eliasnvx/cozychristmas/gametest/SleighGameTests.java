package dev.eliasnvx.cozychristmas.gametest;

import dev.eliasnvx.cozychristmas.karma.KarmaManager;
import dev.eliasnvx.cozychristmas.present.PresentContents;
import dev.eliasnvx.cozychristmas.registry.CozyBlocks;
import dev.eliasnvx.cozychristmas.registry.CozyComponents;
import dev.eliasnvx.cozychristmas.registry.CozyEntities;
import dev.eliasnvx.cozychristmas.season.ChristmasSeason;
import dev.eliasnvx.cozychristmas.sleigh.SantasSleigh;
import dev.eliasnvx.cozychristmas.sleigh.SleighFlight;
import dev.eliasnvx.cozychristmas.workshop.Elf;
import dev.eliasnvx.cozychristmas.workshop.Santa;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * Santa's sleigh on a real server, shared by both loaders. The season clock is fixed and restored within each test
 * body, which runs in one tick; the night is passed to {@link SantasSleigh#advance} directly, because the world's
 * clock can't be moved under the other tests. The flown sleigh and its Santa are removed at the end: they leave the
 * test area. Dev only: not in the released jars.
 */
public final class SleighGameTests {

    public static final List<GameTestEntry> ALL = List.of(
            new GameTestEntry("sleigh_takes_presents_on_delivery_days", SleighGameTests::takesPresentsOnDeliveryDays),
            new GameTestEntry("elves_load_the_sleigh_on_delivery_days", SleighGameTests::elvesLoadIt),
            new GameTestEntry("santa_flies_the_sleigh_on_a_delivery_night", SleighGameTests::santaFliesIt),
            new GameTestEntry("the_sleigh_hides_again_after_a_reload", SleighGameTests::hidesAgainAfterAReload),
            new GameTestEntry("removing_a_flying_sleigh_lets_santa_down", SleighGameTests::removingItLetsSantaDown),
            new GameTestEntry("a_moved_sleigh_comes_back_to_its_new_spot", SleighGameTests::movedSleighComesBackThere),
            new GameTestEntry("santa_aboard_is_out_of_reach", SleighGameTests::santaAboardIsOutOfReach));

    private static final Vec3 SLEIGH = new Vec3(3.5, 1.0, 3.5);
    private static final Vec3 PLAYER = new Vec3(0.5, 1.0, 6.5);
    private static final Vec3 BESIDE = new Vec3(3.5, 1.0, 6.0);
    private static final Vec3 SANTA = new Vec3(6.0, 1.0, 3.5);
    private static final LocalDate CHRISTMAS_EVE = LocalDate.of(2026, 12, 24);
    private static final LocalDate JULY = LocalDate.of(2026, 7, 1);
    /** Far more steps than any phase takes. */
    private static final int ENOUGH = 2000;

    private SleighGameTests() {
    }

    public static void takesPresentsOnDeliveryDays(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        SantasSleigh sleigh = spawn(helper, CozyEntities.SANTAS_SLEIGH.get(), SLEIGH);
        try {
            ChristmasSeason.setClock(fixed(JULY));
            ItemStack present = new ItemStack(CozyBlocks.PRESENT_ITEM.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, present);
            sleigh.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            helper.assertValueEqual(sleigh.load(), 0, "no loading in July");
            helper.assertValueEqual(present.getCount(), 1, "the present stays in hand");

            ChristmasSeason.setClock(fixed(CHRISTMAS_EVE));
            sleigh.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            helper.assertValueEqual(sleigh.load(), 1, "on Christmas Eve it goes in");
            helper.assertTrue(present.isEmpty(), "the present is used");
            helper.assertTrue(sleigh.helpers().contains(player.getUUID()), "Santa will remember who helped");
            helper.assertFalse(KarmaManager.rewardGift(player), "it already earned today's gift karma");

            ItemStack prank = new ItemStack(CozyBlocks.PRESENT_ITEM.get());
            prank.set(CozyComponents.PRESENT_CONTENTS.get(), new PresentContents.Prank(Optional.empty()));
            player.setItemInHand(InteractionHand.MAIN_HAND, prank);
            sleigh.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            helper.assertValueEqual(sleigh.load(), 1, "no pranks in Santa's sack");
            helper.assertFalse(prank.isEmpty(), "the prank stays in hand");
            helper.succeed();
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
            TestPlayers.remove(helper, player);
        }
    }

    public static void elvesLoadIt(GameTestHelper helper) {
        SantasSleigh sleigh = spawn(helper, CozyEntities.SANTAS_SLEIGH.get(), SLEIGH);
        Elf elf = spawn(helper, CozyEntities.ELF.get(), BESIDE);
        try {
            ChristmasSeason.setClock(fixed(JULY));
            elf.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CozyBlocks.PRESENT_ITEM.get()));
            helper.assertFalse(elf.putPresentAway(), "in July a present goes on the pile");
            helper.assertValueEqual(sleigh.load(), 0, "the sleigh stays empty");
            ChristmasSeason.setClock(fixed(CHRISTMAS_EVE));
            elf.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CozyBlocks.PRESENT_ITEM.get()));
            helper.assertTrue(elf.putPresentAway(), "on Christmas Eve it goes into the sleigh");
            helper.assertValueEqual(sleigh.load(), 1, "one present aboard");
            helper.assertFalse(elf.isCarryingPresent(), "the elf's hands are empty");
            helper.succeed();
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
        }
    }

    /** Day: nothing. Night: Santa gets in, it takes off, climbs, is gone; dawn: back, Santa out, sack empty, thanks. */
    public static void santaFliesIt(GameTestHelper helper) {
        ServerPlayer player = TestPlayers.survival(helper, PLAYER);
        SantasSleigh sleigh = spawn(helper, CozyEntities.SANTAS_SLEIGH.get(), SLEIGH);
        Santa santa = spawn(helper, CozyEntities.SANTA.get(), SANTA);
        ServerLevel level = helper.getLevel();
        ChristmasSeason.setClock(fixed(CHRISTMAS_EVE));
        try {
            Vec3 parked = sleigh.position();
            sleigh.advance(level, false); // takes its parking spot
            helper.assertTrue(sleigh.loadPresent(player), "loaded by the player");
            sleigh.advance(level, false);
            helper.assertValueEqual(sleigh.phase(), SleighFlight.Phase.PARKED, "it waits for the night");
            sleigh.advance(level, true);
            helper.assertValueEqual(sleigh.phase(), SleighFlight.Phase.TAKEOFF, "at night it takes off");
            helper.assertTrue(santa.getVehicle() == sleigh, "with Santa aboard");

            run(level, sleigh, true, SleighFlight.Phase.AWAY);
            helper.assertTrue(sleigh.isInvisible() && santa.isInvisible(), "gone into the night");
            helper.assertTrue(sleigh.getY() > parked.y + SleighFlight.TOP - 1.0, "high above the workshop");
            sleigh.advance(level, true);
            helper.assertValueEqual(sleigh.phase(), SleighFlight.Phase.AWAY, "away all night");

            run(level, sleigh, false, SleighFlight.Phase.PARKED);
            helper.assertTrue(sleigh.position().distanceTo(parked) < 0.01, "back where it was parked, at " + sleigh.position());
            helper.assertFalse(santa.isPassenger(), "Santa gets out");
            helper.assertFalse(sleigh.isInvisible() || santa.isInvisible(), "both seen again");
            helper.assertValueEqual(sleigh.load(), 0, "the presents are delivered");
            helper.assertTrue(santa.owesThanks(player.getUUID()), "Santa remembers the helper");

            helper.assertTrue(sleigh.loadPresent(null), "loading again");
            sleigh.advance(level, true);
            helper.assertValueEqual(sleigh.phase(), SleighFlight.Phase.PARKED, "one flight a night");

            santa.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            helper.assertTrue(player.getInventory().contains(stack -> stack.is(CozyBlocks.PRESENT_ITEM.get())),
                    "a thank-you present for the helper");
            helper.assertFalse(santa.owesThanks(player.getUUID()), "thanked once");
            helper.succeed();
        } finally {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
            sleigh.ejectPassengers();
            sleigh.discard();
            santa.discard();
            TestPlayers.remove(helper, player);
        }
    }

    /** Invisibility isn't saved: a sleigh reloaded while away must hide itself and Santa again. */
    public static void hidesAgainAfterAReload(GameTestHelper helper) {
        Flight flight = Flight.start(helper);
        try {
            run(helper.getLevel(), flight.sleigh, true, SleighFlight.Phase.AWAY);
            flight.sleigh.setInvisible(false); // as loaded from disk
            flight.santa.setInvisible(false);
            flight.sleigh.advance(helper.getLevel(), true);
            helper.assertTrue(flight.sleigh.isInvisible() && flight.santa.isInvisible(), "both hidden again while away");
            helper.succeed();
        } finally {
            flight.end(helper);
        }
    }

    /** Removed in flight (a creative player, /kill): Santa is set down where it was parked, seen, and still grateful. */
    public static void removingItLetsSantaDown(GameTestHelper helper) {
        Flight flight = Flight.start(helper);
        try {
            run(helper.getLevel(), flight.sleigh, true, SleighFlight.Phase.AWAY);
            flight.sleigh.discard();
            helper.assertFalse(flight.santa.isPassenger(), "Santa is out");
            helper.assertFalse(flight.santa.isInvisible(), "and seen");
            helper.assertTrue(Math.abs(flight.santa.getY() - flight.parked.y) < 1.5, "on the ground, at y " + flight.santa.getY());
            helper.assertTrue(flight.santa.owesThanks(flight.player.getUUID()), "he still thanks the helper");
            helper.succeed();
        } finally {
            flight.end(helper);
        }
    }

    /** Moved while parked (a piston, /tp): it flies from and lands at the new spot. */
    public static void movedSleighComesBackThere(GameTestHelper helper) {
        Flight flight = Flight.start(helper);
        try {
            Vec3 moved = flight.parked.add(0.0, 0.0, 2.0);
            flight.sleigh.setPos(moved);
            flight.sleigh.advance(helper.getLevel(), false);
            flight.sleigh.advance(helper.getLevel(), true);
            run(helper.getLevel(), flight.sleigh, true, SleighFlight.Phase.AWAY);
            run(helper.getLevel(), flight.sleigh, false, SleighFlight.Phase.PARKED);
            helper.assertTrue(flight.sleigh.position().distanceTo(moved) < 0.01, "back at the new spot, at " + flight.sleigh.position());
            helper.succeed();
        } finally {
            flight.end(helper);
        }
    }

    /** Aboard, Santa can't be talked to (no trading, no presents in the air) or hurt by the blocks he flies through. */
    public static void santaAboardIsOutOfReach(GameTestHelper helper) {
        Flight flight = Flight.start(helper);
        try {
            flight.sleigh.advance(helper.getLevel(), true);
            helper.assertTrue(flight.santa.isPassenger(), "Santa is aboard");
            KarmaManager.set(flight.player, 60);
            flight.santa.interact(flight.player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            helper.assertTrue(flight.santa.getTradingPlayer() == null, "no trading in the air");
            helper.assertFalse(flight.player.getInventory().contains(stack -> stack.is(CozyBlocks.PRESENT_ITEM.get())),
                    "no daily present in the air");
            helper.assertFalse(flight.santa.hurtServer(helper.getLevel(), helper.getLevel().damageSources().inWall(), 5.0F),
                    "flying through a hill doesn't hurt him");
            helper.succeed();
        } finally {
            flight.end(helper);
        }
    }

    /** A loaded sleigh with Santa by it, on Christmas Eve; its parking spot taken. */
    private record Flight(ServerPlayer player, SantasSleigh sleigh, Santa santa, Vec3 parked) {

        static Flight start(GameTestHelper helper) {
            ServerPlayer player = TestPlayers.survival(helper, PLAYER);
            SantasSleigh sleigh = spawn(helper, CozyEntities.SANTAS_SLEIGH.get(), SLEIGH);
            Santa santa = spawn(helper, CozyEntities.SANTA.get(), SANTA);
            ChristmasSeason.setClock(fixed(CHRISTMAS_EVE));
            sleigh.advance(helper.getLevel(), false);
            sleigh.loadPresent(player);
            return new Flight(player, sleigh, santa, sleigh.position());
        }

        void end(GameTestHelper helper) {
            ChristmasSeason.setClock(Clock.systemDefaultZone());
            sleigh.ejectPassengers();
            sleigh.discard();
            santa.discard();
            TestPlayers.remove(helper, player);
        }
    }

    private static void run(ServerLevel level, SantasSleigh sleigh, boolean night, SleighFlight.Phase until) {
        for (int i = 0; i < ENOUGH && sleigh.phase() != until; i++) {
            sleigh.advance(level, night);
        }
        if (sleigh.phase() != until) {
            throw new AssertionError("the sleigh never got to " + until + ", it's " + sleigh.phase());
        }
    }

    private static <T extends Entity> T spawn(GameTestHelper helper, EntityType<T> type, Vec3 at) {
        T entity = type.create(helper.getLevel(), EntitySpawnReason.COMMAND);
        if (entity == null) {
            throw new AssertionError("couldn't create " + type);
        }
        entity.snapTo(helper.absoluteVec(at), 0.0F, 0.0F);
        if (entity instanceof Mob mob) {
            mob.setNoAi(true);
        }
        helper.getLevel().addFreshEntity(entity);
        return entity;
    }

    private static Clock fixed(LocalDate date) {
        return Clock.fixed(date.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
    }
}
