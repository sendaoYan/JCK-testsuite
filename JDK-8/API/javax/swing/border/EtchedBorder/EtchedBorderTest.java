/*
 * @test
 * @summary javax.swing.border.EtchedBorder conformance: etched (headless-safe).
 * @run main EtchedBorderTest
 */

import javax.swing.border.EtchedBorder;

public class EtchedBorderTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        EtchedBorder b = new EtchedBorder();
        check(b.getEtchType() == EtchedBorder.LOWERED, "type");
        check(b.getBorderInsets(null) != null, "insets");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
