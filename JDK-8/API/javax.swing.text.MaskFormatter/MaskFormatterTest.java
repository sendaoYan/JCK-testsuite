/*
 * @test
 * @summary javax.swing.text.MaskFormatter conformance: mask formatter (headless-safe).
 * @run main MaskFormatterTest
 */

import javax.swing.text.MaskFormatter;

public class MaskFormatterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        MaskFormatter f = new MaskFormatter();
        check(f.getMask() == null, "no mask");
        f.setMask("###");
        check("###".equals(f.getMask()), "mask");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
