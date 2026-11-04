/*
 * @test
 * @summary javax.swing.table.TableCellEditor conformance: white-box structure + black-box behavior.
 * @run main TableCellEditorTest
 */

import java.awt.Component;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

public class TableCellEditorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // full anonymous implementation exercising every method
    static TableCellEditor makeEditor() {
        return new TableCellEditor() {
            private Object value;
            private final List<CellEditorListener> listeners = new ArrayList<>();
            public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
                value = v;
                JLabel l = new JLabel();
                l.setText(v == null ? "" : v.toString());
                return l;
            }
            public Object getCellEditorValue() { return value; }
            public boolean isCellEditable(EventObject e) { return e == null; }
            public boolean shouldSelectCell(EventObject e) { return true; }
            public boolean stopCellEditing() {
                for (CellEditorListener l : new ArrayList<>(listeners)) l.editingStopped(new ChangeEvent(this));
                return true;
            }
            public void cancelCellEditing() {
                for (CellEditorListener l : new ArrayList<>(listeners)) l.editingCanceled(new ChangeEvent(this));
            }
            public void addCellEditorListener(CellEditorListener l) { listeners.add(l); }
            public void removeCellEditorListener(CellEditorListener l) { listeners.remove(l); }
        };
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.table.TableCellEditor");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.CellEditor"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        TableCellEditor editor = makeEditor();
        JTable table = new JTable();

        Component comp = editor.getTableCellEditorComponent(table, "edit me", true, 0, 0);
        check(comp instanceof JLabel, "getTableCellEditorComponent returns JLabel");
        check("edit me".equals(((JLabel) comp).getText()), "component text set from value");
        check("edit me".equals(editor.getCellEditorValue()), "getCellEditorValue returns value");

        check(editor.isCellEditable(null), "isCellEditable(null)");
        check(!editor.isCellEditable(new EventObject(new Object())), "isCellEditable(non-null event)");
        check(editor.shouldSelectCell(new EventObject(new Object())), "shouldSelectCell");

        final int[] stopped = {0};
        final int[] canceled = {0};
        CellEditorListener l = new CellEditorListener() {
            public void editingStopped(ChangeEvent e) { stopped[0]++; }
            public void editingCanceled(ChangeEvent e) { canceled[0]++; }
        };
        editor.addCellEditorListener(l);
        check(editor.stopCellEditing(), "stopCellEditing true");
        check(stopped[0] == 1, "editingStopped fired");
        editor.cancelCellEditing();
        check(canceled[0] == 1, "editingCanceled fired");
        editor.removeCellEditorListener(l);
        editor.stopCellEditing();
        check(stopped[0] == 1, "removeCellEditorListener stops delivery");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
