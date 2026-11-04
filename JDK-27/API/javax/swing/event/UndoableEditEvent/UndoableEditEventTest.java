/*
 * @test
 * @summary javax.swing.event.UndoableEditEvent conformance: white-box structure + black-box behavior.
 * @run main UndoableEditEventTest
 */

import java.lang.reflect.Modifier;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

public class UndoableEditEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.UndoableEditEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        Object source = new Object();
        UndoableEdit edit = new AbstractUndoableEdit();
        javax.swing.event.UndoableEditEvent ev = new javax.swing.event.UndoableEditEvent(source, edit);
        check(ev.getSource() == source, "getSource round-trip");
        check(ev.getEdit() == edit, "getEdit round-trip");

        // A custom edit confirms the event stores exactly the supplied edit.
        final String presentation = "my-edit";
        UndoableEdit custom = new AbstractUndoableEdit() {
            public String getPresentationName() { return presentation; }
        };
        javax.swing.event.UndoableEditEvent customEv = new javax.swing.event.UndoableEditEvent("src", custom);
        check(customEv.getEdit() == custom, "custom getEdit identity");
        check(customEv.getEdit().getPresentationName().equals(presentation), "custom edit presentation");
        check(customEv.getSource().equals("src"), "custom getSource");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
