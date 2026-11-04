/*
 * @test
 * @summary javax.swing.event.ListDataEvent conformance: event (headless-safe).
 * @run main ListDataEventTest
 */

import javax.swing.event.ListDataEvent;

public class ListDataEventTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        ListDataEvent e = new ListDataEvent("src", ListDataEvent.INTERVAL_ADDED, 0, 2);
        check(e.getIndex0() == 0 && e.getIndex1() == 2, "indices");
        check(e.getType() == ListDataEvent.INTERVAL_ADDED, "type");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
