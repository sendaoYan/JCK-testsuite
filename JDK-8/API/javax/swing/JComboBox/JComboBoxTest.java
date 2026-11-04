/*
 * @test
 * @summary javax.swing.JComboBox conformance: type shape via reflection + component-layer
 *          black-box contracts (defaults, ctor model wiring, selection/action/item event
 *          payloads, editable+editor, mutator matrices on mutable vs fixed models).
 * @run main JComboBoxTest
 */

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JComboBoxTest {
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

    static class Act implements ActionListener {
        int count; ActionEvent last;
        public void actionPerformed(ActionEvent e) { count++; last = e; }
        void reset() { count = 0; last = null; }
    }

    static class Item implements ItemListener {
        int count; String seq = "";
        public void itemStateChanged(ItemEvent e) {
            count++;
            seq += (e.getStateChange() == ItemEvent.SELECTED ? "SELECTED:" : "DESELECTED:") + e.getItem() + ";";
        }
        void reset() { count = 0; seq = ""; }
    }

    /** Fixed (non-mutable) ComboBoxModel: mutating combo methods must reject it. */
    static class FixedModel implements ComboBoxModel<String> {
        private String sel;
        private final String[] items = {"x", "y"};
        public int getSize() { return items.length; }
        public String getElementAt(int i) { return items[i]; }
        public void setSelectedItem(Object o) { sel = (String) o; }
        public Object getSelectedItem() { return sel; }
        public void addListDataListener(ListDataListener l) { }
        public void removeListDataListener(ListDataListener l) { }
    }

    public static void main(String[] args) throws Exception {
        final Class<?> c = Class.forName("javax.swing.JComboBox", false, JComboBoxTest.class.getClassLoader());
        EventQueue.invokeAndWait(new Runnable() { public void run() { testShape(c); }});
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        EventQueue.invokeAndWait(new Runnable() { public void run() {
            testDefaults();
            testCtors();
            testSelectionEvents();
            testEditableAndConfig();
            testMutators();
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
        check(hasInterface(c, "java.awt.ItemSelectable"), "implements java.awt.ItemSelectable");
        check(hasInterface(c, "javax.swing.event.ListDataListener"), "implements javax.swing.event.ListDataListener");
        check(hasInterface(c, "java.awt.event.ActionListener"), "implements java.awt.event.ActionListener");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setUI"), "method setUI");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
        check(names.contains("getUI"), "method getUI");
    }

    // ---- defaults on an empty combo ----
    static void testDefaults() {
        JComboBox<String> e = new JComboBox<String>();
        checkEquals(0, e.getItemCount(), "default itemCount 0");
        checkEquals(-1, e.getSelectedIndex(), "default selectedIndex -1");
        check(e.getSelectedItem() == null, "default selectedItem null");
        check(e.getSelectedObjects() != null && e.getSelectedObjects().length == 0,
              "default getSelectedObjects empty array");
        checkEquals(8, e.getMaximumRowCount(), "default maximumRowCount 8");
        check(!e.isEditable(), "default not editable");
        check(e.isLightWeightPopupEnabled(), "default lightWeightPopupEnabled true");
        checkEquals("comboBoxChanged", e.getActionCommand(), "default actionCommand comboBoxChanged");
        check(!e.isPopupVisible(), "default popup not visible");
        check(e.getModel() instanceof DefaultComboBoxModel, "default model DefaultComboBoxModel");
        check(e.getRenderer() != null, "default renderer non-null");
        check(e.getEditor() != null, "default editor non-null");
        check(e.getUI() != null && e.getUI().getClass().getName().endsWith("MetalComboBoxUI"),
              "Metal binding installs MetalComboBoxUI");
        checkEquals("ComboBoxUI", e.getUIClassID(), "getUIClassID ComboBoxUI");
    }

    // ---- ctor wiring: all standard ctors install a mutable DefaultComboBoxModel ----
    static void testCtors() {
        JComboBox<String> arr = new JComboBox<String>(new String[] {"a", "b", "c"});
        check(arr.getModel() instanceof DefaultComboBoxModel, "array ctor mutable DefaultComboBoxModel");
        checkEquals(3, arr.getItemCount(), "array ctor count 3");
        checkEquals(0, arr.getSelectedIndex(), "array ctor auto-selects index 0");
        checkEquals("a", arr.getSelectedItem(), "array ctor selectedItem a");
        checkEquals("a", arr.getItemAt(0), "getItemAt(0)");
        checkEquals("c", arr.getItemAt(2), "getItemAt(2)");

        Vector<String> v = new Vector<String>(Arrays.asList("v1", "v2"));
        JComboBox<String> vec = new JComboBox<String>(v);
        check(vec.getModel() instanceof DefaultComboBoxModel, "vector ctor mutable DefaultComboBoxModel");
        checkEquals(2, vec.getItemCount(), "vector ctor count 2");
        checkEquals("v2", vec.getItemAt(1), "vector ctor getElementAt");

        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>(
            new String[] {"m1"});
        JComboBox<String> md = new JComboBox<String>(m);
        check(md.getModel() == m, "model ctor keeps model identity");
        checkEquals(1, md.getItemCount(), "model ctor count");

        check(new JComboBox<String>().getModel().getClass().getName()
              .equals("javax.swing.DefaultComboBoxModel"), "plain ctor DefaultComboBoxModel");
    }

    // ---- selection semantics + ActionEvent/ItemEvent payloads ----
    static void testSelectionEvents() {
        JComboBox<String> cb = new JComboBox<String>(new String[] {"a", "b", "c"});
        Act act = new Act();
        cb.addActionListener(act);
        Item item = new Item();
        cb.addItemListener(item);

        checkEquals(0, cb.getSelectedIndex(), "initial auto-selection");
        act.reset();
        cb.setSelectedIndex(2);
        checkEquals(2, cb.getSelectedIndex(), "setSelectedIndex(2)");
        checkEquals("c", cb.getSelectedItem(), "selectedItem c");
        check(act.count == 1, "one ActionEvent per selection change");
        checkEquals("comboBoxChanged", act.last.getActionCommand(), "ActionEvent command");
        check(item.count == 2, "selection change fires 2 ItemEvents (deselect+select)");
        checkEquals("DESELECTED:a;SELECTED:c;", item.seq, "ItemEvent sequence old->new");

        act.reset();
        cb.setSelectedItem("a");
        checkEquals("a", cb.getSelectedItem(), "setSelectedItem(a)");
        checkEquals(0, cb.getSelectedIndex(), "setSelectedItem updates index");
        check(act.count == 1 && "comboBoxChanged".equals(act.last.getActionCommand()),
              "setSelectedItem fires one comboBoxChanged");

        // legal clear: contrast with JList, -1 is a valid index here
        act.reset();
        cb.setSelectedIndex(-1);
        checkEquals(-1, cb.getSelectedIndex(), "setSelectedIndex(-1) clears");
        check(cb.getSelectedItem() == null, "selectedItem null after clear");
        check(act.count == 1, "clear fires one ActionEvent");

        // absent value on non-editable combo: rejected, selection kept
        cb.setSelectedIndex(1);
        act.reset();
        cb.setSelectedItem("nope");
        checkEquals("b", cb.getSelectedItem(), "non-editable rejects unknown item, keeps selection");
        checkEquals(1, cb.getSelectedIndex(), "index kept after rejected item");
        check(act.count == 0, "rejected item fires no ActionEvent");

        // bounds
        checkThrows(IllegalArgumentException.class, "setSelectedIndex(99)",
            new Runnable() { public void run() { new JComboBox<String>(new String[] {"a"}).setSelectedIndex(99); }});
        checkThrows(IllegalArgumentException.class, "setSelectedIndex(-2)",
            new Runnable() { public void run() { new JComboBox<String>(new String[] {"a"}).setSelectedIndex(-2); }});

        Object[] so = cb.getSelectedObjects();
        check(so != null && so.length == 1 && "b".equals(so[0]), "getSelectedObjects array with b");
    }

    // ---- editable/editor/config properties ----
    static void testEditableAndConfig() {
        JComboBox<String> cb = new JComboBox<String>(new String[] {"a", "b", "c"});
        Act act = new Act();
        cb.addActionListener(act);
        cb.setEditable(true);
        check(cb.isEditable(), "setEditable(true)");
        check(cb.getEditor() != null, "editor non-null when editable");
        check(act.count == 0, "setEditable fires no ActionEvent");

        ComboBoxEditor ed = cb.getEditor();
        cb.setEditor(ed);
        check(cb.getEditor() == ed, "setEditor(same) identity");

        cb.setMaximumRowCount(5);
        checkEquals(5, cb.getMaximumRowCount(), "setMaximumRowCount(5)");
        cb.setMaximumRowCount(0);
        checkEquals(0, cb.getMaximumRowCount(), "setMaximumRowCount(0) legal, no validation");

        cb.setPrototypeDisplayValue("xx");
        checkEquals("xx", cb.getPrototypeDisplayValue(), "setPrototypeDisplayValue roundtrip");

        cb.setActionCommand("CC");
        checkEquals("CC", cb.getActionCommand(), "setActionCommand roundtrip");

        ListCellRenderer<? super String> r = cb.getRenderer();
        cb.setRenderer(r);
        check(cb.getRenderer() == r, "setRenderer identity");

        cb.setLightWeightPopupEnabled(false);
        check(!cb.isLightWeightPopupEnabled(), "setLightWeightPopupEnabled(false)");
        cb.setLightWeightPopupEnabled(true);
        check(cb.isLightWeightPopupEnabled(), "setLightWeightPopupEnabled(true) restore");

        cb.setEnabled(false);
        check(!cb.isEnabled(), "setEnabled(false)");
        cb.setEnabled(true);
        check(cb.isEnabled(), "setEnabled(true) restore");

        // null listener registration is a benign no-op
        cb.addActionListener(null);
        check(true, "addActionListener(null) no-throw");
        cb.removeItemListener(null);
        check(true, "removeItemListener(null) no-throw");

        // setModel(null): NPE, and the instance must not be reused afterwards
        checkThrows(NullPointerException.class, "setModel(null)",
            new Runnable() { public void run() { new JComboBox<String>().setModel(null); }});
        DefaultComboBoxModel<String> m2 = new DefaultComboBoxModel<String>(
            new String[] {"z"});
        cb.setModel(m2);
        check(cb.getModel() == m2, "setModel identity");
        checkEquals(1, cb.getItemCount(), "model swap count");
    }

    // ---- mutator matrix: mutable vs non-mutable model ----
    static void testMutators() {
        JComboBox<String> cb = new JComboBox<String>(new String[] {"a"});
        cb.addItem("b");
        checkEquals(2, cb.getItemCount(), "addItem count 2");
        cb.insertItemAt("z", 1);
        checkEquals("z", cb.getItemAt(1), "insertItemAt places z at 1");
        cb.removeItemAt(0);
        checkEquals(2, cb.getItemCount(), "removeItemAt(0) removes first of three");
        cb.removeItem("z");
        checkEquals(1, cb.getItemCount(), "removeItem(existing) leaves one");

        cb.removeItem("absent");
        checkEquals(1, cb.getItemCount(), "removeItem(absent) no-throw no-op");

        Act act = new Act();
        cb.addActionListener(act);
        cb.removeAllItems();
        checkEquals(0, cb.getItemCount(), "removeAllItems count 0");
        checkEquals(-1, cb.getSelectedIndex(), "removeAllItems clears selection");
        check(act.count == 1, "removeAllItems fires exactly one ActionEvent");

        JComboBox<String> fresh = new JComboBox<String>();
        fresh.addItem("solo");
        checkEquals(0, fresh.getSelectedIndex(), "addItem into empty combo auto-selects it");
        checkEquals("solo", fresh.getSelectedItem(), "auto-selected item readable");

        // bounds on indexed mutators: AIOOBE both ends, count unchanged
        JComboBox<String> b = new JComboBox<String>(new String[] {"a", "b"});
        checkThrows(ArrayIndexOutOfBoundsException.class, "removeItemAt(99)",
            new Runnable() { public void run() { b.removeItemAt(99); }});
        checkThrows(ArrayIndexOutOfBoundsException.class, "removeItemAt(-1)",
            new Runnable() { public void run() { b.removeItemAt(-1); }});
        checkThrows(ArrayIndexOutOfBoundsException.class, "insertItemAt(x,99)",
            new Runnable() { public void run() { b.insertItemAt("x", 99); }});
        checkEquals(2, b.getItemCount(), "failed indexed mutations leave count");
        check(b.getItemAt(99) == null, "getItemAt(99) null");
        check(b.getItemAt(-1) == null, "getItemAt(-1) null");

        // fixed model: every mutator rejected with RuntimeException, count stable
        JComboBox<String> fx = new JComboBox<String>(new FixedModel());
        checkEquals(2, fx.getItemCount(), "fixed model count");
        checkThrows(RuntimeException.class, "addItem on fixed model",
            new Runnable() { public void run() { fx.addItem("q"); }});
        checkThrows(RuntimeException.class, "removeItem on fixed model",
            new Runnable() { public void run() { fx.removeItem("x"); }});
        checkThrows(RuntimeException.class, "removeAllItems on fixed model",
            new Runnable() { public void run() { fx.removeAllItems(); }});
        checkThrows(RuntimeException.class, "insertItemAt on fixed model",
            new Runnable() { public void run() { fx.insertItemAt("q", 0); }});
        checkEquals(2, fx.getItemCount(), "fixed model count unchanged after rejections");
    }
}
