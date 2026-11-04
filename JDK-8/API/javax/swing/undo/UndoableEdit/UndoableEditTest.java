/*
 * @test
 * @summary javax.swing.undo.UndoableEdit conformance: white-box structure + black-box behavior.
 * @run main UndoableEditTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.undo.UndoableEdit;
import java.util.HashSet;
import java.util.Set;

public class UndoableEditTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // Minimal conforming implementation used to exercise the interface contract.
    static class SimpleEdit implements UndoableEdit {
        boolean undone;
        public void undo() { undone = true; }
        public boolean canUndo() { return !undone; }
        public void redo() { undone = false; }
        public boolean canRedo() { return undone; }
        public void die() { }
        public boolean addEdit(UndoableEdit e) { return false; }
        public boolean replaceEdit(UndoableEdit e) { return false; }
        public boolean isSignificant() { return true; }
        public String getPresentationName() { return "Simple"; }
        public String getUndoPresentationName() { return "Undo Simple"; }
        public String getRedoPresentationName() { return "Redo Simple"; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.undo.UndoableEdit");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        Method[] methods = c.getDeclaredMethods();
        check(methods.length == 11, "eleven interface methods");
        check(c.getMethod("undo") != null, "undo declared");
        check(c.getMethod("redo") != null, "redo declared");
        check(c.getMethod("die") != null, "die declared");
        check(c.getMethod("getPresentationName") != null, "getPresentationName declared");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        UndoableEdit e = new SimpleEdit();
        check(e.canUndo() && !e.canRedo(), "fresh edit undoable, not redoable");
        check(e.isSignificant(), "significant");
        check("Simple".equals(e.getPresentationName()), "presentation name");
        check("Undo Simple".equals(e.getUndoPresentationName()), "undo presentation name");
        check("Redo Simple".equals(e.getRedoPresentationName()), "redo presentation name");

        e.undo();
        check(!e.canUndo() && e.canRedo(), "after undo, redoable not undoable");
        e.redo();
        check(e.canUndo() && !e.canRedo(), "after redo, undoable not redoable");

        check(!e.addEdit(new SimpleEdit()), "addEdit returns false");
        check(!e.replaceEdit(new SimpleEdit()), "replaceEdit returns false");
        e.die(); // must not throw; concrete impl may ignore
        check(true, "die callable on interface reference");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.undo.UndoableEdit", false, UndoableEditTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("undo"), "method undo");
        check(names.contains("canUndo"), "method canUndo");
        check(names.contains("redo"), "method redo");
        check(names.contains("canRedo"), "method canRedo");
        check(names.contains("die"), "method die");
        check(names.contains("addEdit"), "method addEdit");
        check(names.contains("replaceEdit"), "method replaceEdit");
        check(names.contains("isSignificant"), "method isSignificant");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
