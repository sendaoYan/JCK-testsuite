/*
 * @test
 * @summary javax.swing.plaf.synth.SynthLabelUI conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main SynthLabelUITest
 */

import javax.swing.plaf.synth.SynthLabelUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;
import javax.swing.plaf.synth.SynthUI;
import javax.swing.*;


public class SynthLabelUITest {
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

    static SynthStyle minimalStyle() {
        return new SynthStyle() {
            protected Color getColorForState(SynthContext c, ColorType t) { return Color.BLACK; }
            protected Font getFontForState(SynthContext c) { return new Font(Font.DIALOG, Font.PLAIN, 12); }
        };
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(SynthLabelUITest::testWhiteBox);
        runOnEDT(SynthLabelUITest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthLabelUI");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicLabelUI"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.plaf.synth.SynthUI"), "interface 0");
    }
    static void testBlackBox() throws Exception {
        SynthStyleFactory old = SynthLookAndFeel.getStyleFactory();
        SynthLookAndFeel.setStyleFactory(new SynthStyleFactory() {
            public SynthStyle getStyle(JComponent c, Region r) { return minimalStyle(); }
        });
        try {
            JLabel comp = new JLabel("x");
            ComponentUI ui = SynthLabelUI.createUI(comp);
            check(ui != null, "createUI non-null");
            check(ui instanceof SynthLabelUI, "createUI returns SynthLabelUI");
            ui.installUI(comp);
            try {
                SynthContext ctx = ((SynthUI) ui).getContext(comp);
                check(ctx != null, "context non-null");
                check(ctx.getComponent() == comp, "context component");
                check(ctx.getRegion() != null && "Label".equals(ctx.getRegion().getName()), "context region");
                check(ctx.getStyle() != null, "context style");
                Dimension pref = ui.getPreferredSize(comp);
                if (pref != null) { check(pref.width >= 0 && pref.height >= 0, "preferred size non-negative"); }
                Dimension min = ui.getMinimumSize(comp);
                if (min != null) { check(min.width >= 0 && min.height >= 0, "minimum size non-negative"); }
                Dimension max = ui.getMaximumSize(comp);
                if (max != null) { check(max.width >= 0 && max.height >= 0, "maximum size non-negative"); }
            } finally {
                ui.uninstallUI(comp);
            }
        } finally {
            SynthLookAndFeel.setStyleFactory(old);
        }
    }
}
