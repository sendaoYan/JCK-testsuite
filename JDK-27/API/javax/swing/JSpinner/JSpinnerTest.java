/*
 * @test
 * @summary javax.swing.JSpinner conformance: white-box structure + black-box behavior
 *          + deep component-layer contracts (editor property events, boundary stepping, class pairing).
 *          + deep component-layer contracts (editor property events, boundary stepping, class pairing).
 * @key headful
 * @run main JSpinnerTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

public class JSpinnerTest {
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

    public static void main(String[] args) throws Exception {
        runOnEDT(JSpinnerTest::testWhiteBox);
        runOnEDT(JSpinnerTest::testBlackBox);
        runOnEDT(JSpinnerTest::testDeepContracts);
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deep component-layer contracts (editor property events, boundary stepping, class pairing) ----
    static void testDeepContracts() throws Exception {
        // null rejection matrix
        try {
            new JSpinner().setModel(null);
            check(false, "setModel(null) must throw");
        } catch (IllegalArgumentException e) { check(true, "setModel(null) IAE"); }
        try {
            new JSpinner().setEditor(null);
            check(false, "setEditor(null) must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("null editor"), "setEditor(null) IAE 'null editor'");
        }

        JSpinner s = new JSpinner();
        final int[] chg = new int[1];
        final java.beans.PropertyChangeEvent[] pce = new java.beans.PropertyChangeEvent[1];
        final int[] propn = new int[1];
        s.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) { chg[0]++; }
        });
        s.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) { propn[0]++; pce[0] = e; }
        });

        s.setValue(5);
        check(chg[0] == 1, "setValue fires one ChangeEvent");
        s.setValue(5);
        check(chg[0] == 1, "same-value setValue fires nothing");

        Object oldEditor = s.getEditor();
        JLabel custom = new JLabel("ed");
        s.setEditor(custom);
        check(s.getEditor() == custom, "setEditor identity");
        check(propn[0] == 1, "setEditor fires one PropertyChangeEvent");
        check("editor".equals(pce[0].getPropertyName()), "property name editor");
        check(pce[0].getOldValue() == oldEditor && pce[0].getNewValue() == custom, "editor event old/new values");
        check(chg[0] == 1, "setEditor fires no ChangeEvent");
        propn[0] = 0;
        s.setEditor(custom);
        check(propn[0] == 0, "same-editor re-set fires nothing");
        s.setModel(s.getModel());
        check(chg[0] == 1, "same-model re-set fires no ChangeEvent");

        // stepping semantics: value+step with NO clamping (overshoot -> null)
        JSpinner num = new JSpinner(new SpinnerNumberModel(5, -10, 10, 2));
        num.setValue(9);
        check(num.getNextValue() == null, "next null from 9 with step 2 (9+2=11 > max)");
        num.setValue(8);
        check(Integer.valueOf(10).equals(num.getNextValue()), "next 10 from 8 (8+2 exact)");

        // model/editor class pairing + automatic re-pairing on model swap
        check("javax.swing.JSpinner$NumberEditor".equals(new JSpinner().getEditor().getClass().getName()),
                "default editor NumberEditor");
        JSpinner date = new JSpinner(new javax.swing.SpinnerDateModel());
        check("javax.swing.JSpinner$DateEditor".equals(date.getEditor().getClass().getName()),
                "date model pairs DateEditor");
        JSpinner list = new JSpinner(new javax.swing.SpinnerListModel(java.util.Arrays.asList("x", "y", "z")));
        check("javax.swing.JSpinner$ListEditor".equals(list.getEditor().getClass().getName()),
                "list model pairs ListEditor");
        list.setValue("z");
        check(list.getNextValue() == null && "y".equals(list.getPreviousValue()),
                "list boundary: next null at last, previous y");
        list.setModel(new javax.swing.SpinnerDateModel());
        check("javax.swing.JSpinner$DateEditor".equals(list.getEditor().getClass().getName()),
                "model swap swaps editor class");

        check(s.getUI() != null && s.getUI().getClass().getName().endsWith("BasicSpinnerUI"),
                "Metal binding installs BasicSpinnerUI");
        check("SpinnerUI".equals(s.getUIClassID()), "getUIClassID SpinnerUI");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JSpinner");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.accessibility.Accessible"), "interface 0");
    }

    static void testBlackBox() throws Exception {
        JSpinner def = new JSpinner();
        check(def.getValue() instanceof Integer, "default value Integer");
        check(((Integer) def.getValue()).intValue() == 0, "default value 0");
        check(((Integer) def.getNextValue()).intValue() == 1, "default next 1");
        check(((Integer) def.getPreviousValue()).intValue() == -1, "default previous -1");
        check(def.getModel() != null, "default model non-null");
        check(def.getModel() instanceof SpinnerNumberModel, "default model number model");
        check(def.getEditor() instanceof JSpinner.DefaultEditor, "default editor DefaultEditor");

        SpinnerNumberModel num = new SpinnerNumberModel(5, 0, 10, 2);
        JSpinner sp = new JSpinner(num);
        check(sp.getModel() == num, "number model ctor");
        check(((Integer) sp.getValue()).intValue() == 5, "number value 5");
        check(((Integer) sp.getNextValue()).intValue() == 7, "number next 7");
        check(((Integer) sp.getPreviousValue()).intValue() == 3, "number previous 3");

        sp.setValue(Integer.valueOf(9));
        check(((Integer) sp.getValue()).intValue() == 9, "setValue 9");
        check(sp.getNextValue() == null, "next null at max edge");
        sp.setValue(Integer.valueOf(0));
        check(sp.getPreviousValue() == null, "previous null at min edge");

        boolean iae = false;
        try { sp.setValue("not-a-number"); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "setValue non-number IAE");

        SpinnerNumberModel other = new SpinnerNumberModel(100, 0, 1000, 5);
        sp.setModel(other);
        check(sp.getModel() == other, "setModel");
        check(((Integer) sp.getValue()).intValue() == 100, "new model value 100");

        JLabel customEditor = new JLabel("editor");
        sp.setEditor(customEditor);
        check(sp.getEditor() == customEditor, "setEditor/getEditor");

        JSpinner empty = new JSpinner();
        empty.commitEdit(); // no exception expected
        check(true, "commitEdit ok");
        check(empty.getEditor() != null, "editor non-null after commit");
    }
}
