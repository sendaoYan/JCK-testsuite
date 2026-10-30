/*
 * @test
 * @summary java.time.Instant API conformance: ofEpochSecond/ofEpochMilli, getEpochSecond/
 *          getNano, plusSeconds, isBefore, and compareTo.
 * @run main InstantTest
 */

import java.time.Instant;

public class InstantTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInstant() {
        Instant i = Instant.ofEpochSecond(60, 500_000_000);
        check(i.getEpochSecond() == 60 && i.getNano() == 500_000_000, "ofEpochSecond/getters");
        check(Instant.ofEpochMilli(0).getEpochSecond() == 0, "ofEpochMilli");
        check(i.plusSeconds(1).getEpochSecond() == 61, "plusSeconds");
        check(Instant.ofEpochSecond(0).isBefore(Instant.ofEpochSecond(1)), "isBefore");
        check(Instant.ofEpochSecond(5).compareTo(Instant.ofEpochSecond(5)) == 0, "compareTo");
    }

    public static void main(String[] args) {
        testInstant();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
