/*
 * @test
 * @summary javax.swing.undo.UndoManager conformance: undo manager (headless-safe).
 * @run main UndoManagerTest
 */

import javax.swing.undo.UndoManager;

public class UndoManagerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        UndoManager m = new UndoManager();
        check(m.getLimit() == 100, "limit");
        check(m.isInProgress(), "in progress");
        check(m.getPresentationName() != null, "presentation");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
