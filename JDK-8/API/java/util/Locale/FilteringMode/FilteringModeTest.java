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


    // ===================== 契约深化：五种模式全名与 valueOf 往返 =====================
    static void testFilteringModeContracts() {
        String[] names = {"AUTOSELECT_FILTERING", "EXTENDED_FILTERING",
                          "IGNORE_EXTENDED_RANGES", "MAP_EXTENDED_RANGES", "REJECT_EXTENDED_RANGES"};
        check(Locale.FilteringMode.values().length == 5, "five constants");
        for (String n : names) {
            check(Locale.FilteringMode.valueOf(n).name().equals(n), "valueOf " + n);
        }
        check(Locale.FilteringMode.MAP_EXTENDED_RANGES != Locale.FilteringMode.REJECT_EXTENDED_RANGES,
              "distinct constants");
    }

    public static void main(String[] args) {
        testFilteringMode();
        testFilteringModeContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
