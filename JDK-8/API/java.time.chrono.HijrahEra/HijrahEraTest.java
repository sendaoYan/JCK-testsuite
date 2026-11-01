/*
 * @test
 * @summary java.time.chrono.HijrahEra conformance: AH value.
 * @run main HijrahEraTest
 */

import java.time.chrono.HijrahEra;

public class HijrahEraTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testHijrahEra() {
        check(HijrahEra.AH.getValue() == 1, "AH value");
        check(HijrahEra.valueOf("AH") == HijrahEra.AH, "valueOf AH");
        check(HijrahEra.values().length == 1, "one value");
        check(HijrahEra.of(1) == HijrahEra.AH, "of(1) AH");
    }

    public static void main(String[] args) {
        testHijrahEra();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
