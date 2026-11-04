/*
 * @test
 * @summary javax.swing.undo.AbstractUndoableEdit conformance: white-box structure + black-box behavior.
 * @run main AbstractUndoableEditTest
 */

import java.lang.reflect.Modifier;
import javax.swing.UIManager;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public class AbstractUndoableEditTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // A subclass that exposes a non-empty presentation name so that the
    // undo/redo presentation-name composition can be observed.
    static class NamedEdit extends AbstractUndoableEdit {
        final String name;
        NamedEdit(String name) { this.name = name; }
        public String getPresentationName() { return name; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.undo.AbstractUndoableEdit");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.undo.UndoableEdit"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AbstractUndoableEdit e = new AbstractUndoableEdit();
        check(e.canUndo(), "new edit canUndo");
        check(!e.canRedo(), "new edit cannot redo");
        check(e.isSignificant(), "default isSignificant true");
        check("".equals(e.getPresentationName()), "default presentation name empty");

        // undo flips hasBeenDone, redo flips it back.
        e.undo();
        check(!e.canUndo(), "after undo cannot undo again");
        check(e.canRedo(), "after undo can redo");
        e.redo();
        check(e.canUndo(), "after redo can undo again");
        check(!e.canRedo(), "after redo cannot redo");

        // Undoing twice throws CannotUndoException; redoing at top throws CannotRedoException.
        e.undo();
        try {
            e.undo();
            check(false, "double undo should throw");
        } catch (CannotUndoException expected) {
            check(true, "double undo throws CannotUndoException");
        }
        e.redo();
        try {
            e.redo();
            check(false, "redo at top should throw");
        } catch (CannotRedoException expected) {
            check(true, "redo at top throws CannotRedoException");
        }

        // addEdit / replaceEdit default to false.
        check(!e.addEdit(new AbstractUndoableEdit()), "default addEdit false");
        check(!e.replaceEdit(new AbstractUndoableEdit()), "default replaceEdit false");

        // die is one-way: both undo and redo become impossible.
        AbstractUndoableEdit dead = new AbstractUndoableEdit();
        dead.die();
        check(!dead.canUndo(), "dead edit cannot undo");
        check(!dead.canRedo(), "dead edit cannot redo");
        try {
            dead.undo();
            check(false, "dead undo should throw");
        } catch (CannotUndoException expected) {
            check(true, "dead undo throws CannotUndoException");
        }
        try {
            dead.redo();
            check(false, "dead redo should throw");
        } catch (CannotRedoException expected) {
            check(true, "dead redo throws CannotRedoException");
        }

        // Presentation-name composition for an empty name: undo/redo text alone.
        String undoText = UIManager.getString("AbstractUndoableEdit.undoText");
        String redoText = UIManager.getString("AbstractUndoableEdit.redoText");
        check(undoText.equals(e.getUndoPresentationName()), "empty-name undo presentation == undo text");
        check(redoText.equals(e.getRedoPresentationName()), "empty-name redo presentation == redo text");

        // Presentation-name composition for a non-empty name: "<undoText> <name>".
        UndoableEdit named = new NamedEdit("Cut");
        check("Cut".equals(named.getPresentationName()), "named presentation name");
        check((undoText + " Cut").equals(named.getUndoPresentationName()), "named undo presentation = undo text + space + name");
        check((redoText + " Cut").equals(named.getRedoPresentationName()), "named redo presentation = redo text + space + name");

        String s = e.toString();
        check(s != null && s.contains("hasBeenDone") && s.contains("alive"), "toString identifies state fields");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
