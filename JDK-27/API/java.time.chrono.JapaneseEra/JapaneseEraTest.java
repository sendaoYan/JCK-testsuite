/*
 * @test
 * @summary java.time.chrono.JapaneseEra conformance: era values and valueOf.
 * @run main JapaneseEraTest
 */

import java.time.chrono.JapaneseEra;

public class JapaneseEraTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testJapaneseEra() {
        check(JapaneseEra.MEIJI.getValue() == -1, "MEIJI value");
        check(JapaneseEra.TAISHO.getValue() == 0, "TAISHO value");
        check(JapaneseEra.SHOWA.getValue() == 1, "SHOWA value");
        check(JapaneseEra.HEISEI.getValue() == 2, "HEISEI value");
        check(JapaneseEra.of(1) == JapaneseEra.SHOWA, "of(1) SHOWA");
    }

    public static void main(String[] args) {
        testJapaneseEra();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
