/*
 * @test
 * @summary java.awt.Scrollbar conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ScrollbarTest
 */

import java.awt.*;
import java.awt.event.AdjustmentListener;
import java.lang.reflect.*;

public class ScrollbarTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Scrollbar");
        check(c != null, "type loads: java.awt.Scrollbar");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(!Modifier.isAbstract(m), "not abstract");
        check(c.getSuperclass() == Component.class, "extends Component");
        boolean adjustable = false;
        for (Class<?> i : c.getInterfaces()) if (i == Adjustable.class) adjustable = true;
        check(adjustable, "implements Adjustable");
        for (String name : new String[]{"getOrientation", "setOrientation", "getValue", "setValue",
                "getMinimum", "setMinimum", "getMaximum", "setMaximum", "getVisibleAmount",
                "setVisibleAmount", "getUnitIncrement", "setUnitIncrement", "getBlockIncrement",
                "setBlockIncrement", "setValues", "getValueIsAdjusting", "setValueIsAdjusting",
                "addAdjustmentListener", "removeAdjustmentListener", "getAdjustmentListeners"}) {
            check(hasMethod(c, name), "has " + name);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(Scrollbar.HORIZONTAL == 0, "HORIZONTAL 0");
        check(Scrollbar.VERTICAL == 1, "VERTICAL 1");

        Scrollbar s = new Scrollbar();
        check(s.getOrientation() == Scrollbar.VERTICAL, "default orientation VERTICAL");
        check(s.getValue() == 0, "default value 0");
        check(s.getMinimum() == 0, "default minimum 0");
        check(s.getMaximum() == 100, "default maximum 100");
        check(s.getVisibleAmount() == 10, "default visible amount 10");
        check(s.getVisible() == 10, "getVisible alias");

        Scrollbar h = new Scrollbar(Scrollbar.HORIZONTAL);
        check(h.getOrientation() == Scrollbar.HORIZONTAL, "constructor orientation HORIZONTAL");

        Scrollbar s2 = new Scrollbar(Scrollbar.HORIZONTAL, 20, 5, 0, 100);
        check(s2.getOrientation() == Scrollbar.HORIZONTAL, "five-arg orientation");
        check(s2.getValue() == 20 && s2.getVisibleAmount() == 5, "five-arg value/visible");
        check(s2.getMinimum() == 0 && s2.getMaximum() == 100, "five-arg min/max");

        s2.setOrientation(Scrollbar.VERTICAL);
        check(s2.getOrientation() == Scrollbar.VERTICAL, "setOrientation");
        s2.setValue(30);
        check(s2.getValue() == 30, "setValue");
        s2.setMinimum(5);
        check(s2.getMinimum() == 5, "setMinimum");
        s2.setMaximum(200);
        check(s2.getMaximum() == 200, "setMaximum");
        s2.setVisibleAmount(15);
        check(s2.getVisibleAmount() == 15, "setVisibleAmount");
        s2.setUnitIncrement(3);
        check(s2.getUnitIncrement() == 3, "setUnitIncrement");
        s2.setLineIncrement(4);
        check(s2.getLineIncrement() == 4, "setLineIncrement");
        s2.setBlockIncrement(20);
        check(s2.getBlockIncrement() == 20, "setBlockIncrement");
        s2.setPageIncrement(25);
        check(s2.getPageIncrement() == 25, "setPageIncrement");

        s2.setValues(10, 5, 0, 50);
        check(s2.getValue() == 10 && s2.getVisibleAmount() == 5
                && s2.getMinimum() == 0 && s2.getMaximum() == 50, "setValues");

        check(!s2.getValueIsAdjusting(), "value not adjusting default");
        s2.setValueIsAdjusting(true);
        check(s2.getValueIsAdjusting(), "setValueIsAdjusting(true)");

        AdjustmentListener l = e -> {};
        s2.addAdjustmentListener(l);
        check(s2.getAdjustmentListeners().length == 1 && s2.getAdjustmentListeners()[0] == l,
              "addAdjustmentListener/getAdjustmentListeners");
        s2.removeAdjustmentListener(l);
        check(s2.getAdjustmentListeners().length == 0, "removeAdjustmentListener");

        Frame f = new Frame("ScrollbarTest");
        f.setSize(150, 150);
        f.setVisible(true);
        try {
            f.add(s2);
            check(s2.isDisplayable(), "displayable after add");
        } finally {
            f.dispose();
        }
    }

    // ---- shared helper for exact-exception assertions (deepening batch) ----
    interface ThrowingAction { void run() throws Exception; }

    static void expectExact(Class<? extends Throwable> expected, String tag, ThrowingAction action) {
        try {
            action.run();
            check(false, tag + ": expected " + expected.getSimpleName() + " but nothing was thrown");
        } catch (Throwable thrown) {
            check(thrown.getClass() == expected,
                  tag + ": expected " + expected.getName() + " but got " + thrown.getClass().getName());
        }
    }

    // ---- Part 3: contract deepening ----
    static void testContractScrollbar() throws Exception {
        // orientation validation across all entry points
        expectExact(IllegalArgumentException.class, "ctor(-1)", () -> new Scrollbar(-1));
        expectExact(IllegalArgumentException.class, "ctor(2)", () -> new Scrollbar(2));
        expectExact(IllegalArgumentException.class, "5-arg ctor(2)",
              () -> new Scrollbar(2, 0, 10, 0, 100));
        Scrollbar s = new Scrollbar();
        expectExact(IllegalArgumentException.class, "setOrientation(5)", () -> s.setOrientation(5));
        s.setOrientation(Scrollbar.HORIZONTAL);
        check(s.getOrientation() == Scrollbar.HORIZONTAL, "setOrientation roundtrip");

        // 5-arg ctor normalization
        Scrollbar c1 = new Scrollbar(Scrollbar.VERTICAL, -5, 10, 0, 100);
        check(c1.getValue() == 0, "ctor value below min clamps to min");
        Scrollbar c2 = new Scrollbar(Scrollbar.VERTICAL, 500, 10, 0, 100);
        check(c2.getValue() == 90, "ctor value above max-visible clamps");
        Scrollbar c3 = new Scrollbar(Scrollbar.VERTICAL, 50, 10, 100, 40);
        check(c3.getMinimum() == 100 && c3.getMaximum() == 101, "ctor min above max lifts max to min+1");

        // increments: invalid values clamp to 1, never throw
        Scrollbar i = new Scrollbar();
        i.setUnitIncrement(0);
        check(i.getUnitIncrement() == 1, "setUnitIncrement(0) clamps to 1");
        i.setUnitIncrement(-5);
        check(i.getUnitIncrement() == 1, "setUnitIncrement(-5) clamps to 1");
        i.setUnitIncrement(3);
        check(i.getUnitIncrement() == 3, "valid unit increment");
        i.setBlockIncrement(0);
        check(i.getBlockIncrement() == 1, "setBlockIncrement(0) clamps to 1");
        i.setBlockIncrement(-5);
        check(i.getBlockIncrement() == 1, "setBlockIncrement(-5) clamps to 1");
        i.setBlockIncrement(7);
        check(i.getBlockIncrement() == 7, "valid block increment");
        Scrollbar dflt = new Scrollbar();
        check(dflt.getUnitIncrement() == 1, "default unit increment 1");
        check(dflt.getBlockIncrement() == dflt.getVisibleAmount(), "default block increment equals visible");

        // setValues clamping matrix
        Scrollbar v = new Scrollbar(Scrollbar.HORIZONTAL, 50, 10, 0, 100);
        v.setValues(-10, 10, 0, 100);
        check(v.getValue() == 0, "setValues: value below min clamps to min");
        v.setValues(200, 10, 0, 100);
        check(v.getValue() == 90, "setValues: value above max-visible clamps");
        v.setValues(40, 200, 0, 100);
        check(v.getVisibleAmount() == 100 && v.getValue() == 0,
              "setValues: oversize visible clamps to max-min");
        v.setValues(5, 10, 20, 100);
        check(v.getMinimum() == 20 && v.getValue() == 20, "setValues: min lift moves value");
        // setValue clamps
        v.setValue(100000);
        check(v.getValue() == 90, "setValue clamps to max-visible");
        v.setValue(-9);
        check(v.getValue() == 20, "setValue clamps to min");
        // min/max coupling
        v.setMinimum(150);
        check(v.getMinimum() == 150 && v.getMaximum() == 151 && v.getValue() == 150,
              "setMinimum above max lifts max to min+1");
        Scrollbar w = new Scrollbar();
        w.setMaximum(-50);
        check(w.getMaximum() == -50 && w.getMinimum() == -51 && w.getValue() == -51,
              "setMaximum below min pulls min down");

        // adjusting flag
        check(!v.getValueIsAdjusting(), "value not adjusting by default");
        v.setValueIsAdjusting(true);
        check(v.getValueIsAdjusting(), "setValueIsAdjusting(true)");
        v.setValueIsAdjusting(false);
        check(!v.getValueIsAdjusting(), "setValueIsAdjusting(false)");

        // listener registry with duplicates; programmatic changes fire no AdjustmentEvent
        java.awt.event.AdjustmentListener al = e -> { };
        v.addAdjustmentListener(al); v.addAdjustmentListener(al);
        check(v.getAdjustmentListeners().length == 2, "duplicate AdjustmentListeners registered");
        v.removeAdjustmentListener(al);
        check(v.getAdjustmentListeners().length == 1, "removeAdjustmentListener removes one instance");
        boolean[] fired = { false };
        v.addAdjustmentListener(e -> fired[0] = true);
        v.setValue(30);
        java.awt.EventQueue.invokeAndWait(() -> { });
        check(!fired[0], "no AdjustmentEvent from programmatic setValue");
        String ts = v.toString();
        check(ts.contains("val=") && ts.contains("vis=") && ts.contains("min=") && ts.contains("max="),
              "toString carries model state");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractScrollbar();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
