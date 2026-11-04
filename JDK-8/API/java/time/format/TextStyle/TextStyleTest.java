/*
 * @test
 * @summary java.time.format.TextStyle API conformance: standalone flag and names.
 * @run main TextStyleTest
 */

import java.time.format.TextStyle;

public class TextStyleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTextStyle() {
        check(!TextStyle.FULL.isStandalone(), "FULL not standalone");
        check(TextStyle.FULL_STANDALONE.isStandalone(), "FULL_STANDALONE standalone");
        check(TextStyle.SHORT_STANDALONE.isStandalone(), "SHORT_STANDALONE standalone");
        check("NARROW".equals(TextStyle.NARROW.name()), "NARROW name");
    }

    public static void main(String[] args) {
        testTextStyle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
