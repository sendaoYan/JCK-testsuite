/*
 * @test
 * @summary javax.swing.JComboBox conformance: white-box structure + black-box behavior
 *          + deep component-layer contracts (ActionEvent counts, bounds matrix, fixed-model rejections).
 * @key headful
 * @run main JComboBoxTest
 */

import java.awt.EventQueue;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

public class JComboBoxTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }

    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JComboBox");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 4, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.ItemSelectable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.swing.event.ListDataListener"), "interface 1");
        check(c.getInterfaces()[2].getName().equals("java.awt.event.ActionListener"), "interface 2");
        check(c.getInterfaces()[3].getName().equals("javax.accessibility.Accessible"), "interface 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Defaults.
        JComboBox<String> cb = new JComboBox<>();
        check(cb.getItemCount() == 0, "default empty");
        check(cb.getSelectedIndex() == -1, "default selectedIndex -1");
        check(cb.getSelectedItem() == null, "default selectedItem null");
        check(cb.getSelectedObjects().length == 0, "default selectedObjects empty");
        check(!cb.isEditable(), "default not editable");
        check(cb.getMaximumRowCount() == 8, "default maximumRowCount 8");
        check("comboBoxChanged".equals(cb.getActionCommand()), "default actionCommand");
        check(cb.getModel() instanceof DefaultComboBoxModel, "default model DefaultComboBoxModel");

        // Array constructor selects first item.
        JComboBox<String> cbArr = new JComboBox<>(new String[]{"a", "b", "c"});
        check(cbArr.getItemCount() == 3, "array ctor count");
        check("a".equals(cbArr.getItemAt(0)), "array ctor item 0");
        check("c".equals(cbArr.getItemAt(2)), "array ctor item 2");
        check("a".equals(cbArr.getSelectedItem()), "array ctor selects first");
        check(cbArr.getSelectedIndex() == 0, "array ctor selectedIndex 0");

        // Vector constructor.
        Vector<String> v = new Vector<>();
        v.add("x"); v.add("y");
        JComboBox<String> cbVec = new JComboBox<>(v);
        check(cbVec.getItemCount() == 2, "vector ctor count");
        check("x".equals(cbVec.getSelectedItem()), "vector ctor selects first");

        // addItem drives auto-selection of the first item.
        cb.addItem("A");
        check(cb.getItemCount() == 1, "addItem count 1");
        check(cb.getSelectedItem().equals("A"), "addItem auto-selects first");
        check(cb.getSelectedIndex() == 0, "addItem selectedIndex 0");
        cb.addItem("B");
        check(cb.getItemCount() == 2, "addItem count 2");
        check("A".equals(cb.getSelectedItem()), "addItem keeps first selection");

        // setSelectedItem / getSelectedItem / getSelectedIndex chain.
        cb.setSelectedItem("B");
        check("B".equals(cb.getSelectedItem()), "setSelectedItem/getSelectedItem");
        check(cb.getSelectedIndex() == 1, "getSelectedIndex after setSelectedItem");

        // Non-editable combo rejects unknown selection.
        cb.setSelectedItem("nonexistent");
        check("B".equals(cb.getSelectedItem()), "non-editable rejects unknown item");

        // setSelectedIndex roundtrip + bounds.
        cb.setSelectedIndex(0);
        check("A".equals(cb.getSelectedItem()), "setSelectedIndex(0)");
        cb.setSelectedIndex(-1);
        check(cb.getSelectedItem() == null, "setSelectedIndex(-1) clears selection");
        check(cb.getSelectedIndex() == -1, "getSelectedIndex -1 after clear");
        try {
            cb.setSelectedIndex(99);
            check(false, "setSelectedIndex(99) should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setSelectedIndex(99) IAE");
        }

        // insertItemAt / getItemAt / removeItemAt chain.
        cb.insertItemAt("Z", 0);
        check(cb.getItemCount() == 3, "insertItemAt count");
        check("Z".equals(cb.getItemAt(0)), "insertItemAt at head");
        cb.removeItemAt(0);
        check(cb.getItemCount() == 2, "removeItemAt count");
        check("A".equals(cb.getItemAt(0)), "removeItemAt shifts");

        // removeItem / removeAllItems.
        cb.setSelectedItem("A");
        cb.removeItem("A");
        check(cb.getItemCount() == 1, "removeItem count");
        check("B".equals(cb.getItemAt(0)), "removeItem remaining item");
        cb.removeAllItems();
        check(cb.getItemCount() == 0, "removeAllItems count 0");
        check(cb.getSelectedItem() == null, "removeAllItems clears selection");

        // Editable roundtrip.
        cb.setEditable(true);
        check(cb.isEditable(), "setEditable(true)");

        // Maximum row count roundtrip.
        cb.setMaximumRowCount(5);
        check(cb.getMaximumRowCount() == 5, "setMaximumRowCount/getMaximumRowCount");

        // Prototype display value roundtrip.
        cb.setPrototypeDisplayValue("proto");
        check("proto".equals(cb.getPrototypeDisplayValue()), "setPrototypeDisplayValue/getPrototypeDisplayValue");

        // Action command roundtrip.
        cb.setActionCommand("cmd");
        check("cmd".equals(cb.getActionCommand()), "setActionCommand/getActionCommand");

        // Light-weight popup roundtrip.
        cb.setLightWeightPopupEnabled(false);
        check(!cb.isLightWeightPopupEnabled(), "setLightWeightPopupEnabled(false)");
        cb.setLightWeightPopupEnabled(true);
        check(cb.isLightWeightPopupEnabled(), "setLightWeightPopupEnabled(true)");

        // Model replacement roundtrip.
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(new String[]{"m1", "m2"});
        cb.setModel(model);
        check(cb.getModel() == model, "setModel/getModel");
        check(cb.getItemCount() == 2, "setModel items visible");
        check("m1".equals(cb.getSelectedItem()), "setModel selects first");

        // Item listener: selection change fires DESELECTED then SELECTED.
        cb.setSelectedItem("m1");
        final List<String> itemEvents = new ArrayList<>();
        cb.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                itemEvents.add((e.getStateChange() == ItemEvent.SELECTED ? "SELECTED:" : "DESELECTED:") + e.getItem());
            }
        });
        cb.setSelectedItem("m2");
        List<String> expected = new ArrayList<>();
        expected.add("DESELECTED:m1");
        expected.add("SELECTED:m2");
        check(expected.equals(itemEvents), "item listener event sequence");

        // getSelectedObjects returns the single selected item.
        Object[] sel = cb.getSelectedObjects();
        check(sel.length == 1 && "m2".equals(sel[0]), "getSelectedObjects");
    }

    // ---- Part 3: deep component-layer contracts (ActionEvent counts, bounds matrix, fixed-model rejections) ----
    static void testDeepContracts() throws Exception {
        JComboBox<String> cb = new JComboBox<>(new String[]{"a", "b", "c"});
        final int[] acts = new int[1];
        final java.awt.event.ActionEvent[] lastAct = new java.awt.event.ActionEvent[1];
        cb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { acts[0]++; lastAct[0] = e; }
        });
        cb.setSelectedIndex(2);
        check(acts[0] == 1, "setSelectedIndex fires one ActionEvent");
        check("comboBoxChanged".equals(lastAct[0].getActionCommand()), "ActionEvent command comboBoxChanged");
        check(lastAct[0].getSource() == cb, "ActionEvent source is the combo box");
        cb.setSelectedItem("a");
        check(acts[0] == 2 && "comboBoxChanged".equals(lastAct[0].getActionCommand()),
                "setSelectedItem fires one ActionEvent with same command");
        cb.setSelectedIndex(-1);
        check(acts[0] == 3 && cb.getSelectedItem() == null, "clear fires one ActionEvent");
        cb.setEditable(true);
        check(acts[0] == 3, "setEditable fires no ActionEvent");
        cb.addActionListener(null);
        check(true, "addActionListener(null) tolerated no-throw");

        // editor: default non-null, same-instance re-set keeps identity
        check(cb.getEditor() != null, "editor non-null when editable");
        javax.swing.ComboBoxEditor ed = cb.getEditor();
        cb.setEditor(ed);
        check(cb.getEditor() == ed, "setEditor(same) identity");

        cb.setMaximumRowCount(0);
        check(cb.getMaximumRowCount() == 0, "setMaximumRowCount(0) accepted, no validation");
        javax.swing.ListCellRenderer<? super String> r = cb.getRenderer();
        cb.setRenderer(r);
        check(cb.getRenderer() == r, "setRenderer identity");
        check(cb.getRenderer() != null, "default renderer non-null");

        cb.setEnabled(false);
        check(!cb.isEnabled(), "setEnabled(false)");
        cb.setEnabled(true);
        check(cb.isEnabled(), "setEnabled(true) restore");

        // indexed bounds matrix: reads return null, writes throw AIOOBE
        JComboBox<String> b = new JComboBox<>(new String[]{"a", "b"});
        check(b.getItemAt(99) == null, "getItemAt(99) null");
        check(b.getItemAt(-1) == null, "getItemAt(-1) null");
        try { b.removeItemAt(99); check(false, "removeItemAt(99) must throw"); }
        catch (ArrayIndexOutOfBoundsException e) { check(true, "removeItemAt(99) AIOOBE"); }
        try { b.removeItemAt(-1); check(false, "removeItemAt(-1) must throw"); }
        catch (ArrayIndexOutOfBoundsException e) { check(true, "removeItemAt(-1) AIOOBE"); }
        try { b.insertItemAt("x", 99); check(false, "insertItemAt(99) must throw"); }
        catch (ArrayIndexOutOfBoundsException e) { check(true, "insertItemAt(99) AIOOBE"); }
        check(b.getItemCount() == 2, "failed mutations leave count");

        // removeAllItems event count
        JComboBox<String> rc = new JComboBox<>(new String[]{"p"});
        final int[] rActs = new int[1];
        rc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) { rActs[0]++; }
        });
        rc.removeAllItems();
        check(rc.getItemCount() == 0 && rc.getSelectedIndex() == -1, "removeAllItems empties");
        check(rActs[0] == 1, "removeAllItems fires exactly one ActionEvent");

        // fixed (non-mutable) model rejects every mutator with RuntimeException
        class FixedModel implements javax.swing.ComboBoxModel<String> {
            private String sel; private final String[] items = {"x", "y"};
            public int getSize() { return items.length; }
            public String getElementAt(int i) { return items[i]; }
            public void setSelectedItem(Object o) { sel = (String) o; }
            public Object getSelectedItem() { return sel; }
            public void addListDataListener(javax.swing.event.ListDataListener l) { }
            public void removeListDataListener(javax.swing.event.ListDataListener l) { }
        }
        JComboBox<String> fx = new JComboBox<>(new FixedModel());
        check(fx.getItemCount() == 2, "fixed model count");
        try { fx.addItem("q"); check(false, "addItem on fixed must throw"); }
        catch (RuntimeException e) { check(true, "addItem on fixed model RuntimeException"); }
        try { fx.removeItem("x"); check(false, "removeItem on fixed must throw"); }
        catch (RuntimeException e) { check(true, "removeItem on fixed model RuntimeException"); }
        try { fx.removeAllItems(); check(false, "removeAllItems on fixed must throw"); }
        catch (RuntimeException e) { check(true, "removeAllItems on fixed model RuntimeException"); }
        try { fx.insertItemAt("q", 0); check(false, "insertItemAt on fixed must throw"); }
        catch (RuntimeException e) { check(true, "insertItemAt on fixed model RuntimeException"); }
        check(fx.getItemCount() == 2, "fixed model count unchanged after rejections");

        // setModel(null): NPE (do not reuse the instance afterwards)
        try {
            new JComboBox<String>().setModel(null);
            check(false, "setModel(null) must throw");
        } catch (NullPointerException e) { check(true, "setModel(null) NPE"); }

        // all standard ctors wire a mutable DefaultComboBoxModel; UI binding
        check(new JComboBox<String>(new String[]{"z"}).getModel() instanceof DefaultComboBoxModel,
                "array ctor model is mutable DefaultComboBoxModel");
        check(new JComboBox<>(new Vector<String>()).getModel() instanceof DefaultComboBoxModel,
                "vector ctor model is mutable DefaultComboBoxModel");
        check(cb.getUI() != null && cb.getUI().getClass().getName().endsWith("MetalComboBoxUI"),
                "Metal binding installs MetalComboBoxUI");
        check("ComboBoxUI".equals(cb.getUIClassID()), "getUIClassID ComboBoxUI");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JComboBoxTest::testWhiteBox);
        runOnEDT(JComboBoxTest::testBlackBox);
        runOnEDT(JComboBoxTest::testDeepContracts);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
