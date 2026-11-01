/*
 * @test
 * @summary java.util.Locale.FilteringMode enum conformance.
 * @run main FilteringModeTest
 */

import java.util.Locale;

public class FilteringModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFilteringMode() {
        check(Locale.FilteringMode.values().length == 5, "5 constants");
        check(Locale.FilteringMode.valueOf("EXTENDED_FILTERING")
              == Locale.FilteringMode.EXTENDED_FILTERING, "valueOf");
        check(Locale.FilteringMode.AUTOSELECT_FILTERING
              != Locale.FilteringMode.REJECT_EXTENDED_RANGES, "distinct constants");
    }

    public static void main(String[] args) {
        testFilteringMode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
