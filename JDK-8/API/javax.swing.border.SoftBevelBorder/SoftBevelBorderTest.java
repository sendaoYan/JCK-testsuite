/*
 * @test
 * @summary javax.swing.border.SoftBevelBorder conformance: soft bevel (headless-safe).
 * @run main SoftBevelBorderTest
 */

import javax.swing.border.SoftBevelBorder;

public class SoftBevelBorderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        SoftBevelBorder b = new SoftBevelBorder(SoftBevelBorder.RAISED);
        check(b.getBevelType() == SoftBevelBorder.RAISED, "type");
        check(b.getBorderInsets(null) != null, "insets");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
