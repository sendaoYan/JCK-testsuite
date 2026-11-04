/*
 * @test
 * @key headful
 * @summary java.awt.Scrollbar conformance: class reflection.
 * @run main ScrollbarTest
 */

import java.awt.*;

public class ScrollbarTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
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
        Class<?> c = Class.forName("java.awt.Scrollbar");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component"), "superclass java.awt.Component");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.Adjustable", "javax.accessibility.Accessible"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addNotify"), "method addNotify");
        check(hasMethod(c, "getOrientation"), "method getOrientation");
        check(hasMethod(c, "setOrientation"), "method setOrientation");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "setValue"), "method setValue");
        check(hasMethod(c, "getMinimum"), "method getMinimum");
        check(hasMethod(c, "setMinimum"), "method setMinimum");
        check(hasMethod(c, "getMaximum"), "method getMaximum");
        check(hasMethod(c, "setMaximum"), "method setMaximum");
        check(hasMethod(c, "getVisibleAmount"), "method getVisibleAmount");
        check(hasMethod(c, "getVisible"), "method getVisible");
        check(hasMethod(c, "setVisibleAmount"), "method setVisibleAmount");
        check(hasMethod(c, "setUnitIncrement"), "method setUnitIncrement");
        check(hasMethod(c, "setLineIncrement"), "method setLineIncrement");
        check(hasMethod(c, "getUnitIncrement"), "method getUnitIncrement");
        check(hasMethod(c, "getLineIncrement"), "method getLineIncrement");
        check(hasMethod(c, "setBlockIncrement"), "method setBlockIncrement");
        check(hasMethod(c, "setPageIncrement"), "method setPageIncrement");
        check(hasMethod(c, "getBlockIncrement"), "method getBlockIncrement");
        check(hasMethod(c, "getPageIncrement"), "method getPageIncrement");
        check(hasMethod(c, "setValues"), "method setValues");
        check(hasMethod(c, "getValueIsAdjusting"), "method getValueIsAdjusting");
        check(hasMethod(c, "setValueIsAdjusting"), "method setValueIsAdjusting");
        check(hasMethod(c, "addAdjustmentListener"), "method addAdjustmentListener");
        check(hasMethod(c, "removeAdjustmentListener"), "method removeAdjustmentListener");
        check(hasMethod(c, "getAdjustmentListeners"), "method getAdjustmentListeners");
        check(hasMethod(c, "getListeners"), "method getListeners");
        check(hasMethod(c, "getAccessibleContext"), "method getAccessibleContext");
        testContractScrollbar();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
