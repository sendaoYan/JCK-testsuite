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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
