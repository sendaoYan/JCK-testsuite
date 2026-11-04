/*
 * @test
 * @summary javax.swing.CellEditor conformance: white-box structure + black-box behavior.
 * @run main CellEditorTest
 */

import java.lang.reflect.Modifier;
import java.util.EventObject;

import javax.swing.CellEditor;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

public class CellEditorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.CellEditor");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final int[] stopped = {0};
        final int[] canceled = {0};
        CellEditor ed = new CellEditor() {
            public Object getCellEditorValue() { return "v"; }
            public boolean isCellEditable(EventObject e) { return true; }
            public boolean shouldSelectCell(EventObject e) { return true; }
            public boolean stopCellEditing() { stopped[0]++; return true; }
            public void cancelCellEditing() { canceled[0]++; }
            public void addCellEditorListener(CellEditorListener l) { }
            public void removeCellEditorListener(CellEditorListener l) { }
        };

        check("v".equals(ed.getCellEditorValue()), "getCellEditorValue");
        check(ed.isCellEditable(null), "isCellEditable");
        check(ed.shouldSelectCell(null), "shouldSelectCell");
        check(ed.stopCellEditing(), "stopCellEditing");
        ed.cancelCellEditing();
        check(stopped[0] == 1 && canceled[0] == 1, "stop/cancel invoked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
