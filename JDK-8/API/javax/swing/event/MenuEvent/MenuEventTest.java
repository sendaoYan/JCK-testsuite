/*
 * @test
 * @summary javax.swing.event.MenuEvent conformance: event (headless-safe).
 * @run main MenuEventTest
 */

import javax.swing.event.MenuEvent;

public class MenuEventTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        MenuEvent e = new MenuEvent("src");
        check("src".equals(e.getSource()), "source");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
