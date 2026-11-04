/*
 * @test
 * @summary javax.swing.border.CompoundBorder conformance: compound (headless-safe).
 * @run main CompoundBorderTest
 */

import javax.swing.border.CompoundBorder;

public class CompoundBorderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        CompoundBorder b = new CompoundBorder();
        check(b.getInsideBorder() == null && b.getOutsideBorder() == null, "empty");
        check(b.getBorderInsets(null) != null, "insets");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
