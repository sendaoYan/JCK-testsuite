/*
 * @test
 * @summary javax.swing.event.ListSelectionEvent conformance: event (headless-safe).
 * @run main ListSelectionEventTest
 */

import javax.swing.event.ListSelectionEvent;

public class ListSelectionEventTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        ListSelectionEvent e = new ListSelectionEvent("src", 1, 5, false);
        check(e.getFirstIndex() == 1 && e.getLastIndex() == 5, "indices");
        check(!e.getValueIsAdjusting(), "adjusting");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
