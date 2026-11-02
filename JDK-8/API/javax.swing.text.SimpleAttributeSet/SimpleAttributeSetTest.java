/*
 * @test
 * @summary javax.swing.text.SimpleAttributeSet conformance: attributes (headless-safe).
 * @run main SimpleAttributeSetTest
 */

import javax.swing.text.SimpleAttributeSet;

public class SimpleAttributeSetTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        SimpleAttributeSet a = new SimpleAttributeSet();
        check(a.isEmpty(), "empty");
        a.addAttribute("k", "v");
        check("v".equals(a.getAttribute("k")), "get");
        check(a.getAttributeCount() == 1, "count");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
