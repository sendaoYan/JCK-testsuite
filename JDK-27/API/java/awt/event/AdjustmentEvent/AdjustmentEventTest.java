/*
 * @test
 * @summary java.awt.event.AdjustmentEvent conformance: white-box structure + black-box behavior.
 * @run main AdjustmentEventTest
 */

import java.awt.Adjustable;
import java.awt.AWTEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.lang.reflect.Modifier;

public class AdjustmentEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class DummyAdjustable implements Adjustable {
        int value;
        DummyAdjustable(int v) { value = v; }
        public int getOrientation() { return 0; }
        public void setOrientation(int o) { }
        public int getMinimum() { return 0; }
        public void setMinimum(int m) { }
        public int getMaximum() { return 100; }
        public void setMaximum(int m) { }
        public int getValue() { return value; }
        public void setValue(int v) { value = v; }
        public void setVisibleAmount(int v) { }
        public int getVisibleAmount() { return 0; }
        public void setUnitIncrement(int u) { }
        public int getUnitIncrement() { return 1; }
        public void setBlockIncrement(int b) { }
        public int getBlockIncrement() { return 1; }
        public void addAdjustmentListener(AdjustmentListener l) { }
        public void removeAdjustmentListener(AdjustmentListener l) { }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.AdjustmentEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "AdjustmentEvent is public");
        check(!Modifier.isAbstract(m), "AdjustmentEvent is concrete");
        check(c.getSuperclass() == AWTEvent.class, "AdjustmentEvent extends AWTEvent");
        check(c.getField("ADJUSTMENT_VALUE_CHANGED").getType() == int.class, "ADJUSTMENT_VALUE_CHANGED int");
        check(c.getField("UNIT_INCREMENT").getType() == int.class, "UNIT_INCREMENT int");
        check(c.getConstructor(Adjustable.class, int.class, int.class, int.class) != null, "has 4-arg ctor");
        check(c.getConstructor(Adjustable.class, int.class, int.class, int.class, boolean.class) != null, "has 5-arg ctor");
    }

    static void testBlackBox() throws Exception {
        DummyAdjustable adj = new DummyAdjustable(42);
        AdjustmentEvent e = new AdjustmentEvent(adj, AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED,
                AdjustmentEvent.UNIT_INCREMENT, 42);

        check(e.getID() == AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED, "getID round-trips");
        check(e.getSource() == adj, "getSource round-trips");
        check(e.getAdjustable() == adj, "getAdjustable returns source");
        check(e.getValue() == 42, "getValue round-trips");
        check(e.getAdjustmentType() == AdjustmentEvent.UNIT_INCREMENT, "getAdjustmentType round-trips");
        check(!e.getValueIsAdjusting(), "default isAdjusting false");

        AdjustmentEvent e2 = new AdjustmentEvent(adj, AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED,
                AdjustmentEvent.TRACK, 77, true);
        check(e2.getValue() == 77, "5-arg ctor value");
        check(e2.getAdjustmentType() == AdjustmentEvent.TRACK, "5-arg ctor type");
        check(e2.getValueIsAdjusting(), "5-arg ctor isAdjusting true");

        // documented constants
        check(AdjustmentEvent.ADJUSTMENT_FIRST == 601, "ADJUSTMENT_FIRST == 601");
        check(AdjustmentEvent.ADJUSTMENT_LAST == 601, "ADJUSTMENT_LAST == 601");
        check(AdjustmentEvent.ADJUSTMENT_VALUE_CHANGED == 601, "ADJUSTMENT_VALUE_CHANGED == 601");
        check(AdjustmentEvent.UNIT_INCREMENT == 1, "UNIT_INCREMENT == 1");
        check(AdjustmentEvent.UNIT_DECREMENT == 2, "UNIT_DECREMENT == 2");
        check(AdjustmentEvent.BLOCK_DECREMENT == 3, "BLOCK_DECREMENT == 3");
        check(AdjustmentEvent.BLOCK_INCREMENT == 4, "BLOCK_INCREMENT == 4");
        check(AdjustmentEvent.TRACK == 5, "TRACK == 5");

        // paramString
        String ps = e.paramString();
        check(ps.contains("ADJUSTMENT_VALUE_CHANGED"), "paramString has id text");
        check(ps.contains("adjType=UNIT_INCREMENT"), "paramString has adjType");
        check(ps.contains("value=42"), "paramString has value");

        String ps2 = e2.paramString();
        check(ps2.contains("value=77"), "e2 paramString value");
        check(ps2.contains("adjType=TRACK"), "e2 paramString type");
        check(ps2.contains("isAdjusting=true"), "e2 paramString isAdjusting");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
