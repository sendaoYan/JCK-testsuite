/*
 * @test
 * @summary javax.swing.border.EmptyBorder conformance: empty insets (headless-safe).
 * @run main EmptyBorderTest
 */

import javax.swing.border.EmptyBorder;

public class EmptyBorderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        EmptyBorder b = new EmptyBorder(1, 2, 3, 4);
        java.awt.Insets i = b.getBorderInsets(null);
        check(i.top == 1 && i.left == 2 && i.bottom == 3 && i.right == 4, "insets");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
