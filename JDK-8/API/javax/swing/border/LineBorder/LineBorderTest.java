/*
 * @test
 * @summary javax.swing.border.LineBorder conformance: line (headless-safe).
 * @run main LineBorderTest
 */

import javax.swing.border.LineBorder;

public class LineBorderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        LineBorder b = new LineBorder(java.awt.Color.BLACK, 2);
        check(java.awt.Color.BLACK.equals(b.getLineColor()), "color");
        check(b.getThickness() == 2, "thickness");
        check(b.getBorderInsets(null) != null, "insets");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
