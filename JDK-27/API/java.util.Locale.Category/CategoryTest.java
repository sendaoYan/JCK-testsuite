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

    public static void main(String[] args) {
        testCategory();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
