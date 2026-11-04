/*
 * @test
 * @summary javax.swing.JProgressBar conformance: type shape via reflection + component-layer
 *          black-box contracts (defaults, ctor matrix, clamped values, percent strings,
 *          string/indeterminate roundtrips, ChangeEvents, model swap, orientation negative).
 * @run main JProgressBarTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JProgressBarTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkEquals(Object expect, Object actual, String m) {
        check(expect == null ? actual == null : expect.equals(actual),
              m + ": expected " + expect + " but got " + actual);
    }

    static void checkEqualsDouble(double expect, double actual, String m) {
        check(Math.abs(expect - actual) < 1e-9, m + ": expected " + expect + " but got " + actual);
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

    public static void main(String[] args) throws Exception {
        final Class<?> c = Class.forName("javax.swing.JProgressBar", false, JProgressBarTest.class.getClassLoader());
        EventQueue.invokeAndWait(new Runnable() { public void run() { testShape(c); }});
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        EventQueue.invokeAndWait(new Runnable() { public void run() {
            testDefaults();
            testCtors();
            testValuesAndStrings();
            testModelAndEvents();
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
        check(hasInterface(c, "javax.swing.SwingConstants"), "implements javax.swing.SwingConstants");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getOrientation"), "method getOrientation");
        check(names.contains("setOrientation"), "method setOrientation");
        check(names.contains("isStringPainted"), "method isStringPainted");
    }

    // ---- defaults ----
    static void testDefaults() {
        JProgressBar pb = new JProgressBar();
        checkEquals(0, pb.getMinimum(), "default min 0");
        checkEquals(100, pb.getMaximum(), "default max 100");
        checkEquals(0, pb.getValue(), "default value 0");
        checkEquals(SwingConstants.HORIZONTAL, pb.getOrientation(), "default orientation HORIZONTAL");
        check(!pb.isStringPainted(), "default stringPainted false");
        check(!pb.isIndeterminate(), "default indeterminate false");
        checkEquals("0%", pb.getString(), "default string 0%");
        checkEqualsDouble(0.0, pb.getPercentComplete(), "default percent 0.0");
        check(pb.getModel() instanceof DefaultBoundedRangeModel, "default model DefaultBoundedRangeModel");
        check(pb.getUI() != null && pb.getUI().getClass().getName().endsWith("MetalProgressBarUI"),
              "Metal binding installs MetalProgressBarUI");
        checkEquals("ProgressBarUI", pb.getUIClassID(), "getUIClassID ProgressBarUI");
    }

    // ---- ctor matrix ----
    static void testCtors() {
        checkEquals(SwingConstants.VERTICAL, new JProgressBar(SwingConstants.VERTICAL).getOrientation(),
              "VERTICAL ctor");
        JProgressBar mm = new JProgressBar(5, 15);
        checkEquals(5, mm.getMinimum(), "min-max ctor min");
        checkEquals(15, mm.getMaximum(), "min-max ctor max");
        checkEquals(5, mm.getValue(), "min-max ctor value starts at min");
        JProgressBar vmm = new JProgressBar(SwingConstants.VERTICAL, 5, 15);
        checkEquals(SwingConstants.VERTICAL, vmm.getOrientation(), "VERTICAL-min-max ctor orientation");
        checkEquals(5, vmm.getValue(), "VERTICAL-min-max ctor value");
        BoundedRangeModel m = new DefaultBoundedRangeModel(6, 0, 5, 15);
        JProgressBar pm = new JProgressBar(m);
        check(pm.getModel() == m, "model ctor identity");
        checkEquals(6, pm.getValue(), "model ctor value 6");
        checkEquals(5, pm.getMinimum(), "model ctor min");
        checkEquals(15, pm.getMaximum(), "model ctor max");
    }

    // ---- value clamping + percent/string contracts ----
    static void testValuesAndStrings() {
        JProgressBar pb = new JProgressBar();
        pb.setValue(50);
        checkEquals(50, pb.getValue(), "in-range setValue(50)");
        checkEqualsDouble(0.5, pb.getPercentComplete(), "percent 0.5");
        checkEquals("50%", pb.getString(), "derived string 50% at midpoint");

        pb.setValue(500);
        checkEquals(100, pb.getValue(), "setValue(500) clamps to max");
        checkEqualsDouble(1.0, pb.getPercentComplete(), "percent 1.0 at clamp");
        checkEquals("100%", pb.getString(), "derived string 100% after clamp");
        pb.setValue(-9);
        checkEquals(0, pb.getValue(), "setValue(-9) clamps to min");
        checkEquals("0%", pb.getString(), "derived string back to 0%");

        pb.setValue(25);
        checkEqualsDouble(0.25, pb.getPercentComplete(), "percent 0.25");

        pb.setString("done");
        checkEquals("done", pb.getString(), "custom string overrides derived");
        pb.setValue(75);
        checkEquals("done", pb.getString(), "custom string stable across value changes");
        pb.setString(null);
        checkEquals("75%", pb.getString(), "null string restores derived");

        pb.setStringPainted(true);
        check(pb.isStringPainted(), "setStringPainted(true)");
        pb.setIndeterminate(true);
        check(pb.isIndeterminate(), "setIndeterminate(true)");
        pb.setIndeterminate(false);
        check(!pb.isIndeterminate(), "setIndeterminate(false)");
        check(pb.isStringPainted(), "stringPainted kept across indeterminate toggle");

        pb.setOrientation(SwingConstants.VERTICAL);
        checkEquals(SwingConstants.VERTICAL, pb.getOrientation(), "setOrientation(VERTICAL)");
        checkThrows(IllegalArgumentException.class, "setOrientation(99)",
            new Runnable() { public void run() { pb.setOrientation(99); }});
        checkEquals(SwingConstants.VERTICAL, pb.getOrientation(), "orientation kept after rejection");
    }

    // ---- model swap + ChangeEvents ----
    static void testModelAndEvents() {
        JProgressBar pb = new JProgressBar();
        Chg chg = new Chg();
        pb.addChangeListener(chg);
        pb.setValue(40);
        checkEquals(40, pb.getValue(), "setValue(40)");
        check(chg.count == 1, "setValue fires one ChangeEvent");
        check(chg.last != null && chg.last.getSource() == pb, "ChangeEvent source is the bar");
        pb.setValue(40);
        check(chg.count == 1, "same-value setValue fires no new ChangeEvent");

        DefaultBoundedRangeModel m = new DefaultBoundedRangeModel(7, 0, 0, 9);
        pb.setModel(m);
        check(pb.getModel() == m, "setModel identity");
        checkEquals(7, pb.getValue(), "value from swapped model");
        chg.count = 0;
        m.setValue(8);
        checkEquals(8, pb.getValue(), "model mutation visible");
        check(chg.count == 1, "model mutation fires ChangeEvent via bar");

        // JProgressBar.setModel(null) is ACCEPTED (contrast with JSlider's NPE):
        // the model field becomes null and any later model-touching op NPEs
        JProgressBar nb = new JProgressBar();
        nb.setModel(null);
        check(nb.getModel() == null, "setModel(null) accepted, getModel null");
        checkThrows(NullPointerException.class, "setValue after null model",
            new Runnable() { public void run() { nb.setValue(3); }});

        pb.setMinimum(2);
        checkEquals(2, pb.getMinimum(), "setMinimum(2)");
        pb.setMaximum(9);
        checkEquals(9, pb.getMaximum(), "setMaximum(9)");
    }
}
