/*
 * @test
 * @summary javax.swing.JSlider conformance: white-box structure + black-box behavior
 *          + deep component-layer contracts (clamp cascades, snap math, ChangeEvent counting).
 * @key headful
 * @run main JSliderTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

public class JSliderTest {
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

    public static void main(String[] args) throws Exception {
        runOnEDT(JSliderTest::testWhiteBox);
        runOnEDT(JSliderTest::testBlackBox);
        runOnEDT(JSliderTest::testDeepContracts);
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deep component-layer contracts (clamp cascades, snap math, ChangeEvent counting) ----
    static void testDeepContracts() throws Exception {
        // 2-arg ctor value is the midpoint, not min
        check(new JSlider(10, 20).getValue() == 15, "2-arg ctor value is midpoint (10+20)/2");

        // min/max clamp cascades
        JSlider s = new JSlider(); // 0..100 value 50
        s.setValue(200);
        check(s.getValue() == 100, "setValue(200) clamps to max");
        s.setValue(-5);
        check(s.getValue() == 0, "setValue(-5) clamps to min");
        s.setValue(50);
        s.setMinimum(200);
        check(s.getMinimum() == 200, "setMinimum(200) drags min above value");
        check(s.getValue() == 200, "value follows min upward");
        s.setMaximum(150); // min==200 collapses everything to 150
        check(s.getMaximum() == 150, "setMaximum(150) below min clamps max");
        check(s.getMinimum() == 150, "min follows max down");
        check(s.getValue() == 150, "value follows collapse");

        // snap-to-ticks exact math with spacing 5
        JSlider t = new JSlider();
        t.setMinorTickSpacing(5);
        t.setSnapToTicks(true);
        t.setValue(3);
        check(t.getValue() == 5, "snap setValue(3) -> 5");
        t.setValue(4);
        check(t.getValue() == 5, "snap setValue(4) -> 5");
        t.setValue(2);
        check(t.getValue() == 0, "snap setValue(2) -> 0");
        t.setSnapToTicks(false);
        t.setValue(3);
        check(t.getValue() == 3, "snap off keeps exact 3");

        // ChangeEvent counting: one per real change, none for same value,
        // propagation from model mutations, quiet same-model re-set
        JSlider q = new JSlider();
        final int[] n = new int[1];
        final javax.swing.event.ChangeEvent[] last = new javax.swing.event.ChangeEvent[1];
        q.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) { n[0]++; last[0] = e; }
        });
        q.setValue(60);
        check(n[0] == 1 && last[0].getSource() == q, "setValue fires one ChangeEvent, source slider");
        q.setValue(60);
        check(n[0] == 1, "same-value setValue fires nothing");
        DefaultBoundedRangeModel m = new DefaultBoundedRangeModel(7, 0, 0, 9);
        q.setModel(m);
        check(q.getModel() == m && q.getValue() == 7, "model swap identity and value");
        check(n[0] == 1, "model swap itself fires no ChangeEvent");
        n[0] = 0;
        m.setValue(8);
        check(q.getValue() == 8, "model mutation visible");
        check(n[0] == 1, "model mutation fires ChangeEvent via slider");
        q.setModel(q.getModel());
        check(n[0] == 1, "same-model re-set fires nothing");
        try {
            new JSlider().setModel(null);
            check(false, "setModel(null) must throw");
        } catch (NullPointerException e) { check(true, "setModel(null) NPE"); }

        // labelTable null + createStandardLabels bounds
        s.setLabelTable(null);
        check(s.getLabelTable() == null, "setLabelTable(null) accepted");
        try {
            new JSlider(0, 100).createStandardLabels(0);
            check(false, "createStandardLabels(0) must throw");
        } catch (IllegalArgumentException e) { check(true, "createStandardLabels(0) IAE"); }

        try {
            new JSlider(99);
            check(false, "ctor JSlider(99) must throw");
        } catch (IllegalArgumentException e) { check(true, "ctor JSlider(99) IAE"); }
        try {
            s.setOrientation(99);
            check(false, "setOrientation(99) must throw");
        } catch (IllegalArgumentException e) { check(true, "setOrientation(99) IAE"); }
        check(s.getOrientation() == SwingConstants.HORIZONTAL, "orientation kept after rejection");

        check(s.getUI() != null && s.getUI().getClass().getName().endsWith("MetalSliderUI"),
                "Metal binding installs MetalSliderUI");
        check("SliderUI".equals(s.getUIClassID()), "getUIClassID SliderUI");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JSlider");
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
        check(c.getInterfaces()[0].getName().equals("javax.swing.SwingConstants"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("javax.accessibility.Accessible"), "interface 1");
    }

    static void testBlackBox() throws Exception {
        JSlider s = new JSlider();
        check(s.getValue() == 50, "default value 50");
        check(s.getMinimum() == 0, "default min 0");
        check(s.getMaximum() == 100, "default max 100");
        check(s.getOrientation() == SwingConstants.HORIZONTAL, "default orientation");
        check(s.getMajorTickSpacing() == 0, "default major spacing 0");
        check(s.getMinorTickSpacing() == 0, "default minor spacing 0");
        check(s.getSnapToTicks() == false, "default no snap");
        check(s.getPaintTicks() == false, "default no paint ticks");
        check(s.getPaintTrack() == true, "default paint track");
        check(s.getPaintLabels() == false, "default no paint labels");
        check(s.getInverted() == false, "default not inverted");
        check(s.getExtent() == 0, "default extent 0");
        check(s.getLabelTable() == null, "default label table null");

        JSlider v = new JSlider(SwingConstants.VERTICAL);
        check(v.getOrientation() == SwingConstants.VERTICAL, "vertical ctor");
        check(v.getValue() == 50, "vertical default value");

        JSlider mm = new JSlider(20, 60);
        check(mm.getMinimum() == 20, "min/max ctor min");
        check(mm.getMaximum() == 60, "min/max ctor max");
        check(mm.getValue() == 40, "min/max ctor midpoint");

        JSlider mmv = new JSlider(10, 90, 30);
        check(mmv.getValue() == 30, "min/max/value ctor value");

        JSlider full = new JSlider(SwingConstants.VERTICAL, 0, 200, 75);
        check(full.getOrientation() == SwingConstants.VERTICAL, "full ctor orientation");
        check(full.getValue() == 75, "full ctor value");

        JSlider modeled = new JSlider(new DefaultBoundedRangeModel(5, 0, 0, 10));
        check(modeled.getValue() == 5, "model ctor value");
        check(modeled.getMinimum() == 0 && modeled.getMaximum() == 10, "model ctor bounds");

        s.setValue(75);
        check(s.getValue() == 75, "setValue");
        s.setValue(-3);
        check(s.getValue() == 0, "setValue clamp min");
        s.setValue(999);
        check(s.getValue() == 100, "setValue clamp max");

        s.setMinimum(10);
        check(s.getMinimum() == 10, "setMinimum");
        s.setMaximum(90);
        check(s.getMaximum() == 90, "setMaximum");

        s.setInverted(true);
        check(s.getInverted() == true, "inverted on");
        s.setInverted(false);
        check(s.getInverted() == false, "inverted off");

        s.setMajorTickSpacing(25);
        check(s.getMajorTickSpacing() == 25, "major spacing");
        s.setMinorTickSpacing(5);
        check(s.getMinorTickSpacing() == 5, "minor spacing");

        s.setSnapToTicks(true);
        check(s.getSnapToTicks() == true, "snap on");
        s.setSnapToTicks(false);
        check(s.getSnapToTicks() == false, "snap off");

        s.setPaintTicks(true);
        check(s.getPaintTicks() == true, "paint ticks on");
        s.setPaintTicks(false);
        check(s.getPaintTicks() == false, "paint ticks off");

        s.setPaintTrack(false);
        check(s.getPaintTrack() == false, "paint track off");
        s.setPaintTrack(true);
        check(s.getPaintTrack() == true, "paint track on");

        s.setPaintLabels(true);
        check(s.getPaintLabels() == true, "paint labels on");
        s.setPaintLabels(false);
        check(s.getPaintLabels() == false, "paint labels off");

        s.setValueIsAdjusting(true);
        check(s.getValueIsAdjusting() == true, "value adjusting on");
        s.setValueIsAdjusting(false);
        check(s.getValueIsAdjusting() == false, "value adjusting off");

        JSlider extSlider = new JSlider(0, 100, 20);
        extSlider.setExtent(12);
        check(extSlider.getExtent() == 12, "extent set");
        check(extSlider.getValue() == 20, "value unchanged by extent");

        Hashtable<Integer, JComponent> labels = new Hashtable<>();
        labels.put(Integer.valueOf(0), new JLabel("low"));
        labels.put(Integer.valueOf(100), new JLabel("high"));
        s.setLabelTable(labels);
        check(s.getLabelTable() == labels, "label table roundtrip same");

        JSlider clean = new JSlider(0, 100, 50);
        Hashtable<Integer, JComponent> std = clean.createStandardLabels(10);
        check(std.size() == 11, "standard labels count");
        check(std.containsKey(Integer.valueOf(0)), "standard label 0");
        check(std.containsKey(Integer.valueOf(100)), "standard label 100");
        Hashtable<Integer, JComponent> std2 = clean.createStandardLabels(10, 2);
        check(std2.containsKey(Integer.valueOf(2)), "standard label start 2");
        check(std2.containsKey(Integer.valueOf(12)), "standard label 12");

        s.setOrientation(SwingConstants.VERTICAL);
        check(s.getOrientation() == SwingConstants.VERTICAL, "setOrientation vertical");
        s.setOrientation(SwingConstants.HORIZONTAL);
        check(s.getOrientation() == SwingConstants.HORIZONTAL, "setOrientation horizontal");
        boolean iae = false;
        try { s.setOrientation(77); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "invalid orientation IAE");
    }
}
