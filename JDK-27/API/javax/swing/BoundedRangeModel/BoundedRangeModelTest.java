/*
 * @test
 * @summary javax.swing.BoundedRangeModel conformance: white-box structure + black-box behavior.
 * @run main BoundedRangeModelTest
 */

import java.lang.reflect.Modifier;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BoundedRangeModelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.BoundedRangeModel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        // supplemented structural assertions
        String[] mnames = {"getMinimum", "setMinimum", "getMaximum", "setMaximum",
                           "getValue", "setValue", "setValueIsAdjusting", "getValueIsAdjusting",
                           "getExtent", "setExtent", "setRangeProperties",
                           "addChangeListener", "removeChangeListener"};
        for (String m : mnames) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getMethods())
                if (mm.getName().equals(m)) { found = true; break; }
            check(found, "interface declares " + m);
        }
        Class<?> def = Class.forName("javax.swing.DefaultBoundedRangeModel");
        check(def.getInterfaces() != null && java.util.Arrays.asList(def.getInterfaces()).toString().contains("BoundedRangeModel"),
              "DefaultBoundedRangeModel implements BoundedRangeModel");
        check(!Modifier.isAbstract(def.getModifiers()), "DefaultBoundedRangeModel concrete");
    }

    // ---- recording listener ----
    static class Rec implements ChangeListener {
        int count; ChangeEvent last;
        public void stateChanged(ChangeEvent e) { count++; last = e; }
        void reset() { count = 0; last = null; }
    }

    static void expectIAE(String why, Runnable r) {
        try { r.run(); throw new AssertionError("expected IAE: " + why); }
        catch (IllegalArgumentException expected) { passed++; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // defaults of the reference implementation
        javax.swing.BoundedRangeModel m = new DefaultBoundedRangeModel();
        check(m.getValue() == 0, "default value 0");
        check(m.getExtent() == 0, "default extent 0");
        check(m.getMinimum() == 0, "default min 0");
        check(m.getMaximum() == 100, "default max 100");
        check(!m.getValueIsAdjusting(), "default isAdjusting false");

        // 4-arg constructor round trip + invariant violations
        javax.swing.BoundedRangeModel m2 = new DefaultBoundedRangeModel(20, 10, 0, 100);
        check(m2.getValue() == 20 && m2.getExtent() == 10 && m2.getMinimum() == 0 && m2.getMaximum() == 100,
              "ctor round trip");
        expectIAE("value<min", () -> new DefaultBoundedRangeModel(-1, 0, 0, 100));
        expectIAE("max<min", () -> new DefaultBoundedRangeModel(0, 0, 5, 4));
        expectIAE("value+extent>max", () -> new DefaultBoundedRangeModel(90, 20, 0, 100));

        // setValue clamping (min <= value <= max-extent)
        m2.setValue(150);
        check(m2.getValue() == 90, "setValue clamps up to max-extent");
        m2.setValue(-7);
        check(m2.getValue() == 0, "setValue clamps down to min");
        m2.setValue(95);
        check(m2.getValue() == 90, "setValue clamps to max-extent (100-10)");

        // setExtent clamping (>=0, value+extent<=max)
        m2.setExtent(-5);
        check(m2.getExtent() == 0, "setExtent clamps to 0");
        m2.setExtent(500);
        check(m2.getExtent() == 10, "setExtent clamps to max-value (100-90)");
        m2.setExtent(4);
        check(m2.getExtent() == 4, "setExtent normal round trip");

        // setMinimum raises value (and max) as needed
        javax.swing.BoundedRangeModel m3 = new DefaultBoundedRangeModel(10, 5, 0, 100);
        m3.setMinimum(30);
        check(m3.getMinimum() == 30 && m3.getValue() == 30, "setMinimum raises value to min");
        check(m3.getMaximum() == 100, "setMinimum keeps max");
        m3.setMinimum(-20);
        check(m3.getMinimum() == -20 && m3.getValue() == 30, "setMinimum below does not lower value");

        // setMaximum lowers value/extent as needed
        javax.swing.BoundedRangeModel m4 = new DefaultBoundedRangeModel(50, 10, 0, 100);
        m4.setMaximum(20);
        check(m4.getMaximum() == 20 && m4.getMinimum() == 0, "setMaximum keeps min<=max");
        check(m4.getValue() == 10 && m4.getExtent() == 10, "setMaximum lowers value to max-extent");

        // setRangeProperties: one-shot update, constraints forced
        javax.swing.BoundedRangeModel m5 = new DefaultBoundedRangeModel();
        m5.setRangeProperties(40, 5, 0, 100, true);
        check(m5.getValue() == 40 && m5.getExtent() == 5 && m5.getMinimum() == 0
              && m5.getMaximum() == 100 && m5.getValueIsAdjusting(), "setRangeProperties all five getters");
        // value > max: max grows to value (documented "usual constraints" forcing)
        m5.setRangeProperties(300, 0, 0, 100, false);
        check(m5.getMaximum() == 300 && m5.getValue() == 300, "setRangeProperties value>max grows max");
        // value < min: min shrinks to value
        m5.setRangeProperties(-40, 0, 0, 300, false);
        check(m5.getMinimum() == -40 && m5.getValue() == -40, "setRangeProperties value<min lowers min");
        // min > max with a low value: min first forced to max, then shrunk to value
        m5.setRangeProperties(10, 0, 500, 400, false);
        check(m5.getMinimum() == 10 && m5.getMaximum() == 400 && m5.getValue() == 10,
              "setRangeProperties min>max then value<min shrinks min to value");
        // extent clamped to max-value, never negative
        m5.setRangeProperties(90, 500, 0, 100, false);
        check(m5.getExtent() == 10, "setRangeProperties clamps extent to max-value");

        // setValueIsAdjusting round trip
        m5.setValueIsAdjusting(true);
        check(m5.getValueIsAdjusting(), "setValueIsAdjusting(true)");
        m5.setValueIsAdjusting(false);
        check(!m5.getValueIsAdjusting(), "setValueIsAdjusting(false)");

        // ---- ChangeListener recording over a multi-step chain ----
        DefaultBoundedRangeModel m6 = new DefaultBoundedRangeModel(0, 0, 0, 100);
        Rec rec = new Rec();
        m6.addChangeListener(rec);
        check(rec.count == 0, "no event before change");
        m6.setValue(50);
        check(rec.count == 1 && rec.last.getSource() == m6, "setValue fires one ChangeEvent with model source");
        m6.setValue(50);
        check(rec.count == 1, "same value fires nothing");
        m6.setRangeProperties(10, 2, 0, 100, true);
        check(rec.count == 2, "setRangeProperties fires exactly one event for a multi-field change");
        m6.setRangeProperties(10, 2, 0, 100, true);
        check(rec.count == 2, "identical setRangeProperties fires nothing");
        m6.setValueIsAdjusting(false);
        check(rec.count == 3, "adjusting flag change alone fires event");
        m6.setValue(100);
        check(rec.count == 4, "clamped setValue(200)->100 still fires");
        m6.removeChangeListener(rec);
        m6.setValue(0);
        check(rec.count == 4, "removed listener gets nothing");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
