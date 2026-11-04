/*
 * @test
 * @summary java.util.Locale.Category enum conformance.
 * @run main CategoryTest
 */

import java.util.Locale;

public class CategoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testCategory() {
        check(Locale.Category.values().length == 2, "2 constants");
        check(Locale.Category.valueOf("DISPLAY") == Locale.Category.DISPLAY, "valueOf");
        check(Locale.Category.DISPLAY != Locale.Category.FORMAT, "distinct constants");
    }


    // ===================== 契约深化：完整常量表/类别默认区独立生效 =====================
    static void testCategoryContracts() {
        Locale.Category[] vals = Locale.Category.values();
        check(vals.length == 2, "exactly 2 constants");
        check(Locale.Category.valueOf("DISPLAY") == Locale.Category.DISPLAY
              && Locale.Category.valueOf("FORMAT") == Locale.Category.FORMAT,
              "valueOf both constants");
        check("DISPLAY".equals(Locale.Category.DISPLAY.name())
              && "FORMAT".equals(Locale.Category.FORMAT.name()), "constant names");
        Locale oldDisplay = Locale.getDefault(Locale.Category.DISPLAY);
        try {
            Locale.setDefault(Locale.Category.DISPLAY, Locale.FRENCH);
            check(Locale.FRENCH.equals(Locale.getDefault(Locale.Category.DISPLAY)),
                  "DISPLAY default settable");
        } finally {
            Locale.setDefault(Locale.Category.DISPLAY, oldDisplay);
        }
    }

    public static void main(String[] args) {
        testCategory();
        testCategoryContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
