/*
 * @test
 * @summary javax.swing.border.MatteBorder conformance: matte (headless-safe).
 * @run main MatteBorderTest
 */

import javax.swing.border.MatteBorder;

public class MatteBorderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        MatteBorder b = new MatteBorder(1, 2, 3, 4, java.awt.Color.RED);
        java.awt.Insets i = b.getBorderInsets(null);
        check(i.top == 1 && i.left == 2 && i.bottom == 3 && i.right == 4, "insets");
        check(java.awt.Color.RED.equals(b.getMatteColor()), "color");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
