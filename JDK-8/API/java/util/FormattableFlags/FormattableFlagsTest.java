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


    // ===================== 契约深化：位值组合/Formattable 回调对齐 =====================
    static void testFlagsContracts() {
        check(FormattableFlags.LEFT_JUSTIFY == 1 && FormattableFlags.UPPERCASE == 2
              && FormattableFlags.ALTERNATE == 4, "flag bit values 1/2/4");
        check((FormattableFlags.LEFT_JUSTIFY | FormattableFlags.UPPERCASE
              | FormattableFlags.ALTERNATE) == 7, "bitmask-combinable");
        final int[] got = { -1 };
        class F implements java.util.Formattable {
            public void formatTo(java.util.Formatter f, int flags, int width, int precision) {
                got[0] = flags;
                f.format("x");
            }
        }
        String.format("%#-4S", new F());
        check(got[0] == 7, "%#-4S delivers all three flags (LEFT|UPPER|ALT)");
        String.format("%s", new F());
        check(got[0] == 0, "plain %s delivers no flags");
        String.format("%-10.2S", new F());
        check(got[0] == (FormattableFlags.LEFT_JUSTIFY | FormattableFlags.UPPERCASE),
              "%-10.2S delivers LEFT_JUSTIFY|UPPERCASE");
    }

    public static void main(String[] args) {
        testFormattableFlags();
        testFlagsContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
