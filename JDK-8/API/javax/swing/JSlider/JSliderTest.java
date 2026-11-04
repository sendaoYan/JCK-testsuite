/*
 * @test
 * @summary javax.swing.JSlider conformance: type shape via reflection + component-layer
 *          black-box contracts (defaults, ctor matrix, min/max clamp cascades,
 *          snap-to-ticks math, ChangeEvent counting, label tables, orientation negatives).
 * @run main JSliderTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JSliderTest {
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
        void reset() { count = 0; last = null; }
    }

    public static void main(String[] args) throws Exception {
        final Class<?> c = Class.forName("javax.swing.JSlider", false, JSliderTest.class.getClassLoader());
        EventQueue.invokeAndWait(new Runnable() { public void run() { testShape(c); }});
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        EventQueue.invokeAndWait(new Runnable() { public void run() {
            testDefaults();
            testCtors();
            testClampCascades();
            testSnapAndEvents();
            testLabelsAndOrientation();
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
        check(names.contains("getUI"), "method getUI");
        check(names.contains("setUI"), "method setUI");
    }

    // ---- defaults ----
    static void testDefaults() {
        JSlider s = new JSlider();
        checkEquals(0, s.getMinimum(), "default min 0");
        checkEquals(100, s.getMaximum(), "default max 100");
        checkEquals(50, s.getValue(), "default value 50");
        checkEquals(0, s.getExtent(), "default extent 0");
        checkEquals(SwingConstants.HORIZONTAL, s.getOrientation(), "default orientation HORIZONTAL");
        check(!s.getInverted(), "default not inverted");
        check(!s.getPaintTicks(), "default paintTicks false");
        check(!s.getPaintLabels(), "default paintLabels false");
        check(s.getPaintTrack(), "default paintTrack true");
        check(!s.getSnapToTicks(), "default snapToTicks false");
        checkEquals(0, s.getMajorTickSpacing(), "default majorTickSpacing 0");
        checkEquals(0, s.getMinorTickSpacing(), "default minorTickSpacing 0");
        check(s.getLabelTable() == null, "default labelTable null");
        check(s.getModel() instanceof DefaultBoundedRangeModel, "default model DefaultBoundedRangeModel");
        check(s.getUI() != null && s.getUI().getClass().getName().endsWith("MetalSliderUI"),
              "Metal binding installs MetalSliderUI");
        checkEquals("SliderUI", s.getUIClassID(), "getUIClassID SliderUI");
        check(!s.getValueIsAdjusting(), "default valueIsAdjusting false");
    }

    // ---- ctor matrix ----
    static void testCtors() {
        checkEquals(15, new JSlider(10, 20).getValue(), "2-arg ctor value==midpoint (10+20)/2");
        JSlider mmv = new JSlider(10, 20, 15);
        checkEquals(15, mmv.getValue(), "3-arg ctor value");
        checkEquals(10, mmv.getMinimum(), "3-arg ctor min");
        checkEquals(20, mmv.getMaximum(), "3-arg ctor max");
        JSlider v = new JSlider(SwingConstants.VERTICAL);
        checkEquals(SwingConstants.VERTICAL, v.getOrientation(), "VERTICAL ctor");
        JSlider full = new JSlider(SwingConstants.VERTICAL, 5, 55, 30);
        checkEquals(SwingConstants.VERTICAL, full.getOrientation(), "4-arg ctor orientation");
        checkEquals(5, full.getMinimum(), "4-arg ctor min");
        checkEquals(55, full.getMaximum(), "4-arg ctor max");
        checkEquals(30, full.getValue(), "4-arg ctor value");
        BoundedRangeModel m = new DefaultBoundedRangeModel(3, 0, 1, 9);
        JSlider ms = new JSlider(m);
        check(ms.getModel() == m, "model ctor identity");
        checkEquals(3, ms.getValue(), "model ctor value");
        checkThrows(IllegalArgumentException.class, "ctor JSlider(99)",
            new Runnable() { public void run() { new JSlider(99); }});
    }

    // ---- value/min/max clamp cascades ----
    static void testClampCascades() {
        JSlider s = new JSlider(); // 0..100, value 50
        s.setValue(200);
        checkEquals(100, s.getValue(), "setValue(200) clamps to max");
        s.setValue(-5);
        checkEquals(0, s.getValue(), "setValue(-5) clamps to min");

        s.setValue(50);
        s.setMinimum(200);
        checkEquals(200, s.getMinimum(), "setMinimum(200) drags min above value");
        checkEquals(200, s.getValue(), "value follows min upward");

        s.setMaximum(150); // now min==200: everything collapses to 150
        checkEquals(150, s.getMaximum(), "setMaximum(150) below min clamps max");
        checkEquals(150, s.getMinimum(), "min follows max down");
        checkEquals(150, s.getValue(), "value follows collapse");

        JSlider neg = new JSlider(-10, 10);
        checkEquals(-10, neg.getMinimum(), "negative min ctor");
        neg.setValue(-7);
        checkEquals(-7, neg.getValue(), "negative value stored");

        s.setValueIsAdjusting(true);
        check(s.getValueIsAdjusting(), "setValueIsAdjusting(true)");
    }

    // ---- snap-to-ticks + ChangeEvent counting ----
    static void testSnapAndEvents() {
        JSlider s = new JSlider();
        s.setMinorTickSpacing(5);
        s.setSnapToTicks(true);
        check(s.getSnapToTicks(), "snapToTicks on");
        s.setValue(3);
        checkEquals(5, s.getValue(), "snap: setValue(3) -> 5");
        s.setValue(4);
        checkEquals(5, s.getValue(), "snap: setValue(4) -> 5");
        s.setValue(2);
        checkEquals(0, s.getValue(), "snap: setValue(2) -> 0");
        s.setSnapToTicks(false);
        s.setValue(3);
        checkEquals(3, s.getValue(), "snap off: setValue(3) stays 3");

        Chg chg = new Chg();
        JSlider q = new JSlider();
        q.addChangeListener(chg);
        q.setValue(60);
        checkEquals(60, q.getValue(), "plain setValue(60)");
        check(chg.count == 1, "setValue fires one ChangeEvent");
        check(chg.last != null && chg.last.getSource() == q, "ChangeEvent source is slider");
        q.setValue(60);
        check(chg.count == 1, "same-value setValue fires no new ChangeEvent");

        // model swap keeps listeners attached to slider-level dispatch
        DefaultBoundedRangeModel m = new DefaultBoundedRangeModel(7, 0, 0, 9);
        q.setModel(m);
        check(q.getModel() == m, "setModel identity");
        checkEquals(7, q.getValue(), "value from swapped model");
        chg.reset();
        m.setValue(8);
        checkEquals(8, q.getValue(), "model mutation visible");
        check(chg.count == 1, "model mutation fires ChangeEvent via slider");
        // JSlider.setModel(null): NPE (contrast with JSpinner's IllegalArgumentException)
        checkThrows(NullPointerException.class, "setModel(null)",
            new Runnable() { public void run() { new JSlider().setModel(null); }});
    }

    // ---- labels + orientation negatives ----
    static void testLabelsAndOrientation() {
        JSlider s = new JSlider(0, 100);
        Dictionary<Integer, ?> table = s.createStandardLabels(10);
        check(table != null, "createStandardLabels(10) non-null");
        int n10 = 0;
        for (Enumeration<Integer> e = table.keys(); e.hasMoreElements(); ) { e.nextElement(); n10++; }
        checkEquals(11, n10, "labels every 10 from 0 to 100 inclusive (11)");

        Dictionary<Integer, ?> t2 = s.createStandardLabels(10, 20);
        int n2 = 0;
        for (Enumeration<Integer> e = t2.keys(); e.hasMoreElements(); ) { e.nextElement(); n2++; }
        checkEquals(9, n2, "labels from start 20 up to 100 (9)");
        check(t2.get(20) != null, "label present at start 20");
        check(t2.get(100) != null, "label present at max 100");
        check(t2.get(0) == null, "no label below start");
        check(t2.get(10) == null, "no label at non-multiple 10 below start");
        check(t2.get(20) != null && t2.get(20) instanceof java.awt.Component, "label values are Components");
        checkThrows(IllegalArgumentException.class, "createStandardLabels(0)",
            new Runnable() { public void run() { s.createStandardLabels(0); }});

        Hashtable<Integer, javax.swing.JComponent> custom =
            new Hashtable<Integer, javax.swing.JComponent>();
        custom.put(0, new javax.swing.JLabel("zero"));
        s.setLabelTable(custom);
        check(s.getLabelTable() == custom, "setLabelTable identity");
        s.setLabelTable(null);
        check(s.getLabelTable() == null, "setLabelTable(null) accepted");

        s.setPaintTicks(true);
        check(s.getPaintTicks(), "setPaintTicks(true)");
        s.setPaintLabels(true);
        check(s.getPaintLabels(), "setPaintLabels(true)");
        s.setPaintTrack(false);
        check(!s.getPaintTrack(), "setPaintTrack(false)");
        s.setInverted(true);
        check(s.getInverted(), "setInverted(true)");
        s.setMajorTickSpacing(10);
        checkEquals(10, s.getMajorTickSpacing(), "setMajorTickSpacing(10)");
        s.setMinorTickSpacing(2);
        checkEquals(2, s.getMinorTickSpacing(), "setMinorTickSpacing(2)");

        s.setOrientation(SwingConstants.VERTICAL);
        checkEquals(SwingConstants.VERTICAL, s.getOrientation(), "setOrientation(VERTICAL)");
        checkThrows(IllegalArgumentException.class, "setOrientation(99)",
            new Runnable() { public void run() { s.setOrientation(99); }});
        checkEquals(SwingConstants.VERTICAL, s.getOrientation(), "orientation kept after rejection");
    }
}
