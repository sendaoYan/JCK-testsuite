/*
 * @test
 * @summary javax.swing.DefaultCellEditor conformance: type shape + editor lifecycle (headless-safe).
 * @run main DefaultCellEditorTest
 */

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.CellEditorListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class DefaultCellEditorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultCellEditor", false,
                DefaultCellEditorTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null
                && c.getSuperclass().getName().equals("javax.swing.AbstractCellEditor"), "superclass");
        check(hasInterface(c, "javax.swing.table.TableCellEditor"), "implements TableCellEditor");
        check(hasInterface(c, "javax.swing.tree.TreeCellEditor"), "implements TreeCellEditor");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getComponent"), "method getComponent");
        check(names.contains("setClickCountToStart"), "method setClickCountToStart");
        check(names.contains("getClickCountToStart"), "method getClickCountToStart");
        check(names.contains("getCellEditorValue"), "method getCellEditorValue");
        check(names.contains("isCellEditable"), "method isCellEditable");
    }

    static void testConstructors() {
        JTextField field = new JTextField();
        DefaultCellEditor text = new DefaultCellEditor(field);
        check(text.getComponent() == field, "text editor component is the field");
        check(text.getClickCountToStart() == 2, "text editor double-click to start");
        JComboBox<String> combo = new JComboBox<String>(new String[]{"a", "b"});
        DefaultCellEditor comboEd = new DefaultCellEditor(combo);
        check(comboEd.getComponent() == combo, "combo editor component is the combo");
        check(comboEd.getClickCountToStart() == 1, "combo editor single-click to start");
        DefaultCellEditor checkEd = new DefaultCellEditor(new JCheckBox());
        check(checkEd.getClickCountToStart() == 1, "checkbox editor single-click to start");
        text.setClickCountToStart(3);
        check(text.getClickCountToStart() == 3, "setClickCountToStart");
    }

    static void testValues() {
        JTextField field = new JTextField("init");
        DefaultCellEditor text = new DefaultCellEditor(field);
        check("init".equals(text.getCellEditorValue()), "text editor value is field text");
        field.setText("edited");
        check("edited".equals(text.getCellEditorValue()), "text editor follows field edits");
        JComboBox<String> combo = new JComboBox<String>(new String[]{"a", "b", "c"});
        DefaultCellEditor comboEd = new DefaultCellEditor(combo);
        check("a".equals(comboEd.getCellEditorValue()), "combo editor value is selection");
        combo.setSelectedItem("b");
        check("b".equals(comboEd.getCellEditorValue()), "combo editor follows selection");
        DefaultCellEditor checkEd = new DefaultCellEditor(new JCheckBox());
        check(Boolean.FALSE.equals(checkEd.getCellEditorValue()), "checkbox editor default false");
    }

    static void testEditorComponents() {
        JTable table = new JTable(2, 2);
        JTextField field = new JTextField();
        DefaultCellEditor text = new DefaultCellEditor(field);
        Component c = text.getTableCellEditorComponent(table, "v1", true, 0, 0);
        check(c == field, "table editor component is the field");
        check("v1".equals(field.getText()), "table editor writes value into field");
        JComboBox<String> combo = new JComboBox<String>(new String[]{"x", "y"});
        DefaultCellEditor comboEd = new DefaultCellEditor(combo);
        Component cc = comboEd.getTableCellEditorComponent(table, "x", false, 1, 1);
        check(cc == combo, "table editor component is the combo");
        Component tc = text.getTreeCellEditorComponent(new JTree(), "tv", true, true, false, 0);
        check(tc == field, "tree editor component is the field");
        check("tv".equals(field.getText()), "tree editor writes value into field");
        Component tcc = comboEd.getTreeCellEditorComponent(new JTree(), "y", true, true, false, 0);
        check(tcc == combo, "tree editor component is the combo");
    }

    static void testEditability() {
        JTextField field = new JTextField();
        DefaultCellEditor text = new DefaultCellEditor(field);
        check(text.isCellEditable(null), "null event means keyboard edit: editable");
        check(text.shouldSelectCell(null), "shouldSelectCell true");
        JLabel src = new JLabel("src");
        MouseEvent one = new MouseEvent(src, MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 1, 1, 1, false);
        MouseEvent two = new MouseEvent(src, MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(), 0, 1, 1, 2, false);
        check(!text.isCellEditable(one), "single click below clickCountToStart");
        check(text.isCellEditable(two), "double click meets clickCountToStart");
        text.setClickCountToStart(1);
        check(text.isCellEditable(one), "single click enough after setClickCountToStart(1)");
        check(text.isCellEditable(new java.awt.event.ActionEvent(src, 0, "cmd")),
                "non-mouse events are editable");
    }

    static void testLifecycle() throws Exception {
        final JTextField field = new JTextField();
        final DefaultCellEditor text = new DefaultCellEditor(field);
        final AtomicInteger stopped = new AtomicInteger();
        final AtomicInteger cancelled = new AtomicInteger();
        CellEditorListener l = new CellEditorListener() {
            public void editingStopped(ChangeEvent e) { stopped.incrementAndGet(); }
            public void editingCanceled(ChangeEvent e) { cancelled.incrementAndGet(); }
        };
        text.addCellEditorListener(l);
        SwingUtilities.invokeAndWait(new Runnable() { public void run() {
            check(text.stopCellEditing(), "stopCellEditing returns true");
        }});
        check(stopped.get() == 1, "stopCellEditing notifies listeners");
        text.cancelCellEditing();
        check(cancelled.get() == 1, "cancelCellEditing notifies listeners");
        text.removeCellEditorListener(l);
        text.stopCellEditing();
        text.cancelCellEditing();
        check(stopped.get() == 1 && cancelled.get() == 1,
                "removed listener receives no further events");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testShape();
        testConstructors();
        testValues();
        testEditorComponents();
        testEditability();
        testLifecycle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
