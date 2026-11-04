/*
 * @test
 * @summary com.sun.java.accessibility.util.SwingEventMonitor conformance: white-box structure + black-box behavior.
 * @run main SwingEventMonitorTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.beans.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.swing.undo.*;

public class SwingEventMonitorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.java.accessibility.util.SwingEventMonitor");
        check(!c.isInterface(), "not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("com.sun.java.accessibility.util.AWTEventMonitor"), "extends AWTEventMonitor");
        Set<String> m = methodNames(c);
        check(m.contains("addAncestorListener"), "addAncestorListener");
        check(m.contains("addCaretListener"), "addCaretListener");
        check(m.contains("addCellEditorListener"), "addCellEditorListener");
        check(m.contains("addChangeListener"), "addChangeListener");
        check(m.contains("addColumnModelListener"), "addColumnModelListener");
        check(m.contains("addDocumentListener"), "addDocumentListener");
        check(m.contains("addListDataListener"), "addListDataListener");
        check(m.contains("addListSelectionListener"), "addListSelectionListener");
        check(m.contains("addMenuListener"), "addMenuListener");
        check(m.contains("addPopupMenuListener"), "addPopupMenuListener");
        check(m.contains("addTableModelListener"), "addTableModelListener");
        check(m.contains("addTreeExpansionListener"), "addTreeExpansionListener");
        check(m.contains("addTreeModelListener"), "addTreeModelListener");
        check(m.contains("addTreeSelectionListener"), "addTreeSelectionListener");
        check(m.contains("addUndoableEditListener"), "addUndoableEditListener");
        check(m.contains("addInternalFrameListener"), "addInternalFrameListener");
        check(m.contains("addPropertyChangeListener"), "addPropertyChangeListener");
        check(m.contains("addVetoableChangeListener"), "addVetoableChangeListener");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        com.sun.java.accessibility.util.SwingEventMonitor mon =
            new com.sun.java.accessibility.util.SwingEventMonitor();
        check(mon != null, "new SwingEventMonitor() non-null");
        // Inherited AWT monitor behavior remains callable.
        check(com.sun.java.accessibility.util.SwingEventMonitor.getComponentWithFocus() == null,
              "inherited getComponentWithFocus null in headless");

        AncestorListener anl = new AncestorListener() {
            public void ancestorAdded(AncestorEvent e) {}
            public void ancestorMoved(AncestorEvent e) {}
            public void ancestorRemoved(AncestorEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addAncestorListener(anl);
        com.sun.java.accessibility.util.SwingEventMonitor.removeAncestorListener(anl);

        CaretListener car = new CaretListener() {
            public void caretUpdate(CaretEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addCaretListener(car);
        com.sun.java.accessibility.util.SwingEventMonitor.removeCaretListener(car);

        CellEditorListener cel = new CellEditorListener() {
            public void editingStopped(ChangeEvent e) {}
            public void editingCanceled(ChangeEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addCellEditorListener(cel);
        com.sun.java.accessibility.util.SwingEventMonitor.removeCellEditorListener(cel);

        ChangeListener chl = new ChangeListener() {
            public void stateChanged(ChangeEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addChangeListener(chl);
        com.sun.java.accessibility.util.SwingEventMonitor.removeChangeListener(chl);

        TableColumnModelListener cml = new TableColumnModelListener() {
            public void columnAdded(TableColumnModelEvent e) {}
            public void columnRemoved(TableColumnModelEvent e) {}
            public void columnMoved(TableColumnModelEvent e) {}
            public void columnMarginChanged(ChangeEvent e) {}
            public void columnSelectionChanged(ListSelectionEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addColumnModelListener(cml);
        com.sun.java.accessibility.util.SwingEventMonitor.removeColumnModelListener(cml);

        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {}
            public void removeUpdate(DocumentEvent e) {}
            public void changedUpdate(DocumentEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addDocumentListener(dl);
        com.sun.java.accessibility.util.SwingEventMonitor.removeDocumentListener(dl);

        ListDataListener ldl = new ListDataListener() {
            public void intervalAdded(ListDataEvent e) {}
            public void intervalRemoved(ListDataEvent e) {}
            public void contentsChanged(ListDataEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addListDataListener(ldl);
        com.sun.java.accessibility.util.SwingEventMonitor.removeListDataListener(ldl);

        ListSelectionListener lsl = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addListSelectionListener(lsl);
        com.sun.java.accessibility.util.SwingEventMonitor.removeListSelectionListener(lsl);

        MenuListener menl = new MenuListener() {
            public void menuSelected(MenuEvent e) {}
            public void menuDeselected(MenuEvent e) {}
            public void menuCanceled(MenuEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addMenuListener(menl);
        com.sun.java.accessibility.util.SwingEventMonitor.removeMenuListener(menl);

        PopupMenuListener pml = new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            public void popupMenuCanceled(PopupMenuEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addPopupMenuListener(pml);
        com.sun.java.accessibility.util.SwingEventMonitor.removePopupMenuListener(pml);

        TableModelListener tml = new TableModelListener() {
            public void tableChanged(TableModelEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addTableModelListener(tml);
        com.sun.java.accessibility.util.SwingEventMonitor.removeTableModelListener(tml);

        TreeExpansionListener tel = new TreeExpansionListener() {
            public void treeExpanded(TreeExpansionEvent e) {}
            public void treeCollapsed(TreeExpansionEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addTreeExpansionListener(tel);
        com.sun.java.accessibility.util.SwingEventMonitor.removeTreeExpansionListener(tel);

        TreeModelListener tmol = new TreeModelListener() {
            public void treeNodesChanged(TreeModelEvent e) {}
            public void treeNodesInserted(TreeModelEvent e) {}
            public void treeNodesRemoved(TreeModelEvent e) {}
            public void treeStructureChanged(TreeModelEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addTreeModelListener(tmol);
        com.sun.java.accessibility.util.SwingEventMonitor.removeTreeModelListener(tmol);

        TreeSelectionListener tsl = new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addTreeSelectionListener(tsl);
        com.sun.java.accessibility.util.SwingEventMonitor.removeTreeSelectionListener(tsl);

        UndoableEditListener uel = new UndoableEditListener() {
            public void undoableEditHappened(UndoableEditEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addUndoableEditListener(uel);
        com.sun.java.accessibility.util.SwingEventMonitor.removeUndoableEditListener(uel);

        InternalFrameListener ifl = new InternalFrameListener() {
            public void internalFrameOpened(InternalFrameEvent e) {}
            public void internalFrameClosing(InternalFrameEvent e) {}
            public void internalFrameClosed(InternalFrameEvent e) {}
            public void internalFrameIconified(InternalFrameEvent e) {}
            public void internalFrameDeiconified(InternalFrameEvent e) {}
            public void internalFrameActivated(InternalFrameEvent e) {}
            public void internalFrameDeactivated(InternalFrameEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addInternalFrameListener(ifl);
        com.sun.java.accessibility.util.SwingEventMonitor.removeInternalFrameListener(ifl);

        PropertyChangeListener pcl = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addPropertyChangeListener(pcl);
        com.sun.java.accessibility.util.SwingEventMonitor.removePropertyChangeListener(pcl);

        VetoableChangeListener vcl = new VetoableChangeListener() {
            public void vetoableChange(PropertyChangeEvent e) throws PropertyVetoException {}
        };
        com.sun.java.accessibility.util.SwingEventMonitor.addVetoableChangeListener(vcl);
        com.sun.java.accessibility.util.SwingEventMonitor.removeVetoableChangeListener(vcl);

        // Combined add/remove across categories still leaves no focused component.
        com.sun.java.accessibility.util.SwingEventMonitor.addChangeListener(chl);
        com.sun.java.accessibility.util.SwingEventMonitor.addPropertyChangeListener(pcl);
        com.sun.java.accessibility.util.SwingEventMonitor.removeChangeListener(chl);
        com.sun.java.accessibility.util.SwingEventMonitor.removePropertyChangeListener(pcl);
        check(com.sun.java.accessibility.util.SwingEventMonitor.getComponentWithFocus() == null,
              "focus null after combined swing add/remove");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
