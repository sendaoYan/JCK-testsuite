/*
 * @test
 * @summary javax.swing.JProgressBar conformance: white-box structure + black-box behavior
 *          + deep component-layer contracts (derived strings, event counting, null-model quirk).
 *          + deep component-layer contracts (derived strings, event counting, null-model quirk).
 * @key headful
 * @run main JProgressBarTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class JProgressBarTest {
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
        runOnEDT(JProgressBarTest::testWhiteBox);
        runOnEDT(JProgressBarTest::testBlackBox);
        runOnEDT(JProgressBarTest::testDeepContracts);
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deep component-layer contracts (derived strings, event counting, null-model quirk) ----
    static void testDeepContracts() throws Exception {
        // 2-arg ctor starts at min
        check(new JProgressBar(5, 15).getValue() == 5, "min-max ctor value starts at min");

        JProgressBar pb = new JProgressBar();
        check("0%".equals(pb.getString()), "default derived string 0%");
        pb.setValue(50);
        check("50%".equals(pb.getString()), "derived string 50% at midpoint");
        check(Math.abs(pb.getPercentComplete() - 0.5) < 1e-9, "percent 0.5");
        pb.setValue(500);
        check(pb.getValue() == 100 && "100%".equals(pb.getString()), "clamp to max derives 100%");
        check(Math.abs(pb.getPercentComplete() - 1.0) < 1e-9, "percent 1.0 at clamp");
        pb.setValue(-9);
        check(pb.getValue() == 0 && "0%".equals(pb.getString()), "clamp to min derives 0%");

        pb.setValue(25);
        pb.setString("done");
        check("done".equals(pb.getString()), "custom string overrides derived");
        pb.setValue(75);
        check("done".equals(pb.getString()), "custom string stable across value changes");
        pb.setString(null);
        check("75%".equals(pb.getString()), "null string restores derived");

        pb.setStringPainted(true);
        check(pb.isStringPainted(), "setStringPainted(true)");
        pb.setIndeterminate(true);
        check(pb.isIndeterminate(), "setIndeterminate(true)");
        pb.setIndeterminate(false);
        check(!pb.isIndeterminate(), "setIndeterminate(false)");
        check(pb.isStringPainted(), "stringPainted kept across indeterminate toggle");

        // ChangeEvent counting + propagation
        final int[] n = new int[1];
        final javax.swing.event.ChangeEvent[] last = new javax.swing.event.ChangeEvent[1];
        pb.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) { n[0]++; last[0] = e; }
        });
        pb.setValue(40);
        check(n[0] == 1 && last[0].getSource() == pb, "setValue fires one ChangeEvent, source bar");
        pb.setValue(40);
        check(n[0] == 1, "same-value setValue fires nothing");
        DefaultBoundedRangeModel m = new DefaultBoundedRangeModel(7, 0, 0, 9);
        pb.setModel(m);
        check(pb.getModel() == m && pb.getValue() == 7, "model swap identity and value");
        check(n[0] == 1, "model swap itself fires no ChangeEvent");
        n[0] = 0;
        m.setValue(8);
        check(pb.getValue() == 8 && n[0] == 1, "model mutation propagates one ChangeEvent");
        pb.setModel(pb.getModel());
        check(n[0] == 1, "same-model re-set fires nothing");

        // setModel(null) is ACCEPTED here (contrast with JSlider NPE); later ops NPE
        JProgressBar nb = new JProgressBar();
        nb.setModel(null);
        check(nb.getModel() == null, "setModel(null) accepted, getModel null");
        try {
            nb.setValue(3);
            check(false, "setValue after null model must throw");
        } catch (NullPointerException e) { check(true, "setValue after null model NPE"); }

        // orientation negative
        pb.setOrientation(SwingConstants.VERTICAL);
        check(pb.getOrientation() == SwingConstants.VERTICAL, "setOrientation(VERTICAL)");
        try {
            pb.setOrientation(99);
            check(false, "setOrientation(99) must throw");
        } catch (IllegalArgumentException e) { check(true, "setOrientation(99) IAE"); }
        check(pb.getOrientation() == SwingConstants.VERTICAL, "orientation kept after rejection");

        check(pb.getUI() != null && pb.getUI().getClass().getName().endsWith("MetalProgressBarUI"),
                "Metal binding installs MetalProgressBarUI");
        check("ProgressBarUI".equals(pb.getUIClassID()), "getUIClassID ProgressBarUI");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.JProgressBar");
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
        JProgressBar p = new JProgressBar();
        check(p.getValue() == 0, "default value 0");
        check(p.getMinimum() == 0, "default min 0");
        check(p.getMaximum() == 100, "default max 100");
        check(p.getOrientation() == SwingConstants.HORIZONTAL, "default orientation");
        check(p.isIndeterminate() == false, "default not indeterminate");
        check(p.isStringPainted() == false, "default string not painted");
        check(p.isBorderPainted() == true, "default border painted");

        JProgressBar vert = new JProgressBar(SwingConstants.VERTICAL);
        check(vert.getOrientation() == SwingConstants.VERTICAL, "vertical ctor");
        check(vert.getValue() == 0 && vert.getMinimum() == 0 && vert.getMaximum() == 100, "vertical defaults");

        JProgressBar range = new JProgressBar(10, 50);
        check(range.getMinimum() == 10, "range min");
        check(range.getMaximum() == 50, "range max");
        check(range.getValue() == 10, "range value == min");

        JProgressBar full = new JProgressBar(SwingConstants.VERTICAL, 5, 25);
        check(full.getOrientation() == SwingConstants.VERTICAL, "full ctor orientation");
        check(full.getMinimum() == 5, "full ctor min");
        check(full.getMaximum() == 25, "full ctor max");
        check(full.getValue() == 5, "full ctor value");

        DefaultBoundedRangeModel m = new DefaultBoundedRangeModel(3, 0, 0, 10);
        JProgressBar modeled = new JProgressBar(m);
        check(modeled.getModel() == m, "model ctor same model");
        check(modeled.getValue() == 3, "model ctor value");

        p.setValue(50);
        check(p.getValue() == 50, "setValue 50");
        p.setValue(-7);
        check(p.getValue() == 0, "setValue clamps to min");
        p.setValue(1000);
        check(p.getValue() == 100, "setValue clamps to max");

        p.setMinimum(20);
        check(p.getMinimum() == 20, "setMinimum");
        p.setMaximum(80);
        check(p.getMaximum() == 80, "setMaximum");

        p.setValue(40);
        check(p.getValue() == 40, "setValue 40");
        // reset to a clean model for percent math
        p.setModel(new DefaultBoundedRangeModel(25, 0, 0, 100));
        check(p.getValue() == 25, "model value 25");
        check(p.getPercentComplete() == 0.25, "percent 0.25");
        p.setValue(50);
        check(p.getPercentComplete() == 0.5, "percent 0.5");
        p.setValue(100);
        check(p.getPercentComplete() == 1.0, "percent 1.0");
        p.setValue(0);
        check(p.getPercentComplete() == 0.0, "percent 0.0");

        p.setIndeterminate(true);
        check(p.isIndeterminate() == true, "indeterminate on");
        p.setIndeterminate(false);
        check(p.isIndeterminate() == false, "indeterminate off");

        p.setStringPainted(true);
        check(p.isStringPainted() == true, "string painted on");
        p.setString("Working...");
        check("Working...".equals(p.getString()), "custom string roundtrip");
        p.setString(null);
        check(p.getString() != null, "reset string non-null");

        p.setBorderPainted(false);
        check(p.isBorderPainted() == false, "border unpainted");
        p.setBorderPainted(true);
        check(p.isBorderPainted() == true, "border painted");

        p.setOrientation(SwingConstants.VERTICAL);
        check(p.getOrientation() == SwingConstants.VERTICAL, "setOrientation vertical");
        p.setOrientation(SwingConstants.HORIZONTAL);
        check(p.getOrientation() == SwingConstants.HORIZONTAL, "setOrientation horizontal");
        boolean iae = false;
        try { p.setOrientation(987); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "invalid orientation IAE");
    }
}
