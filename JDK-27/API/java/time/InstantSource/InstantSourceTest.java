/*
 * @test
 * @summary java.time.InstantSource conformance: white-box interface + black-box fixed/offset/tick/withZone/lambda.
 * @run main InstantSourceTest
 */

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.time.ZoneId;

public class InstantSourceTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.InstantSource");
        check(c.isInterface(), "is interface");
        check(c.getMethod("instant") != null, "instant()");
        check(c.getMethod("millis") != null, "millis()");
        check(c.getMethod("withZone", ZoneId.class) != null, "withZone(ZoneId)");
        check(c.getMethod("system") != null, "static system()");
        check(c.getMethod("tick", InstantSource.class, Duration.class) != null, "static tick(...)");
        check(c.getMethod("fixed", Instant.class) != null, "static fixed(Instant)");
        check(c.getMethod("offset", InstantSource.class, Duration.class) != null, "static offset(...)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Instant fixed = Instant.ofEpochSecond(100);
        InstantSource src = InstantSource.fixed(fixed);
        check(src.instant().equals(fixed), "fixed instant");
        check(src.millis() == 100000L, "millis");
        check(InstantSource.system().instant() != null, "system instant");

        Clock clock = src.withZone(ZoneId.of("UTC"));
        check(clock != null && clock.instant().equals(fixed), "withZone instant");
        check(ZoneId.of("UTC").equals(clock.getZone()), "withZone zone");

        check(InstantSource.offset(src, Duration.ofSeconds(5)).instant().getEpochSecond() == 105, "offset +5s");
        check(InstantSource.offset(src, Duration.ofSeconds(-5)).instant().getEpochSecond() == 95, "offset -5s");
        check(InstantSource.tick(src, Duration.ofSeconds(10)).instant().getEpochSecond() == 100, "tick 10s");
        check(InstantSource.tick(src, Duration.ofSeconds(10)).millis() == 100000L, "tick millis");

        // lambda-backed custom source exercising the abstract instant() and default millis()/withZone()
        InstantSource lambda = () -> Instant.ofEpochSecond(7);
        check(lambda.instant().getEpochSecond() == 7, "lambda instant");
        check(lambda.millis() == 7000L, "lambda millis");
        Clock lc = lambda.withZone(ZoneId.of("UTC"));
        check(lc.instant().getEpochSecond() == 7, "lambda withZone instant");
        check(ZoneId.of("UTC").equals(lc.getZone()), "lambda withZone zone");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepInstantSource();
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


    static void testDeepInstantSource() {
        java.time.Instant f = java.time.Instant.ofEpochSecond(100);
        InstantSource src = InstantSource.fixed(f);
        xeq(src.instant().toString(), "1970-01-01T00:01:40Z", "fixed instant");
        check(src.millis() == 100000L, "fixed millis");
        check(InstantSource.fixed(java.time.Instant.ofEpochSecond(100, 500)).millis() == 100000L, "fixed nanos truncated in millis");
        xeq(InstantSource.offset(src, java.time.Duration.ofSeconds(5)).instant().toString(),
            "1970-01-01T00:01:45Z", "offset adds duration");
        xeq(InstantSource.offset(src, java.time.Duration.ofSeconds(-5)).instant().toString(),
            "1970-01-01T00:01:35Z", "negative offset subtracts");
        xeq(InstantSource.tick(src, java.time.Duration.ofSeconds(15)).instant().toString(),
            "1970-01-01T00:01:30Z", "tick floors to 15s");
        xeq(InstantSource.tick(src, java.time.Duration.ofMinutes(1)).instant().toString(),
            "1970-01-01T00:01:00Z", "tick floors to minute");
        check(InstantSource.tick(src, java.time.Duration.ofSeconds(15)).instant().equals(
            InstantSource.tick(InstantSource.tick(src, java.time.Duration.ofSeconds(15)), java.time.Duration.ofSeconds(15)).instant()),
            "tick idempotent");
        xeq(InstantSource.tick(src, java.time.Duration.ofNanos(1)).instant().toString(),
            "1970-01-01T00:01:40Z", "tick 1ns no-op");
        java.time.Clock c = src.withZone(java.time.ZoneId.of("UTC"));
        check(c.instant().equals(f), "withZone keeps instant");
        check(c.getZone().equals(java.time.ZoneId.of("UTC")), "withZone zone");
        check(c.getClass().getName().equals("java.time.Clock$FixedClock"), "withZone returns FixedClock");
        check(InstantSource.fixed(f).equals(InstantSource.fixed(f)), "fixed equals");
        check(java.time.Clock.fixed(f, java.time.ZoneId.of("UTC")) instanceof InstantSource, "Clock is an InstantSource");
        check(InstantSource.system().millis() > 0, "system millis positive");
        check(InstantSource.system().instant() != null, "system instant nonnull");
        InstantSource lambda = () -> java.time.Instant.ofEpochSecond(42);
        xeq(lambda.instant().toString(), "1970-01-01T00:00:42Z", "lambda instant");
        check(lambda.millis() == 42000L, "lambda millis default");
        xact(() -> InstantSource.fixed(null), NullPointerException.class, "fixedInstant", "fixed null");
        xact(() -> InstantSource.offset(null, java.time.Duration.ZERO), NullPointerException.class,
            "baseSource", "offset null source");
        xact(() -> InstantSource.offset(src, null), NullPointerException.class, "offsetDuration", "offset null duration");
        xact(() -> InstantSource.tick(null, java.time.Duration.ofSeconds(1)), NullPointerException.class,
            "baseSource", "tick null source");
        xact(() -> InstantSource.tick(src, null), NullPointerException.class, "tickDuration", "tick null duration");
        xact(() -> InstantSource.tick(src, java.time.Duration.ofSeconds(-1)), IllegalArgumentException.class,
            "Tick duration must not be negative", "tick negative");
        xact(() -> src.withZone(null), NullPointerException.class,
            "Cannot invoke \"java.time.ZoneId.equals(Object)\" because \"zone\" is null", "withZone null");
    }

}
