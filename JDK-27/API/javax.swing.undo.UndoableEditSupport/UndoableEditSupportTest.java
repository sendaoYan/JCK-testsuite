/*
 * @test
 * @summary javax.swing.undo.UndoableEditSupport conformance: white-box structure + black-box behavior.
 * @run main UndoableEditSupportTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

public class UndoableEditSupportTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class NamedEdit extends AbstractUndoableEdit {
        final String name;
        NamedEdit(String name) { this.name = name; }
        public String getPresentationName() { return name; }
    }

    // Listener that records every posted edit and its source.
    static class RecordingListener implements UndoableEditListener {
        final List<UndoableEdit> edits = new ArrayList<UndoableEdit>();
        final List<Object> sources = new ArrayList<Object>();
        public void undoableEditHappened(UndoableEditEvent e) {
            edits.add(e.getEdit());
            sources.add(e.getSource());
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.undo.UndoableEditSupport");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Object source = new Object();
        UndoableEditSupport support = new UndoableEditSupport(source);
        check(support.getUpdateLevel() == 0, "initial update level 0");

        RecordingListener l1 = new RecordingListener();
        RecordingListener l2 = new RecordingListener();
        support.addUndoableEditListener(l1);
        support.addUndoableEditListener(l2);

        javax.swing.event.UndoableEditListener[] listeners = support.getUndoableEditListeners();
        check(listeners.length == 2, "two listeners registered");

        // postEdit fires listeners with the posted edit and the real source.
        NamedEdit e1 = new NamedEdit("one");
        support.postEdit(e1);
        check(l1.edits.size() == 1 && l2.edits.size() == 1, "postEdit fires each listener");
        check(l1.edits.get(0) == e1, "listener receives the posted edit");
        check(l1.sources.get(0) == source, "event source is the real source");

        // removeUndoableEditListener stops delivery.
        support.removeUndoableEditListener(l2);
        check(support.getUndoableEditListeners().length == 1, "listener removed");
        NamedEdit e2 = new NamedEdit("two");
        support.postEdit(e2);
        check(l2.edits.size() == 1, "removed listener not notified");

        // beginUpdate accumulates edits into a compound; endUpdate posts it once.
        RecordingListener l3 = new RecordingListener();
        support.addUndoableEditListener(l3);
        support.beginUpdate();
        check(support.getUpdateLevel() == 1, "beginUpdate raises level");
        support.postEdit(new NamedEdit("a"));
        support.postEdit(new NamedEdit("b"));
        check(l3.edits.size() == 0, "no firing while update in progress");
        support.endUpdate();
        check(support.getUpdateLevel() == 0, "endUpdate lowers level");
        check(l3.edits.size() == 1, "endUpdate posts compound edit once");
        check(l3.edits.get(0) instanceof CompoundEdit, "posted edit is a CompoundEdit");
        check("b".equals(l3.edits.get(0).getPresentationName()), "compound name from last subedit");

        // Nested beginUpdate defers posting until the outermost endUpdate.
        RecordingListener l4 = new RecordingListener();
        support.addUndoableEditListener(l4);
        support.beginUpdate();       // level 1
        support.beginUpdate();       // level 2
        support.postEdit(new NamedEdit("x"));
        support.endUpdate();         // back to level 1
        check(support.getUpdateLevel() == 1, "nested endUpdate keeps level 1");
        check(l4.edits.size() == 0, "no firing at inner level");
        support.endUpdate();         // level 0
        check(l4.edits.size() == 1, "outer endUpdate fires once");

        // Default constructor uses 'this' as the real source.
        UndoableEditSupport noSource = new UndoableEditSupport();
        RecordingListener l5 = new RecordingListener();
        noSource.addUndoableEditListener(l5);
        noSource.postEdit(new NamedEdit("self"));
        check(l5.sources.get(0) == noSource, "default real source is the support itself");

        // getUndoableEditListeners returns a snapshot array.
        UndoableEditSupport snap = new UndoableEditSupport();
        snap.addUndoableEditListener(l5);
        javax.swing.event.UndoableEditListener[] arr = snap.getUndoableEditListeners();
        arr[0] = null;
        check(snap.getUndoableEditListeners()[0] == l5, "listener array is a snapshot copy");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
