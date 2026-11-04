/*
 * @test
 * @summary javax.swing.event.PopupMenuEvent conformance: event (headless-safe).
 * @run main PopupMenuEventTest
 */

import javax.swing.event.PopupMenuEvent;

public class PopupMenuEventTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        PopupMenuEvent e = new PopupMenuEvent("src");
        check("src".equals(e.getSource()), "source");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
