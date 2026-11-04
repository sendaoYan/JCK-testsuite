/*
 * @test
 * @summary javax.swing.border.StrokeBorder conformance: stroke (headless-safe).
 * @run main StrokeBorderTest
 */

import javax.swing.border.StrokeBorder;

public class StrokeBorderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        StrokeBorder b = new StrokeBorder(new java.awt.BasicStroke(2f));
        check(b.getStroke() != null, "stroke");
        check(b.getBorderInsets(null) != null, "insets");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
