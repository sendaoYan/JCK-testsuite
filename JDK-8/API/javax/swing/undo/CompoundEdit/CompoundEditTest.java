/*
 * @test
 * @summary javax.swing.undo.CompoundEdit conformance: compound edit (headless-safe).
 * @run main CompoundEditTest
 */

import javax.swing.undo.CompoundEdit;

public class CompoundEditTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        CompoundEdit e = new CompoundEdit();
        check(e.isInProgress(), "in progress");
        e.end();
        check(!e.isInProgress(), "ended");
        check(e.canUndo() && !e.canRedo(), "undoable, not redoable");
        check(e.getPresentationName() != null, "presentation");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
