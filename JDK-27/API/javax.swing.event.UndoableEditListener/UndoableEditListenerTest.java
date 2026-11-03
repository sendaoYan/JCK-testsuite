/*
 * @test
 * @summary javax.swing.event.UndoableEditListener conformance: white-box structure + black-box behavior.
 * @run main UndoableEditListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

public class UndoableEditListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.UndoableEditListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "interface 0");
        Method m = c.getMethod("undoableEditHappened", UndoableEditEvent.class);
        check(m.getReturnType() == void.class, "undoableEditHappened returns void");
        check(Modifier.isAbstract(m.getModifiers()), "undoableEditHappened abstract");
        check(c.getDeclaredMethods().length == 1, "declared method count == 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final UndoableEditEvent[] last = new UndoableEditEvent[1];
        UndoableEditListener l = new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) { last[0] = e; }
        };

        Object src = new Object();
        UndoableEdit edit = new AbstractUndoableEdit();
        UndoableEditEvent ev = new UndoableEditEvent(src, edit);
        l.undoableEditHappened(ev);
        check(last[0] == ev, "undoableEditHappened fired");
        check(last[0].getSource() == src, "getSource round-trip");
        check(last[0].getEdit() == edit, "getEdit round-trip");
        check(edit.isSignificant() == true, "edit significant by default");
        // Multi-step undo/redo state machine for the captured edit.
        check(edit.canUndo() == true, "canUndo initially true");
        check(edit.canRedo() == false, "canRedo initially false");
        edit.undo();
        check(edit.canUndo() == false, "canUndo false after undo");
        check(edit.canRedo() == true, "canRedo true after undo");
        edit.redo();
        check(edit.canUndo() == true, "canUndo true after redo");
        check(edit.canRedo() == false, "canRedo false after redo");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
