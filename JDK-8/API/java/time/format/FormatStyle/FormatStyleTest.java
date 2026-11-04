/*
 * @test
 * @summary java.time.format.FormatStyle API conformance: constants and valueOf.
 * @run main FormatStyleTest
 */

import java.time.format.FormatStyle;

public class FormatStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFormatStyle() {
        check(FormatStyle.valueOf("FULL") == FormatStyle.FULL, "valueOf FULL");
        check(FormatStyle.valueOf("MEDIUM") == FormatStyle.MEDIUM, "valueOf MEDIUM");
        check(FormatStyle.values().length == 4, "four styles");
    }

    public static void main(String[] args) {
        testFormatStyle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
