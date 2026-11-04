/*
 * @test
 * @summary javax.swing.JList conformance: type shape via reflection + component-layer
 *          black-box contracts (defaults, selection algebra, listener payloads,
 *          model swap rules, layout config, negative matrices, Metal UI binding).
 * @run main JListTest
 */

import java.awt.EventQueue;
import java.awt.Rectangle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Vector;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JListTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkEquals(Object expect, Object actual, String m) {
        check(expect == null ? actual == null : expect.equals(actual),
              m + ": expected " + expect + " but got " + actual);
    }

    static void checkThrows(final Class<? extends Throwable> type, final String what, final Runnable code) {
        try {
            code.run();
        } catch (Throwable t) {
            check(type.isInstance(t),
                  what + ": expected " + type.getSimpleName() + " but threw " + t.getClass().getSimpleName());
            return;
        }
        throw new AssertionError(what + ": expected " + type.getSimpleName() + " but no exception thrown");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    /** ListSelectionListener recorder: counts events and remembers the last payload. */
    static class Sel implements ListSelectionListener {
        int count; ListSelectionEvent last;
        public void valueChanged(ListSelectionEvent e) { count++; last = e; }
        void reset() { count = 0; last = null; }
    }

    static Vector<String> data() {
        Vector<String> v = new Vector<String>();
        v.add("a"); v.add("b"); v.add("c");
        return v;
    }

    public static void main(String[] args) throws Exception {
        final Class<?> c = Class.forName("javax.swing.JList", false, JListTest.class.getClassLoader());
        EventQueue.invokeAndWait(new Runnable() { public void run() { testShape(c); }});
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        EventQueue.invokeAndWait(new Runnable() { public void run() {
            testDefaults();
            testSelectionAlgebra();
            testModelRules();
            testConfigAndUI();
        }});
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- type shape (reflection, no construction) ----
    static void testShape(Class<?> c) {
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.swing.Scrollable"), "implements javax.swing.Scrollable");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getUI"), "method getUI");
        check(names.contains("setUI"), "method setUI");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
    }

    // ---- defaults on an unshown, empty and populated list ----
    static void testDefaults() {
        JList<String> empty = new JList<String>();
        checkEquals(-1, empty.getSelectedIndex(), "empty getSelectedIndex");
        check(empty.getSelectedValue() == null, "empty getSelectedValue");
        check(empty.getSelectedValuesList().isEmpty(), "empty getSelectedValuesList");
        check(empty.getAnchorSelectionIndex() == -1, "default anchor -1");
        check(empty.getLeadSelectionIndex() == -1, "default lead -1");
        check(empty.getSelectionModel().getSelectionMode() == ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
              "default selectionMode MULTIPLE_INTERVAL_SELECTION");
        check(empty.getLayoutOrientation() == JList.VERTICAL, "default layoutOrientation VERTICAL");
        check(empty.getVisibleRowCount() == 8, "default visibleRowCount 8");
        check(empty.getFixedCellWidth() == -1 && empty.getFixedCellHeight() == -1, "default fixed cell size -1");
        check(empty.getPrototypeCellValue() == null, "default prototypeCellValue null");
        check(empty.getDragEnabled() == false, "default dragEnabled false");
        check(empty.getFirstVisibleIndex() == -1, "unshown getFirstVisibleIndex -1");
        check(empty.getLastVisibleIndex() == -1, "unshown getLastVisibleIndex -1");
        checkEquals(new Rectangle(0, 0, 0, 0), empty.getVisibleRect(), "unshown getVisibleRect");
        check(empty.getCellRenderer() != null, "default cellRenderer non-null");

        JList<String> list = new JList<String>(data());
        checkEquals(3, list.getModel().getSize(), "3-element model size");
        checkEquals(-1, list.getSelectedIndex(), "populated default getSelectedIndex");
        check(list.getSelectedValuesList().isEmpty(), "populated default empty selection");
        // no-throw scroll contracts on unshown list (value clamped/no-op internally)
        list.ensureIndexIsVisible(1);
        check(true, "ensureIndexIsVisible no-throw unshown");
    }

    // ---- selection algebra + exact listener payloads ----
    static void testSelectionAlgebra() {
        JList<String> list = new JList<String>(data());
        Sel sel = new Sel();
        list.addListSelectionListener(sel);

        list.setSelectedIndex(1);
        checkEquals(1, list.getSelectedIndex(), "setSelectedIndex(1)");
        check(list.isSelectedIndex(1), "isSelectedIndex(1)");
        check(!list.isSelectedIndex(0), "isSelectedIndex(0) false");
        checkEquals("b", list.getSelectedValue(), "getSelectedValue b");
        check(sel.count == 1, "one event for setSelectedIndex");
        check(sel.last.getFirstIndex() == 1 && sel.last.getLastIndex() == 1, "event first/last == 1/1");
        checkEquals(1, list.getAnchorSelectionIndex(), "anchor follows selection");
        checkEquals(1, list.getLeadSelectionIndex(), "lead follows selection");

        sel.reset();
        list.setSelectedIndices(new int[] {2, 0});
        checkEquals(0, list.getSelectedIndices()[0], "getSelectedIndices sorted [0]");
        checkEquals(2, list.getSelectedIndices()[1], "getSelectedIndices sorted [2]");
        checkEquals(2, list.getSelectedIndices().length, "two selected indices");
        check(sel.count == 3, "setSelectedIndices({2,0}) fires 3 events (clear + 2 adds)");
        check(sel.last.getFirstIndex() == 0 && sel.last.getLastIndex() == 2, "last event range 0..2");

        sel.reset();
        list.clearSelection();
        checkEquals(-1, list.getSelectedIndex(), "clearSelection resets index");
        check(list.getSelectedValuesList().isEmpty(), "clearSelection empties values");
        check(sel.count == 1, "clearSelection fires exactly one event");

        // out-of-range and absent-value selection attempts are benign no-ops
        sel.reset();
        list.setSelectedIndex(99);
        checkEquals(-1, list.getSelectedIndex(), "setSelectedIndex(99) no-throw, selection unchanged");
        check(sel.count == 0, "setSelectedIndex(99) fires no event");
        list.setSelectedValue("nope", false);
        checkEquals(-1, list.getSelectedIndex(), "setSelectedValue(absent) no-throw, selection unchanged");
        check(sel.count == 0, "setSelectedValue(absent) fires no event");
        list.setSelectedValue("b", false);
        checkEquals(1, list.getSelectedIndex(), "setSelectedValue(b) selects 1");
        check(sel.count == 1, "setSelectedValue fires one event");

        // JList contrast with JComboBox: setSelectedIndex(-1) does NOT clear an
        // existing selection (model keeps interval; no event fired)
        sel.reset();
        list.setSelectedIndex(-1);
        checkEquals(1, list.getSelectedIndex(), "setSelectedIndex(-1) keeps existing selection");
        check(sel.count == 0, "setSelectedIndex(-1) fires no event");

        // re-setting the same interval keeps the selection and stays quiet
        list.setSelectionInterval(0, 0);
        sel.reset();
        list.setSelectionInterval(0, 0);
        checkEquals(0, list.getSelectedIndex(), "re-set same interval keeps selection");
        check(sel.count == 0, "re-set same interval fires no event");

        // custom selection model swap: identity + independence
        DefaultListSelectionModel sm = new DefaultListSelectionModel();
        sm.setSelectionInterval(2, 2);
        list.setSelectionModel(sm);
        check(list.getSelectionModel() == sm, "setSelectionModel identity");
        checkEquals(2, list.getSelectedIndex(), "selection from swapped model visible");
        checkThrows(IllegalArgumentException.class, "setSelectionModel(null)",
            new Runnable() { public void run() { new JList<String>().setSelectionModel(null); }});
    }

    // ---- model swap rules + renderer ----
    static void testModelRules() {
        JList<String> list = new JList<String>(data());
        list.setSelectedIndex(1);
        checkEquals(1, list.getSelectedIndex(), "pre-swap selection");

        DefaultListModel<String> lm = new DefaultListModel<String>();
        lm.addElement("x"); lm.addElement("y");
        list.setModel(lm);
        check(list.getModel() == lm, "setModel identity");
        checkEquals(2, list.getModel().getSize(), "new model size visible");
        checkEquals(-1, list.getSelectedIndex(), "setModel clears selection");

        String[] arr = {"p", "q", "r"};
        list.setListData(arr);
        checkEquals(3, list.getModel().getSize(), "setListData array size");
        checkEquals(-1, list.getSelectedIndex(), "setListData resets selection");
        list.setListData(data());
        checkEquals(3, list.getModel().getSize(), "setListData vector size");

        checkThrows(IllegalArgumentException.class, "setModel(null)",
            new Runnable() { public void run() { new JList<String>().setModel(null); }});

        ListCellRenderer<? super String> r = list.getCellRenderer();
        list.setCellRenderer(r);
        check(list.getCellRenderer() == r, "setCellRenderer identity");
    }

    // ---- layout config negatives + UI binding ----
    static void testConfigAndUI() {
        JList<String> list = new JList<String>(data());
        list.setVisibleRowCount(5);
        checkEquals(5, list.getVisibleRowCount(), "setVisibleRowCount(5)");
        list.setVisibleRowCount(0);
        checkEquals(0, list.getVisibleRowCount(), "setVisibleRowCount(0) stored as-is");
        list.setVisibleRowCount(-1);
        checkEquals(0, list.getVisibleRowCount(), "setVisibleRowCount(-1) clamped to 0, no throw");

        list.setLayoutOrientation(JList.VERTICAL_WRAP);
        checkEquals(JList.VERTICAL_WRAP, list.getLayoutOrientation(), "VERTICAL_WRAP");
        check(JList.VERTICAL == 0 && JList.VERTICAL_WRAP == 1 && JList.HORIZONTAL_WRAP == 2,
              "layout orientation constants 0/1/2");
        checkThrows(IllegalArgumentException.class, "setLayoutOrientation(99)",
            new Runnable() { public void run() { list.setLayoutOrientation(99); }});

        list.setPrototypeCellValue("xx");
        checkEquals("xx", list.getPrototypeCellValue(), "setPrototypeDisplayValue via setPrototypeCellValue");

        // drag enablement needs a headful environment: headless throws exactly
        // java.awt.HeadlessException (both JDKs), leaving dragEnabled unchanged
        checkThrows(java.awt.HeadlessException.class, "setDragEnabled(true) headless",
            new Runnable() { public void run() { list.setDragEnabled(true); }});
        check(!list.getDragEnabled(), "dragEnabled unchanged after headless rejection");

        check(list.getUI() != null && list.getUI().getClass().getName().endsWith("BasicListUI"),
              "Metal binding installs BasicListUI");
        checkEquals("ListUI", list.getUIClassID(), "getUIClassID ListUI");
        check(list.getAccessibleContext() != null, "getAccessibleContext non-null");
        list.updateUI();
        check(list.getUI() != null, "updateUI no-throw");
        List<String> vals = list.getSelectedValuesList();
        check(vals != null, "getSelectedValuesList never null");
    }
}
