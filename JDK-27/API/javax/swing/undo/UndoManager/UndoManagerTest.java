/*
 * @test
 * @summary javax.swing.undo.UndoManager conformance: white-box structure + black-box behavior.
 * @run main UndoManagerTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.swing.UIManager;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

public class UndoManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Records undo/redo call order; significant flag drives UndoManager grouping.
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

    // Exposes protected state and protected undoTo/redoTo/lastEdit for observation.
    static class ExposedUndoManager extends UndoManager {
        int editCount() { return edits.size(); }
        UndoableEdit lastEditExposed() { return lastEdit(); }
        void undoToExposed(UndoableEdit e) { undoTo(e); }
        void redoToExposed(UndoableEdit e) { redoTo(e); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.undo.UndoManager");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.undo.CompoundEdit"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.event.UndoableEditListener"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        String undoText = UIManager.getString("AbstractUndoableEdit.undoText");
        String redoText = UIManager.getString("AbstractUndoableEdit.redoText");

        // Default state.
        ExposedUndoManager m = new ExposedUndoManager();
        check(m.getLimit() == 100, "default limit 100");
        check(!m.canUndo(), "empty manager cannot undo");
        check(!m.canRedo(), "empty manager cannot redo");
        check(!m.canUndoOrRedo(), "empty manager cannot undoOrRedo");

        // Significant/insignificant grouping: A b c D (matches documented example).
        List<String> log = new ArrayList<String>();
        RecordEdit A = new RecordEdit("A", log, true);
        RecordEdit b = new RecordEdit("b", log, false);
        RecordEdit c = new RecordEdit("c", log, false);
        RecordEdit D = new RecordEdit("D", log, true);
        check(m.addEdit(A), "addEdit A returns true");
        check(m.addEdit(b), "addEdit b returns true");
        check(m.addEdit(c), "addEdit c returns true");
        check(m.addEdit(D), "addEdit D returns true");
        check(m.editCount() == 4, "four edits held");
        check(m.canUndo(), "canUndo after adding significant edits");
        check(!m.canRedo(), "cannot redo before undo");

        // undo undoes only the last significant edit (D).
        m.undo();
        check("[undo:D]".equals(log.toString()), "undo undoes last significant edit only");
        check(m.canRedo(), "canRedo after undo");
        check(m.canUndo(), "canUndo still true");

        // redo redoes D.
        m.redo();
        check("[undo:D, redo:D]".equals(log.toString()), "redo redoes last significant edit");
        check(!m.canRedo(), "cannot redo after redoing to top");

        // Second undo undoes c, b, A in reverse order down to the previous significant edit.
        m.undo();
        m.undo();
        check("[undo:D, redo:D, undo:D, undo:c, undo:b, undo:A]".equals(log.toString()),
                "undo groups insignificant edits with previous significant edit");
        check(!m.canUndo(), "cannot undo at start");

        // redo first redoes only the next significant edit (A)...
        m.redo();
        check("[undo:D, redo:D, undo:D, undo:c, undo:b, undo:A, redo:A]".equals(log.toString()),
                "redo redoes only the next significant edit (A)");
        // ...then the next redo redoes the remaining edits up to the next significant edit (D).
        m.redo();
        check("[undo:D, redo:D, undo:D, undo:c, undo:b, undo:A, redo:A, redo:b, redo:c, redo:D]".equals(log.toString()),
                "second redo redoes remaining edits in forward order");

        // undoOrRedo picks redo when edits are undone, undo otherwise.
        m.undo(); // now index=3, undone D
        check(m.canUndoOrRedo(), "canUndoOrRedo when edits undone");
        m.undoOrRedo(); // redoes D
        check("[undo:D, redo:D, undo:D, undo:c, undo:b, undo:A, redo:A, redo:b, redo:c, redo:D, undo:D, redo:D]".equals(log.toString()),
                "undoOrRedo redoes when edits undone");
        m.undoOrRedo(); // undoes D again
        check("[undo:D, redo:D, undo:D, undo:c, undo:b, undo:A, redo:A, redo:b, redo:c, redo:D, undo:D, redo:D, undo:D]".equals(log.toString()),
                "undoOrRedo undoes when at end");

        // Presentation names.
        String undoName = m.getUndoOrRedoPresentationName();
        check(undoName.equals(redoText + " D"), "undoOrRedo name after undo == redo text + D");
        String undoPres = m.getUndoPresentationName();
        check(undoPres.equals(undoText + " A"), "undo name points at next significant edit (A)");
        String redoPres = m.getRedoPresentationName();
        check(redoPres.equals(redoText + " D"), "redo name points at D");

        // Empty-manager presentation names fall back to UIManager text.
        UndoManager empty = new UndoManager();
        check(empty.getUndoPresentationName().equals(undoText), "empty undo name == UIManager undo text");
        check(empty.getRedoPresentationName().equals(redoText), "empty redo name == UIManager redo text");

        // Edge exceptions: undo/redo on empty manager.
        try {
            empty.undo();
            check(false, "undo on empty should throw");
        } catch (CannotUndoException expected) {
            check(true, "undo on empty throws CannotUndoException");
        }
        try {
            empty.redo();
            check(false, "redo on empty should throw");
        } catch (CannotRedoException expected) {
            check(true, "redo on empty throws CannotRedoException");
        }

        // setLimit/getLimit and trimming (dies dropped edits).
        ExposedUndoManager limited = new ExposedUndoManager();
        limited.setLimit(2);
        check(limited.getLimit() == 2, "setLimit reflected by getLimit");
        RecordEdit lA = new RecordEdit("lA", log, true);
        RecordEdit lB = new RecordEdit("lB", log, true);
        RecordEdit lC = new RecordEdit("lC", log, true);
        limited.addEdit(lA);
        limited.addEdit(lB);
        limited.addEdit(lC);
        check(limited.editCount() == 2, "limit trims edit count to 2");
        check(!lA.canUndo(), "trimmed-away edit received die");

        // Unlimited limit (negative) disables trimming.
        ExposedUndoManager unlimited = new ExposedUndoManager();
        unlimited.setLimit(-1);
        check(unlimited.getLimit() == -1, "negative limit means unlimited");
        for (int i = 0; i < 150; i++) {
            unlimited.addEdit(new RecordEdit("e" + i, log, true));
        }
        check(unlimited.editCount() == 150, "unlimited manager keeps all edits");

        // discardAllEdits empties the manager and dies the edits.
        RecordEdit keep = new RecordEdit("keep", log, true);
        unlimited.addEdit(keep);
        unlimited.discardAllEdits();
        check(unlimited.editCount() == 0, "discardAllEdits empties manager");
        check(!unlimited.canUndo(), "discardAllEdits leaves nothing to undo");
        check(!keep.canUndo(), "discardAllEdits dies contained edits");

        // undoTo/redoTo (protected, exposed) update index and undo/redo the target edit.
        List<String> directLog = new ArrayList<String>();
        ExposedUndoManager direct = new ExposedUndoManager();
        RecordEdit pA = new RecordEdit("pA", directLog, true);
        RecordEdit pB = new RecordEdit("pB", directLog, true);
        direct.addEdit(pA);
        direct.addEdit(pB);
        direct.undoToExposed(pB);
        check("[undo:pB]".equals(directLog.toString()), "undoTo(B) undoes B only");
        check(direct.canRedo(), "canRedo after undoTo(B)");
        direct.redoToExposed(pB);
        check("[undo:pB, redo:pB]".equals(directLog.toString()), "redoTo(B) redoes B");
        check(!direct.canRedo(), "cannot redo after redoTo(B) to top");
        // undoTo(A) undoes B then A down to A.
        direct.undoToExposed(pA);
        check("[undo:pB, redo:pB, undo:pB, undo:pA]".equals(directLog.toString()),
                "undoTo(A) undoes B then A");

        // undoableEditHappened adds the event's edit.
        UndoManager viaEvent = new UndoManager();
        RecordEdit ev = new RecordEdit("ev", log, true);
        viaEvent.undoableEditHappened(new UndoableEditEvent(viaEvent, ev));
        check(viaEvent.canUndo(), "undoableEditHappened adds edit");

        // end(): after end, addEdit returns false and setLimit throws.
        UndoManager ended = new UndoManager();
        ended.addEdit(new RecordEdit("x", log, true));
        ended.end();
        check(!ended.addEdit(new RecordEdit("y", log, true)), "addEdit after end returns false");
        try {
            ended.setLimit(5);
            check(false, "setLimit after end should throw");
        } catch (RuntimeException expected) {
            check(true, "setLimit after end throws RuntimeException");
        }

        // lastEdit() exposes the most recently added edit (protected, exposed).
        check(m.lastEditExposed() == D, "lastEdit returns most recent edit");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        UndoManager m = new UndoManager();
        check(m.getLimit() == 100, "limit");
        check(m.isInProgress(), "in progress");
        check(m.getPresentationName() != null, "presentation");
    }



    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
