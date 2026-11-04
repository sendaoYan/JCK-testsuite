/*
 * @test
 * @summary java.time.Clock API conformance: fixed instant/getZone/millis, and
 *          systemDefaultZone.
 * @run main ClockTest
 */

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.Duration;
import java.time.ZoneId;

public class ClockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClock() {
        Clock fixed = Clock.fixed(Instant.ofEpochSecond(0), ZoneOffset.UTC);
        check(fixed.instant().equals(Instant.ofEpochSecond(0)), "fixed instant");
        check(fixed.getZone().equals(ZoneOffset.UTC), "fixed zone");
        check(fixed.millis() == 0L, "millis");
        check(Clock.systemDefaultZone() != null, "systemDefaultZone non-null");
    }

    public static void main(String[] args) {
        testClock();
        testDeepClock();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== contract deepening harness (appended) ====
    interface DeepRun { void run() throws Throwable; }

    static void xact(DeepRun r, Class<?> type, String exactMsg, String tag) {
        try {
            r.run();
            throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but nothing was thrown");
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(tag + ": expected " + type.getSimpleName() + " but got "
                        + t.getClass().getName() + ": " + t.getMessage(), t);
            if (exactMsg != null && !exactMsg.equals(t.getMessage()))
                throw new AssertionError(tag + ": " + type.getSimpleName() + " message <" + t.getMessage()
                        + "> want <" + exactMsg + ">");
            passed++;
        }
    }

    static void xeq(Object got, Object want, String tag) {
        if (got == null ? want != null : !got.equals(want))
            throw new AssertionError(tag + ": got <" + got + "> want <" + want + ">");
        passed++;
    }


    static void testDeepClock() {
        Instant fixed = Instant.ofEpochSecond(1709206530, 700000000);
        ZoneId z1 = ZoneOffset.ofHours(1);
        Clock c = Clock.fixed(fixed, z1);
        check(c.instant().equals(fixed), "fixed instant");
        check(c.millis() == 1709206530700L, "fixed millis");
        check(c.getZone().equals(z1), "fixed zone");
        check(c.instant().equals(c.instant()), "fixed is stable");
        Clock tickQ = Clock.tick(Clock.fixed(fixed, ZoneOffset.UTC), Duration.ofMillis(250));
        xeq(tickQ.instant().toString(), "2024-02-29T11:35:30.500Z", "tick 250ms floors");
        xeq(Clock.tick(Clock.fixed(fixed, ZoneOffset.UTC), Duration.ofHours(1)).instant().toString(),
            "2024-02-29T11:00:00Z", "tickHours");
        xeq(Clock.tick(Clock.fixed(fixed, ZoneOffset.UTC), Duration.ofSeconds(60)).instant().toString(),
            "2024-02-29T11:35:00Z", "tickMinutes");
        check(Clock.tickSeconds(ZoneOffset.UTC).instant().getNano() == 0, "tickSeconds floors nanos");
        check(Clock.tickMinutes(ZoneOffset.UTC).instant().getNano() == 0
            && Clock.tickMinutes(ZoneOffset.UTC).instant().getEpochSecond() % 60 == 0, "tickMinutes floors");
        Clock off = Clock.offset(Clock.fixed(fixed, ZoneOffset.UTC), Duration.ofHours(1));
        xeq(off.instant().toString(), "2024-02-29T12:35:30.700Z", "offset adds duration");
        Clock offNeg = Clock.offset(Clock.fixed(fixed, ZoneOffset.UTC), Duration.ofMinutes(-60));
        xeq(offNeg.instant().toString(), "2024-02-29T10:35:30.700Z", "offset subtracts");
        check(Clock.systemUTC().instant().isAfter(Instant.ofEpochSecond(1700000000)), "systemUTC monotonic-ish");
        check(Clock.system(ZoneOffset.UTC).getZone().equals(ZoneOffset.UTC), "system zone");
        check(Clock.systemDefaultZone().getZone().equals(ZoneId.systemDefault()), "systemDefaultZone zone");
        long m1 = Clock.systemUTC().millis();
        check(Clock.systemUTC().millis() - m1 >= 0, "millis monotonic");
        xeq(c.withZone(ZoneOffset.ofHours(2)).getZone().toString(), "+02:00", "withZone");
        check(c.withZone(z1) == c, "withZone same returns this");
        xact(() -> Clock.fixed(null, z1), NullPointerException.class, "fixedInstant", "fixed null instant");
        xact(() -> Clock.fixed(fixed, null), NullPointerException.class, "zone", "fixed null zone");
        xact(() -> Clock.tick(null, Duration.ofSeconds(1)), NullPointerException.class, "baseClock", "tick null clock");
        xact(() -> Clock.tick(c, null), NullPointerException.class, "tickDuration", "tick null duration");
        check(Clock.tick(c, Duration.ofSeconds(0)).instant().equals(c.instant()), "tick zero duration is identity");
        xact(() -> Clock.tick(c, Duration.ofSeconds(-1)), IllegalArgumentException.class,
            "Tick duration must not be negative", "tick negative");
        xact(() -> Clock.offset(null, Duration.ofSeconds(1)), NullPointerException.class, "baseClock", "offset null clock");
        check(!c.instant().equals(Clock.offset(c, Duration.ofSeconds(1)).instant()), "offset differs");
        check(c.equals(Clock.fixed(fixed, z1)), "fixed equals same instant+zone");
        check(c.hashCode() == Clock.fixed(fixed, z1).hashCode(), "fixed hashCode");
        check(!c.equals(Clock.fixed(fixed, ZoneOffset.UTC)), "fixed differs by zone");
    }

}
