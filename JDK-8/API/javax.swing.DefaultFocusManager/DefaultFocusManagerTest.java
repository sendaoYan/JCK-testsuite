/*
 * @test
 * @summary javax.swing.DefaultFocusManager conformance: focus manager (headless-safe).
 * @run main DefaultFocusManagerTest
 */

import javax.swing.DefaultFocusManager;

public class DefaultFocusManagerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultFocusManager m = new DefaultFocusManager();
        check(m instanceof javax.swing.FocusManager, "extends FocusManager");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
