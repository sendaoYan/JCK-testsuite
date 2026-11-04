/*
 * @test
 * @summary javax.swing.JScrollBar conformance: type shape via reflection + component-layer
 *          black-box contracts (defaults, ctor matrices, value clamping to [min,max-extent],
 *          increment no-validation, AdjustmentEvent payloads, model swap/null rules).
 * @run main JScrollBarTest
 */

import java.awt.Adjustable;
import java.awt.EventQueue;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JScrollBarTest {
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

    static class Adj implements AdjustmentListener {
        int count; AdjustmentEvent last;
        public void adjustmentValueChanged(AdjustmentEvent e) { count++; last = e; }
        void reset() { count = 0; last = null; }
    }

    public static void main(String[] args) throws Exception {
        final Class<?> c = Class.forName("javax.swing.JScrollBar", false, JScrollBarTest.class.getClassLoader());
        EventQueue.invokeAndWait(new Runnable() { public void run() { testShape(c); }});
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        EventQueue.invokeAndWait(new Runnable() { public void run() {
            testDefaults();
            testCtors();
            testClamping();
            testIncrementsAndOrientation();
            testModelAndListener();
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
        check(hasInterface(c, "java.awt.Adjustable"), "implements java.awt.Adjustable");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setUI"), "method setUI");
        check(names.contains("getUI"), "method getUI");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
        check(names.contains("getOrientation"), "method getOrientation");
    }

    // ---- defaults ----
    static void testDefaults() {
        JScrollBar sb = new JScrollBar();
        checkEquals(Adjustable.VERTICAL, sb.getOrientation(), "default orientation VERTICAL");
        check(SwingConstants.VERTICAL == 1 && SwingConstants.HORIZONTAL == 0, "SwingConstants 0/1");
        checkEquals(0, sb.getValue(), "default value 0");
        checkEquals(10, sb.getVisibleAmount(), "default extent 10");
        checkEquals(0, sb.getMinimum(), "default min 0");
        checkEquals(100, sb.getMaximum(), "default max 100");
        checkEquals(1, sb.getUnitIncrement(), "default unitIncrement 1");
        checkEquals(10, sb.getBlockIncrement(), "default blockIncrement 10");
        check(!sb.getValueIsAdjusting(), "default valueIsAdjusting false");
        check(sb.getModel() != null && sb.getModel() instanceof DefaultBoundedRangeModel,
              "default model DefaultBoundedRangeModel");
        checkEquals(1, sb.getUnitIncrement(1), "getUnitIncrement(+1) same");
        checkEquals(1, sb.getUnitIncrement(-1), "getUnitIncrement(-1) same");
        checkEquals(10, sb.getBlockIncrement(1), "getBlockIncrement(+1) same");
        checkEquals(10, sb.getBlockIncrement(-1), "getBlockIncrement(-1) same");
        check(sb.getUI() != null && sb.getUI().getClass().getName().endsWith("MetalScrollBarUI"),
              "Metal binding installs MetalScrollBarUI");
        checkEquals("ScrollBarUI", sb.getUIClassID(), "getUIClassID ScrollBarUI");
        check(sb.getAccessibleContext() != null, "getAccessibleContext non-null");
    }

    // ---- ctor matrix ----
    static void testCtors() {
        JScrollBar h = new JScrollBar(Adjustable.HORIZONTAL);
        checkEquals(Adjustable.HORIZONTAL, h.getOrientation(), "HORIZONTAL ctor orientation");
        checkEquals(0, h.getValue(), "HORIZONTAL ctor value 0");
        JScrollBar full = new JScrollBar(Adjustable.VERTICAL, 5, 20, 0, 200);
        checkEquals(5, full.getValue(), "5-arg ctor value");
        checkEquals(20, full.getVisibleAmount(), "5-arg ctor extent");
        checkEquals(0, full.getMinimum(), "5-arg ctor min");
        checkEquals(200, full.getMaximum(), "5-arg ctor max");
        checkEquals(20, full.getBlockIncrement(), "5-arg ctor blockIncrement==extent");
        checkEquals(1, full.getUnitIncrement(), "5-arg ctor unitIncrement still 1");
        checkThrows(IllegalArgumentException.class, "ctor JScrollBar(99)",
            new Runnable() { public void run() { new JScrollBar(99); }});
        checkThrows(IllegalArgumentException.class, "ctor JScrollBar(-1)",
            new Runnable() { public void run() { new JScrollBar(-1); }});
    }

    // ---- value clamping: value in [min, max-extent] ----
    static void testClamping() {
        JScrollBar sb = new JScrollBar();
        Adj adj = new Adj();
        sb.addAdjustmentListener(adj);

        sb.setValue(50);
        checkEquals(50, sb.getValue(), "in-range setValue(50)");
        check(adj.count == 1, "setValue fires one AdjustmentEvent");
        check(adj.last.getValue() == 50, "event value 50");
        check(adj.last.getAdjustmentType() == AdjustmentEvent.TRACK, "event type TRACK");
        check(adj.last.getSource() == sb, "event source is the scrollbar");
        check(!adj.last.getValueIsAdjusting(), "event adjusting flag false");
        checkEquals(AdjustmentEvent.TRACK, 5, "TRACK constant 5");

        sb.setValue(1000);
        checkEquals(90, sb.getValue(), "setValue(1000) clamps to max-extent (90)");
        sb.setValue(-5);
        checkEquals(0, sb.getValue(), "setValue(-5) clamps to min (0)");

        sb.setValues(30, 20, 0, 100);
        checkEquals(30, sb.getValue(), "setValues value");
        checkEquals(20, sb.getVisibleAmount(), "setValues extent");
        checkEquals(0, sb.getMinimum(), "setValues min");
        checkEquals(100, sb.getMaximum(), "setValues max");
        // setValues is a raw bulk setter: unlike setValue it does NOT clamp the
        // requested value into [min, max-extent] — the model may hold an
        // inconsistent state (value+extent > max) until the next mutation
        sb.setValues(95, 20, 0, 100);
        checkEquals(95, sb.getValue(), "setValues stores requested value unclamped");
        checkEquals(100, sb.getMaximum(), "setValues keeps requested max");

        // extent is clamped into [0, max - value] (here 100-95 = 5)
        sb.setVisibleAmount(50);
        checkEquals(5, sb.getVisibleAmount(), "setVisibleAmount clamps extent to max-value");

        // minimum/maximum mutations also notify
        JScrollBar sb2 = new JScrollBar();
        Adj a2 = new Adj();
        sb2.addAdjustmentListener(a2);
        sb2.setMinimum(5);
        checkEquals(5, sb2.getMinimum(), "setMinimum(5)");
        check(a2.count == 1, "setMinimum fires one AdjustmentEvent");
        sb2.setVisibleAmount(20);
        checkEquals(20, sb2.getVisibleAmount(), "setVisibleAmount(20) stored when in range");
        check(a2.count == 2, "setVisibleAmount fires one AdjustmentEvent");
        sb2.setMaximum(300);
        checkEquals(300, sb2.getMaximum(), "setMaximum(300)");
        check(a2.count == 3, "setMaximum fires one AdjustmentEvent");
        check(sb2.getValue() <= sb2.getMaximum() - sb2.getVisibleAmount(), "invariant after model edits");

        sb.setValueIsAdjusting(true);
        check(sb.getValueIsAdjusting(), "setValueIsAdjusting(true)");
        sb.setValueIsAdjusting(false);
        check(!sb.getValueIsAdjusting(), "setValueIsAdjusting(false)");
    }

    // ---- increments are NOT validated; orientation IS ----
    static void testIncrementsAndOrientation() {
        JScrollBar sb = new JScrollBar();
        sb.setUnitIncrement(3);
        checkEquals(3, sb.getUnitIncrement(), "setUnitIncrement(3)");
        sb.setUnitIncrement(0);
        checkEquals(0, sb.getUnitIncrement(), "setUnitIncrement(0) accepted, no validation");
        sb.setBlockIncrement(-1);
        checkEquals(-1, sb.getBlockIncrement(), "setBlockIncrement(-1) accepted, no validation");

        sb.setOrientation(Adjustable.HORIZONTAL);
        checkEquals(Adjustable.HORIZONTAL, sb.getOrientation(), "setOrientation(HORIZONTAL)");
        checkEquals(0, sb.getUnitIncrement(), "orientation change keeps unitIncrement");
        checkEquals(-1, sb.getBlockIncrement(), "orientation change keeps blockIncrement");
        checkThrows(IllegalArgumentException.class, "setOrientation(99)",
            new Runnable() { public void run() { sb.setOrientation(99); }});
    }

    // ---- model swap + null rules + live propagation ----
    static void testModelAndListener() {
        JScrollBar sb = new JScrollBar();
        Adj adj = new Adj();
        sb.addAdjustmentListener(adj);

        DefaultBoundedRangeModel m = new DefaultBoundedRangeModel(5, 10, 0, 50);
        adj.reset();
        sb.setModel(m);
        check(sb.getModel() == m, "setModel identity");
        checkEquals(5, sb.getValue(), "value visible from swapped model");
        check(adj.count == 0, "model swap itself fires no AdjustmentEvent");

        // live model mutation propagates through the scrollbar to its listeners
        adj.reset();
        m.setValue(9);
        checkEquals(9, sb.getValue(), "model mutation visible in scrollbar");
        check(adj.count == 1, "model mutation fires AdjustmentEvent via scrollbar");
        check(adj.last.getValue() == 9 && adj.last.getSource() == sb,
              "propagated event value/source");

        // unitIncrement lives on the scrollbar, not the model
        sb.setUnitIncrement(7);
        checkEquals(7, sb.getUnitIncrement(), "increment stays scrollbar-local");

        checkThrows(NullPointerException.class, "setModel(null)",
            new Runnable() { public void run() { new JScrollBar().setModel(null); }});
        check(adj.count == 1, "failed null setModel fires nothing");
    }
}
