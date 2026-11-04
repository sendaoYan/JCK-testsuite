/*
 * @test
 * @summary java.time.Duration API conformance: ofSeconds/ofMillis, getSeconds/getNano, plus,
 *          isZero/isNegative, and toMillis.
 * @run main DurationTest
 */

import java.time.Duration;

public class DurationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDuration() {
        Duration d = Duration.ofSeconds(90, 500_000_000);
        check(d.getSeconds() == 90 && d.getNano() == 500_000_000, "ofSeconds/getters");
        check(Duration.ofMillis(1500).getSeconds() == 1
              && Duration.ofMillis(1500).getNano() == 500_000_000, "ofMillis");
        check(d.plus(Duration.ofSeconds(10)).getSeconds() == 100, "plus");
        check(Duration.ZERO.isZero() && !d.isZero(), "isZero");
        check(Duration.ofSeconds(-5).isNegative(), "isNegative");
        check(Duration.ofSeconds(2).toMillis() == 2000, "toMillis");
    }

    public static void main(String[] args) {
        testDuration();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
