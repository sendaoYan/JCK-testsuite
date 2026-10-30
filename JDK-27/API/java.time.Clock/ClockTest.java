/*
 * @test
 * @summary java.time.Clock API conformance: fixed instant/getZone/millis, and
 *          systemDefaultZone.
 * @run main ClockTest
 */

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
