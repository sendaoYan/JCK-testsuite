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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
