/*
 * @test
 * @summary javax.swing.event.UndoableEditEvent conformance: event (headless-safe).
 * @run main UndoableEditEventTest
 */

import javax.swing.event.UndoableEditEvent;

public class UndoableEditEventTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        UndoableEditEvent e = new UndoableEditEvent("src", null);
        check("src".equals(e.getSource()) && e.getEdit() == null, "source/edit");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
