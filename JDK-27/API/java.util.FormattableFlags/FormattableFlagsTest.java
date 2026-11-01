/*
 * @test
 * @summary java.util.FormattableFlags API conformance: flag constants.
 * @run main FormattableFlagsTest
 */

import java.util.FormattableFlags;

public class FormattableFlagsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFormattableFlags() {
        check(FormattableFlags.LEFT_JUSTIFY == 1, "LEFT_JUSTIFY");
        check(FormattableFlags.UPPERCASE == 2, "UPPERCASE");
        check(FormattableFlags.ALTERNATE == 4, "ALTERNATE");
    }

    public static void main(String[] args) {
        testFormattableFlags();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
