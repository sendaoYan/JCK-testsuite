/*
 * @test
 * @summary javax.swing.border.BevelBorder conformance: bevel insets (headless-safe).
 * @run main BevelBorderTest
 */

import javax.swing.border.BevelBorder;

public class BevelBorderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        BevelBorder b = new BevelBorder(BevelBorder.RAISED);
        check(b.getBevelType() == BevelBorder.RAISED, "type");
        check(b.getBorderInsets(null) != null, "insets");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
