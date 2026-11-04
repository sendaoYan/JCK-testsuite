/*
 * @test
 * @summary javax.swing.event.ChangeEvent conformance: event (headless-safe).
 * @run main ChangeEventTest
 */

import javax.swing.event.ChangeEvent;

public class ChangeEventTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        ChangeEvent e = new ChangeEvent("src");
        check("src".equals(e.getSource()), "source");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
