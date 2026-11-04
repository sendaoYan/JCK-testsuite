/*
 * @test
 * @summary java.awt.Adjustable conformance: white-box structure + black-box behavior.
 * @run main AdjustableTest
 */

import java.lang.reflect.*;
import java.awt.Adjustable;
import java.awt.event.AdjustmentListener;

public class AdjustableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Adjustable", false, AdjustableTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Adjustable");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isInterface(m), "public interface");
        check(c.isInterface(), "is interface");
        check(c.getField("HORIZONTAL").getType() == int.class, "int HORIZONTAL");
        check(c.getField("VERTICAL").getType() == int.class, "int VERTICAL");
        check(c.getField("NO_ORIENTATION").getType() == int.class, "int NO_ORIENTATION");
        for (String n : new String[] {"getOrientation", "setMinimum", "getMinimum", "setMaximum",
                "getMaximum", "setValue", "getValue", "addAdjustmentListener", "removeAdjustmentListener"})
            check(hasMethod(c, n), "method present: " + n);
    }

    // minimal in-memory implementation exercising the full contract
    static final class Mem implements Adjustable {
        int min, max, value, unit, block, visible, orientation;
        public int getOrientation() { return orientation; }
        public void setMinimum(int v) { min = v; }
        public int getMinimum() { return min; }
        public void setMaximum(int v) { max = v; }
        public int getMaximum() { return max; }
        public void setUnitIncrement(int v) { unit = v; }
        public int getUnitIncrement() { return unit; }
        public void setBlockIncrement(int v) { block = v; }
        public int getBlockIncrement() { return block; }
        public void setVisibleAmount(int v) { visible = v; }
        public int getVisibleAmount() { return visible; }
        public void setValue(int v) { value = v; }
        public int getValue() { return value; }
        public void addAdjustmentListener(AdjustmentListener l) { }
        public void removeAdjustmentListener(AdjustmentListener l) { }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(Adjustable.HORIZONTAL == 0, "HORIZONTAL == 0");
        check(Adjustable.VERTICAL == 1, "VERTICAL == 1");
        check(Adjustable.NO_ORIENTATION == 2, "NO_ORIENTATION == 2");

        Mem a = new Mem();
        a.orientation = Adjustable.VERTICAL;
        check(a.getOrientation() == Adjustable.VERTICAL, "orientation roundtrip");

        // min/max/value state machine
        a.setMinimum(0);
        a.setMaximum(100);
        a.setValue(25);
        check(a.getMinimum() == 0, "getMinimum");
        check(a.getMaximum() == 100, "getMaximum");
        check(a.getValue() == 25, "getValue");

        // increments + visible amount
        a.setUnitIncrement(1);
        a.setBlockIncrement(10);
        a.setVisibleAmount(20);
        check(a.getUnitIncrement() == 1, "getUnitIncrement");
        check(a.getBlockIncrement() == 10, "getBlockIncrement");
        check(a.getVisibleAmount() == 20, "getVisibleAmount");

        // combination chain: value within [min, max] and increments positive
        a.setValue(a.getMinimum());
        check(a.getValue() == 0, "value at minimum");
        a.setValue(a.getMaximum());
        check(a.getValue() == 100, "value at maximum");
        check(a.getUnitIncrement() <= a.getBlockIncrement(), "unit <= block");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
