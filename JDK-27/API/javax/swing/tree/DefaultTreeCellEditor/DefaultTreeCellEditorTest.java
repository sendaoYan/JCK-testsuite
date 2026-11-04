/*
 * @test
 * @summary javax.swing.tree.DefaultTreeCellEditor conformance: white-box structure + black-box behavior.
 * @run main DefaultTreeCellEditorTest
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.lang.reflect.Modifier;
import java.util.EventObject;

import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

public class DefaultTreeCellEditorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class EditorListener implements CellEditorListener {
        int stopped, canceled;
        public void editingStopped(ChangeEvent e) { stopped++; }
        public void editingCanceled(ChangeEvent e) { canceled++; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.tree.DefaultTreeCellEditor");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 3, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.event.ActionListener"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.tree.TreeCellEditor"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("javax.swing.event.TreeSelectionListener"), "interface 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JTree tree = new JTree();
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        DefaultTreeCellEditor editor = new DefaultTreeCellEditor(tree, renderer);

        check(editor.getFont() == null, "font null by default");
        Font f = new Font(Font.DIALOG, Font.PLAIN, 14);
        editor.setFont(f);
        check(f.equals(editor.getFont()), "setFont/getFont round-trip");

        Color c = new Color(20, 21, 22);
        editor.setBorderSelectionColor(c);
        check(c.equals(editor.getBorderSelectionColor()), "setBorderSelectionColor/getBorderSelectionColor");

        EditorListener listener = new EditorListener();
        editor.addCellEditorListener(listener);
        check(editor.getCellEditorListeners().length == 1 && editor.getCellEditorListeners()[0] == listener, "getCellEditorListeners");

        // getTreeCellEditorComponent configures the real (text field) editor.
        Component comp = editor.getTreeCellEditorComponent(tree, "hello", true, true, false, 0);
        check(comp != null, "editor component non-null");
        check(comp instanceof java.awt.Container, "editor component is a Container");
        check("hello".equals(editor.getCellEditorValue()), "getCellEditorValue round-trip");

        // Editing predicates (no real UI events needed).
        check(editor.isCellEditable(null) == true, "isCellEditable(null)");
        check(editor.shouldSelectCell(null) == true, "shouldSelectCell(null)");

        // stop / cancel editing fire listener callbacks and do not pop dialogs.
        check(editor.stopCellEditing() == true, "stopCellEditing true");
        check(listener.stopped == 1, "editingStopped fired once");
        editor.cancelCellEditing();
        check(listener.canceled == 1, "editingCanceled fired once");

        editor.removeCellEditorListener(listener);
        check(editor.getCellEditorListeners().length == 0, "removeCellEditorListener");

        // valueChanged / actionPerformed are safe when there is no live editing session.
        TreePath path = new TreePath("node");
        TreeSelectionEvent se = new TreeSelectionEvent("src", new TreePath[] { path },
                new boolean[] { true }, path, path);
        editor.valueChanged(se);
        editor.actionPerformed(new ActionEvent(new Object(), 0, "cmd"));

        // 3-arg constructor with a custom editor still configures correctly.
        TreeCellEditor custom = new TreeCellEditor() {
            public Component getTreeCellEditorComponent(JTree t, Object value, boolean sel, boolean exp, boolean leaf, int row) {
                return new javax.swing.JLabel(String.valueOf(value));
            }
            public Object getCellEditorValue() { return "custom"; }
            public boolean isCellEditable(EventObject e) { return false; }
            public boolean shouldSelectCell(EventObject e) { return false; }
            public boolean stopCellEditing() { return true; }
            public void cancelCellEditing() { }
            public void addCellEditorListener(CellEditorListener l) { }
            public void removeCellEditorListener(CellEditorListener l) { }
        };
        DefaultTreeCellEditor customEditor = new DefaultTreeCellEditor(new JTree(), new DefaultTreeCellRenderer(), custom);
        check(customEditor != null, "3-arg constructor");
        check("custom".equals(customEditor.getCellEditorValue()), "custom editor getCellEditorValue");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
