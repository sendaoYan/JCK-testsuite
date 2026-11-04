/*
 * @test
 * @summary javax.swing.tree.TreeCellEditor conformance: white-box structure + black-box behavior.
 * @run main TreeCellEditorTest
 */

import java.awt.Component;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.TreeCellEditor;

public class TreeCellEditorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.TreeCellEditor");
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
        final JLabel label = new JLabel();
        final List<CellEditorListener> listeners = new ArrayList<>();
        final boolean[] stopped = new boolean[1];
        final boolean[] canceled = new boolean[1];

        TreeCellEditor editor = new TreeCellEditor() {
            public Component getTreeCellEditorComponent(JTree tree, Object value,
                    boolean selected, boolean expanded, boolean leaf, int row) {
                label.setText(String.valueOf(value));
                return label;
            }
            public Object getCellEditorValue() { return label.getText(); }
            public boolean isCellEditable(EventObject e) { return false; }
            public boolean shouldSelectCell(EventObject e) { return true; }
            public boolean stopCellEditing() { stopped[0] = true; return true; }
            public void cancelCellEditing() { canceled[0] = true; }
            public void addCellEditorListener(CellEditorListener l) { listeners.add(l); }
            public void removeCellEditorListener(CellEditorListener l) { listeners.remove(l); }
        };

        JTree tree = new JTree();
        Component c = editor.getTreeCellEditorComponent(tree, "value", true, true, false, 0);
        check(c == label, "getTreeCellEditorComponent returns label");
        check("value".equals(editor.getCellEditorValue()), "getCellEditorValue");
        check(editor.isCellEditable(null) == false, "isCellEditable");
        check(editor.shouldSelectCell(null) == true, "shouldSelectCell");

        CellEditorListener l = new CellEditorListener() {
            public void editingStopped(ChangeEvent e) { }
            public void editingCanceled(ChangeEvent e) { }
        };
        editor.addCellEditorListener(l);
        check(listeners.size() == 1 && listeners.get(0) == l, "addCellEditorListener");
        editor.removeCellEditorListener(l);
        check(listeners.size() == 0, "removeCellEditorListener");

        check(editor.stopCellEditing() == true && stopped[0] == true, "stopCellEditing");
        editor.cancelCellEditing();
        check(canceled[0] == true, "cancelCellEditing");

        check(TreeCellEditor.class.isAssignableFrom(DefaultTreeCellEditor.class), "DefaultTreeCellEditor implements TreeCellEditor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
