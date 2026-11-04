/*
 * @test
 * @summary javax.swing.JSpinner conformance: type shape via reflection + component-layer
 *          black-box contracts (defaults, model/editor pairing, ChangeEvent vs
 *          PropertyChangeEvent("editor") semantics, boundary values, null/negative matrix).
 * @run main JSpinnerTest
 */

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JSpinnerTest {
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

    static class Chg implements ChangeListener {
        int count; ChangeEvent last;
        public void stateChanged(ChangeEvent e) { count++; last = e; }
    }

    static class Prop implements PropertyChangeListener {
        int count; PropertyChangeEvent last;
        public void propertyChange(PropertyChangeEvent e) { count++; last = e; }
    }

    public static void main(String[] args) throws Exception {
        final Class<?> c = Class.forName("javax.swing.JSpinner", false, JSpinnerTest.class.getClassLoader());
        EventQueue.invokeAndWait(new Runnable() { public void run() { testShape(c); }});
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        EventQueue.invokeAndWait(new Runnable() { public void run() {
            testDefaults();
            testNumberModel();
            testListAndDateModels();
            testEditorAndEvents();
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
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getUI"), "method getUI");
        check(names.contains("setUI"), "method setUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("setModel"), "method setModel");
        check(names.contains("getModel"), "method getModel");
    }

    // ---- defaults: SpinnerNumberModel 0 step 1, NumberEditor, BasicSpinnerUI ----
    static void testDefaults() {
        JSpinner s = new JSpinner();
        checkEquals(0, s.getValue(), "default value 0");
        checkEquals(1, s.getNextValue(), "default next 1");
        checkEquals(-1, s.getPreviousValue(), "default previous -1");
        checkEquals(0, s.getValue(), "getNext/Previous are pure queries");
        check(s.getModel() instanceof SpinnerNumberModel, "default model SpinnerNumberModel");
        checkEquals("javax.swing.JSpinner$NumberEditor", s.getEditor().getClass().getName(),
              "default editor NumberEditor");
        check(s.getUI() != null && s.getUI().getClass().getName().endsWith("BasicSpinnerUI"),
              "Metal binding installs BasicSpinnerUI");
        checkEquals("SpinnerUI", s.getUIClassID(), "getUIClassID SpinnerUI");
        check(s.getAccessibleContext() != null, "getAccessibleContext non-null");

        try {
            s.commitEdit();
            check(true, "commitEdit no-throw with unchanged text");
        } catch (java.text.ParseException pe) {
            throw new AssertionError("commitEdit failed: " + pe);
        }
        checkEquals(0, s.getValue(), "commitEdit keeps value");
    }

    // ---- SpinnerNumberModel stepping, bounds and typed-value rejection ----
    static void testNumberModel() {
        JSpinner s = new JSpinner(new SpinnerNumberModel(5, -10, 10, 2));
        checkEquals(5, s.getValue(), "ctor model value 5");
        checkEquals(7, s.getNextValue(), "step +2 next");
        checkEquals(3, s.getPreviousValue(), "step -2 previous");
        s.setValue(9);
        checkEquals(9, s.getValue(), "setValue(9)");
        // stepping is value+step with NO clamping: 9+2=11 > max 10 -> null
        check(s.getNextValue() == null, "next null from 9 with step 2 (overshoots max)");
        s.setValue(8);
        checkEquals(10, s.getNextValue(), "next 10 from 8 (8+2 exact)");
        s.setValue(10);
        check(s.getNextValue() == null, "next null at upper bound");
        s.setValue(-10);
        check(s.getPreviousValue() == null, "previous null at lower bound");

        checkThrows(IllegalArgumentException.class, "setValue(not-a-number)",
            new Runnable() { public void run() { s.setValue("not-a-number"); }});
        checkEquals(-10, s.getValue(), "value kept after rejected set");

        checkThrows(IllegalArgumentException.class, "setModel(null)",
            new Runnable() { public void run() { new JSpinner().setModel(null); }});
        SpinnerNumberModel m = new SpinnerNumberModel(1, 0, 5, 1);
        s.setModel(m);
        check(s.getModel() == m, "setModel identity");
        checkEquals(1, s.getValue(), "value from swapped model");
    }

    // ---- list/date models drive editor pairing ----
    static void testListAndDateModels() {
        JSpinner l = new JSpinner(new SpinnerListModel(Arrays.asList("x", "y", "z")));
        check(l.getModel() instanceof SpinnerListModel, "list model instance");
        checkEquals("javax.swing.JSpinner$ListEditor", l.getEditor().getClass().getName(),
              "list editor ListEditor");
        checkEquals("x", l.getValue(), "list default first element");
        checkEquals("y", l.getNextValue(), "list next y");
        l.setValue("z");
        check(l.getNextValue() == null, "list next null at last element");
        checkEquals("y", l.getPreviousValue(), "list previous y");

        JSpinner d = new JSpinner(new SpinnerDateModel());
        check(d.getModel() instanceof SpinnerDateModel, "date model instance");
        checkEquals("javax.swing.JSpinner$DateEditor", d.getEditor().getClass().getName(),
              "date editor DateEditor");
        check(d.getValue() instanceof java.util.Date, "date value is Date");

        // swapping models re-creates the matching editor automatically
        l.setModel(new SpinnerDateModel());
        checkEquals("javax.swing.JSpinner$DateEditor", l.getEditor().getClass().getName(),
              "model swap swaps editor class");
    }

    // ---- editor lifecycle: null rejection, property event, quiet re-set ----
    static void testEditorAndEvents() {
        JSpinner s = new JSpinner();
        Chg chg = new Chg();
        s.addChangeListener(chg);
        Prop prop = new Prop();
        s.addPropertyChangeListener(prop);

        s.setValue(5);
        checkEquals(5, s.getValue(), "setValue(5)");
        check(chg.count == 1, "setValue fires one ChangeEvent");
        check(chg.last != null && chg.last.getSource() == s, "ChangeEvent source is spinner");
        s.setValue(5);
        check(chg.count == 1, "same-value setValue fires no new ChangeEvent");

        checkThrows(IllegalArgumentException.class, "setEditor(null)",
            new Runnable() { public void run() { s.setEditor(null); }});
        Object oldEditor = s.getEditor();

        JLabel custom = new JLabel("ed");
        prop.count = 0;
        s.setEditor(custom);
        check(s.getEditor() == custom, "setEditor identity");
        check(prop.count == 1, "setEditor fires one PropertyChangeEvent");
        checkEquals("editor", prop.last.getPropertyName(), "property name editor");
        check(prop.last.getOldValue() == oldEditor && prop.last.getNewValue() == custom,
              "editor event old/new values");
        check(chg.count == 1, "setEditor fires no ChangeEvent");

        prop.count = 0;
        s.setEditor(custom);
        check(prop.count == 0, "same-editor re-set fires nothing");
        check(chg.count == 1, "same-editor re-set fires no ChangeEvent");

        s.setModel(s.getModel());
        check(chg.count == 1, "same-model re-set fires no ChangeEvent");

        s.removeChangeListener(chg);
        s.setValue(7);
        checkEquals(7, s.getValue(), "removed listener still allows setValue");
    }
}
