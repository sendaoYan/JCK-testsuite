/*
 * @test
 * @summary javax.swing.JList conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JListTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Vector;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class JListTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JList");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.Scrollable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
        // supplementary structural checks
        check(c.getTypeParameters().length == 1, "one type parameter <E>");
        check(c.getDeclaredField("VERTICAL").getInt(null) == 0, "VERTICAL == 0");
        check(c.getDeclaredField("VERTICAL_WRAP").getInt(null) == 1, "VERTICAL_WRAP == 1");
        check(c.getDeclaredField("HORIZONTAL_WRAP").getInt(null) == 2, "HORIZONTAL_WRAP == 2");
        Class<?> drop = Class.forName("javax.swing.JList$DropLocation");
        check(drop.getSuperclass().getName().equals("javax.swing.TransferHandler$DropLocation"), "DropLocation superclass");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JFrame frame = new JFrame("JListTest");
        try {
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            Vector<String> items = new Vector<>(java.util.Arrays.asList("alpha", "beta", "gamma", "delta"));
            JList<String> list = new JList<>(items);
            frame.add(list);
            frame.pack();

            // fixed items + model identity/content (no concrete anonymous class asserted)
            ListModel<String> model = list.getModel();
            check(model != null, "model non-null");
            check(model instanceof AbstractListModel, "vector ctor wraps model in AbstractListModel");
            check(model.getSize() == 4, "model size 4");
            check("gamma".equals(model.getElementAt(2)), "model element 2");

            JList<String> arrList = new JList<>(new String[]{"one", "two"});
            check(arrList.getModel().getSize() == 2, "array ctor model size 2");
            check("two".equals(arrList.getModel().getElementAt(1)), "array ctor element 1");
            check(arrList.getCellRenderer() != null, "cell renderer non-null");
            check(ListCellRenderer.class.isInstance(arrList.getCellRenderer()), "renderer is ListCellRenderer");

            // empty selection initial state
            check(list.getSelectedIndex() == -1, "initial selected index -1");
            check(list.isSelectionEmpty(), "initial selection empty");
            check(list.getSelectedValue() == null, "initial selected value null");
            check(list.getSelectedValuesList().isEmpty(), "initial selected values empty");
            check(list.getSelectedIndices().length == 0, "initial selected indices empty");

            // selection listener recording: programmatic setSelectedIndex fires
            final java.util.List<ListSelectionEvent> fired = new java.util.ArrayList<>();
            list.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) { fired.add(e); }
            });
            list.setSelectedIndex(2);
            check(fired.size() == 1, "setSelectedIndex fires exactly one event");
            check(!fired.get(0).getValueIsAdjusting(), "event not adjusting");
            check(fired.get(0).getFirstIndex() == 2 && fired.get(0).getLastIndex() == 2, "event range 2..2");
            check(fired.get(0).getSource() == list, "event source is the JList");
            check(list.getSelectedIndex() == 2, "selected index 2");
            check(list.isSelectedIndex(2), "isSelectedIndex(2)");
            check(!list.isSelectedIndex(0), "!isSelectedIndex(0)");
            check("gamma".equals(list.getSelectedValue()), "selected value gamma");
            List<String> one = list.getSelectedValuesList();
            check(one.size() == 1 && "gamma".equals(one.get(0)), "selected values list [gamma]");
            list.setSelectedIndex(2);           // same index: no new event
            check(fired.size() == 1, "re-select same index fires nothing");

            // multi selection via setSelectedIndices
            list.setSelectedIndices(new int[]{0, 3});
            check(list.getSelectedIndices().length == 2
                    && list.getSelectedIndices()[0] == 0 && list.getSelectedIndices()[1] == 3, "selected indices {0,3}");
            check(list.getMinSelectionIndex() == 0, "min selection index 0");
            check(list.getMaxSelectionIndex() == 3, "max selection index 3");
            List<String> two = list.getSelectedValuesList();
            check(two.size() == 2 && "alpha".equals(two.get(0)) && "delta".equals(two.get(1)), "selected values list [alpha,delta]");

            // clearSelection resets and notifies
            fired.clear();
            list.clearSelection();
            check(list.isSelectionEmpty() && list.getSelectedIndex() == -1, "clearSelection empties");
            check(fired.size() == 1, "clearSelection fired one event");

            // visible row count / layout orientation
            list.setVisibleRowCount(7);
            check(list.getVisibleRowCount() == 7, "visible row count roundtrip");
            list.setLayoutOrientation(JList.VERTICAL_WRAP);
            check(list.getLayoutOrientation() == JList.VERTICAL_WRAP, "layout orientation VERTICAL_WRAP");
            list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
            check(list.getLayoutOrientation() == JList.HORIZONTAL_WRAP, "layout orientation HORIZONTAL_WRAP");
            boolean iae = false;
            try { list.setLayoutOrientation(77); }
            catch (IllegalArgumentException e) { iae = true; }
            check(iae, "setLayoutOrientation(77) throws IAE");
            check(list.getLayoutOrientation() == JList.HORIZONTAL_WRAP, "orientation unchanged after IAE");

            // selection model swap keeps user listeners wired
            DefaultListSelectionModel dsm = new DefaultListSelectionModel();
            list.setSelectionModel(dsm);
            check(list.getSelectionModel() == dsm, "selection model same instance");
            fired.clear();
            list.setSelectedIndex(1);
            check(fired.size() == 1, "listener still fires after selection model swap");
            check(list.getSelectedIndex() == 1 && "beta".equals(list.getSelectedValue()), "selection via new model");
            boolean iae2 = false;
            try { list.setSelectionModel(null); }
            catch (IllegalArgumentException e) { iae2 = true; }
            check(iae2, "setSelectionModel(null) throws IAE");

            // selection mode behavior through the list
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            check(list.getSelectionMode() == ListSelectionModel.SINGLE_SELECTION, "selection mode SINGLE");
            list.setSelectedIndices(new int[]{0, 2});
            check(list.getSelectedIndices().length == 1 && list.getSelectedIndices()[0] == 2,
                    "SINGLE mode keeps only last index of setSelectedIndices");

            // setModel resets selection (source: setModel -> clearSelection)
            javax.swing.DefaultListModel<String> dlm = new javax.swing.DefaultListModel<>();
            dlm.addElement("x"); dlm.addElement("y");
            list.setSelectedIndex(0);
            list.setModel(dlm);
            check(list.getModel() == dlm, "model swapped to DefaultListModel");
            check(list.getSelectedIndex() == -1, "setModel clears selection");
            check(list.isSelectionEmpty(), "selection empty after setModel");
            list.setSelectedValue("y", true);
            check("y".equals(list.getSelectedValue()), "setSelectedValue after model swap");
            boolean iae3 = false;
            try { list.setModel(null); }
            catch (IllegalArgumentException e) { iae3 = true; }
            check(iae3, "setModel(null) throws IAE");

            // setListData swaps to a fresh fixed model
            list.setListData(new String[]{"p", "q"});
            check(list.getModel().getSize() == 2 && "q".equals(list.getModel().getElementAt(1)),
                    "setListData(array) installs fixed model");

            // prototype cell value roundtrip
            list.setPrototypeCellValue("prototype");
            check("prototype".equals(list.getPrototypeCellValue()), "prototype cell value roundtrip");

            // tolerate scroll request (no assertion on geometry)
            list.ensureIndexIsVisible(0);
            passed++;  // counted as one scenario completion check
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JListTest::testWhiteBox);
        runOnEDT(JListTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
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
}
