/*
 * @test
 * @summary javax.swing.JScrollBar conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main JScrollBarTest
 */

import java.awt.Adjustable;
import java.awt.EventQueue;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.lang.reflect.Modifier;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JScrollBar;

public class JScrollBarTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JScrollBar");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.awt.Adjustable"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Default constructor: vertical scrollbar with standard range.
        JScrollBar sb = new JScrollBar();
        check(sb.getOrientation() == Adjustable.VERTICAL, "default orientation vertical");
        check(sb.getValue() == 0, "default value 0");
        check(sb.getMinimum() == 0, "default minimum 0");
        check(sb.getMaximum() == 100, "default maximum 100");
        check(sb.getVisibleAmount() == 10, "default visibleAmount 10");
        check(sb.getUnitIncrement() == 1, "default unitIncrement 1");
        check(sb.getBlockIncrement() == 10, "default blockIncrement 10");
        check(sb.getModel() instanceof DefaultBoundedRangeModel, "default model DefaultBoundedRangeModel");

        // Orientation constructor.
        JScrollBar h = new JScrollBar(Adjustable.HORIZONTAL);
        check(h.getOrientation() == Adjustable.HORIZONTAL, "JScrollBar(HORIZONTAL) orientation");

        // Five-argument constructor.
        JScrollBar sb2 = new JScrollBar(Adjustable.VERTICAL, 20, 5, 0, 50);
        check(sb2.getValue() == 20, "5-arg ctor value");
        check(sb2.getVisibleAmount() == 5, "5-arg ctor extent");
        check(sb2.getMinimum() == 0, "5-arg ctor min");
        check(sb2.getMaximum() == 50, "5-arg ctor max");
        check(sb2.getUnitIncrement() == 1, "5-arg ctor unitIncrement 1");
        check(sb2.getBlockIncrement() == 5, "5-arg ctor blockIncrement equals extent");

        // Orientation roundtrip + invalid value.
        sb.setOrientation(Adjustable.HORIZONTAL);
        check(sb.getOrientation() == Adjustable.HORIZONTAL, "setOrientation(HORIZONTAL)");
        sb.setOrientation(Adjustable.VERTICAL);
        check(sb.getOrientation() == Adjustable.VERTICAL, "setOrientation(VERTICAL)");
        try {
            sb.setOrientation(999);
            check(false, "setOrientation(999) should throw IAE");
        } catch (IllegalArgumentException expected) {
            check(true, "setOrientation(999) IAE");
        }

        // Bounded-range property roundtrips.
        sb.setValue(30);
        check(sb.getValue() == 30, "setValue/getValue");
        sb.setMinimum(5);
        check(sb.getMinimum() == 5, "setMinimum/getMinimum");
        sb.setMaximum(200);
        check(sb.getMaximum() == 200, "setMaximum/getMaximum");
        sb.setVisibleAmount(15);
        check(sb.getVisibleAmount() == 15, "setVisibleAmount/getVisibleAmount");

        // Increment roundtrips.
        sb.setUnitIncrement(2);
        check(sb.getUnitIncrement() == 2, "setUnitIncrement/getUnitIncrement");
        sb.setBlockIncrement(20);
        check(sb.getBlockIncrement() == 20, "setBlockIncrement/getBlockIncrement");
        check(sb.getUnitIncrement(1) == 2, "getUnitIncrement(direction)");
        check(sb.getBlockIncrement(-1) == 20, "getBlockIncrement(direction)");

        // setValues changes all four at once.
        sb.setValues(40, 10, 0, 100);
        check(sb.getValue() == 40, "setValues value");
        check(sb.getVisibleAmount() == 10, "setValues extent");
        check(sb.getMinimum() == 0, "setValues min");
        check(sb.getMaximum() == 100, "setValues max");

        // Value-is-adjusting flag.
        sb.setValueIsAdjusting(true);
        check(sb.getValueIsAdjusting(), "setValueIsAdjusting(true)");
        sb.setValueIsAdjusting(false);
        check(!sb.getValueIsAdjusting(), "setValueIsAdjusting(false)");

        // Model replacement.
        sb.setModel(new DefaultBoundedRangeModel(7, 3, 0, 10));
        check(sb.getModel().getValue() == 7, "setModel value");

        // Adjustment event fired through model changes (fresh scrollbar).
        JScrollBar sb3 = new JScrollBar();
        final int[] events = new int[1];
        final int[] value = new int[1];
        final int[] type = new int[1];
        final Object[] src = new Object[1];
        sb3.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                events[0]++;
                value[0] = e.getValue();
                type[0] = e.getAdjustmentType();
                src[0] = e.getSource();
            }
        });
        sb3.setValue(55);
        check(events[0] == 1, "adjustment listener fired once");
        check(value[0] == 55, "adjustment value");
        check(type[0] == AdjustmentEvent.TRACK, "adjustment type TRACK");
        check(src[0] == sb3, "adjustment source is scrollbar");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(JScrollBarTest::testWhiteBox);
        runOnEDT(JScrollBarTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
