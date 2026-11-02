/*
 * @test
 * @summary javax.swing.UIDefaults conformance: defaults map (headless-safe).
 * @run main UIDefaultsTest
 */

import javax.swing.UIDefaults;

public class UIDefaultsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        UIDefaults d = new UIDefaults();
        d.put("k", "v");
        check("v".equals(d.get("k")), "put/get");
        check(d.size() == 1, "size");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
