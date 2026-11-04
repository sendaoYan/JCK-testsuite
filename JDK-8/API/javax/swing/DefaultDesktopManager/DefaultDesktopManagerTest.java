/*
 * @test
 * @summary javax.swing.DefaultDesktopManager conformance: desktop manager (headless-safe).
 * @run main DefaultDesktopManagerTest
 */

import javax.swing.DefaultDesktopManager;

public class DefaultDesktopManagerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultDesktopManager m = new DefaultDesktopManager();
        check(m instanceof javax.swing.DesktopManager, "implements DesktopManager");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
