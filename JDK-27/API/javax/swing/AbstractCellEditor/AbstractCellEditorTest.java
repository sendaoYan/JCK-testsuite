/*
 * @test
 * @summary javax.swing.AbstractCellEditor conformance: white-box structure + black-box behavior.
 * @run main AbstractCellEditorTest
 */

import java.lang.reflect.Modifier;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

public class AbstractCellEditorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.AbstractCellEditor");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.CellEditor"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AbstractCellEditor ed = new AbstractCellEditor() {
            public Object getCellEditorValue() { return "value"; }
        };

        // default policy: always editable, always select, stop returns true
        check(ed.isCellEditable(null), "default editable");
        check(ed.isCellEditable(new EventObject(new Object())), "editable with event");
        check(ed.shouldSelectCell(null), "default shouldSelectCell");
        check(ed.stopCellEditing(), "stopCellEditing true");
        check("value".equals(ed.getCellEditorValue()), "getCellEditorValue");

        // stop/cancel fire the corresponding listener callbacks
        final int[] stopped = {0};
        final int[] canceled = {0};
        CellEditorListener l = new CellEditorListener() {
            public void editingStopped(ChangeEvent e) { stopped[0]++; }
            public void editingCanceled(ChangeEvent e) { canceled[0]++; }
        };
        ed.addCellEditorListener(l);
        check(ed.getCellEditorListeners().length == 1, "listener registered");
        ed.stopCellEditing();
        ed.cancelCellEditing();
        check(stopped[0] == 1, "editingStopped fired");
        check(canceled[0] == 1, "editingCanceled fired");
        ed.removeCellEditorListener(l);
        check(ed.getCellEditorListeners().length == 0, "listener removed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
