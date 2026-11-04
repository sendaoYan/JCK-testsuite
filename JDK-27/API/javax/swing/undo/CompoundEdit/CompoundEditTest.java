/*
 * @test
 * @summary javax.swing.undo.CompoundEdit conformance: white-box structure + black-box behavior.
 * @run main CompoundEditTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

public class CompoundEditTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Records undo/redo call order so multi-edit ordering can be asserted.
    static class RecordEdit extends AbstractUndoableEdit {
        final String name;
        final List<String> log;
        final boolean significant;
        RecordEdit(String name, List<String> log, boolean significant) {
            this.name = name; this.log = log; this.significant = significant;
        }
        public void undo() { super.undo(); log.add("undo:" + name); }
        public void redo() { super.redo(); log.add("redo:" + name); }
        public boolean isSignificant() { return significant; }
        public String getPresentationName() { return name; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.undo.CompoundEdit");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.undo.AbstractUndoableEdit"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        List<String> log = new ArrayList<String>();

        CompoundEdit ce = new CompoundEdit();
        check(ce.isInProgress(), "new compound edit is in progress");
        check(!ce.canUndo(), "in-progress compound cannot undo");
        check(!ce.canRedo(), "in-progress compound cannot redo");
        check(!ce.isSignificant(), "empty compound is not significant");

        RecordEdit e1 = new RecordEdit("e1", log, true);
        RecordEdit e2 = new RecordEdit("e2", log, false);
        RecordEdit e3 = new RecordEdit("e3", log, true);

        check(ce.addEdit(e1), "addEdit during progress returns true");
        check(ce.addEdit(e2), "addEdit during progress returns true");
        check(ce.isSignificant(), "compound significant when any subedit significant");
        check("e2".equals(ce.getPresentationName()), "presentation name from last edit");
        check(ce.isInProgress(), "still in progress before end");

        ce.end();
        check(!ce.isInProgress(), "end stops progress");
        check(ce.canUndo(), "ended compound can undo");
        check(!ce.canRedo(), "ended compound cannot redo (nothing undone)");

        // undo undoes subedits in reverse order.
        ce.undo();
        check("[undo:e2, undo:e1]".equals(log.toString()), "undo reverses subedit order");
        check(!ce.canUndo(), "after undo cannot undo again");
        check(ce.canRedo(), "after undo can redo");

        // redo redoes subedits in forward order.
        ce.redo();
        check("[undo:e2, undo:e1, redo:e1, redo:e2]".equals(log.toString()), "redo preserves subedit order");
        check(ce.canUndo(), "after redo can undo again");

        // addEdit after end is refused.
        check(!ce.addEdit(e3), "addEdit after end returns false");

        // die kills subedits (reverse order) and the compound itself.
        CompoundEdit dying = new CompoundEdit();
        RecordEdit d1 = new RecordEdit("d1", log, true);
        RecordEdit d2 = new RecordEdit("d2", log, true);
        dying.addEdit(d1);
        dying.addEdit(d2);
        dying.end();
        dying.die();
        check(!dying.canUndo() && !dying.canRedo(), "died compound cannot undo/redo");
        check(!d1.canUndo() && !d2.canUndo(), "die propagated to subedits");

        // Undo/redo edge exceptions: double undo throws, redo at top throws.
        ce.undo(); // undo e2, e1 again
        check(!ce.canUndo(), "after second undo cannot undo again");
        try {
            ce.undo();
            check(false, "double undo should throw");
        } catch (CannotUndoException expected) {
            check(true, "double undo throws CannotUndoException");
        }
        ce.redo(); // back to done state
        check(!ce.canRedo(), "at top cannot redo");
        try {
            ce.redo();
            check(false, "redo at top should throw");
        } catch (CannotRedoException expected) {
            check(true, "redo at top throws CannotRedoException");
        }

        // Empty compound delegates presentation names to the superclass (empty name).
        CompoundEdit edge = new CompoundEdit();
        check("".equals(edge.getPresentationName()), "empty compound presentation name empty");
        check(edge.isInProgress(), "empty compound still in progress");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
