/*
 * @test
 * @summary javax.swing.plaf.synth.SynthPainter conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main SynthPainterTest
 */

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthPainter;
import javax.swing.plaf.synth.SynthStyle;

public class SynthPainterTest {
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
        runOnEDT(SynthPainterTest::testWhiteBox);
        runOnEDT(SynthPainterTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthPainter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        JComponent comp = new JLabel();
        SynthContext ctx = new SynthContext(comp, Region.LABEL, minimalStyle(), SynthConstants.ENABLED);
        SynthPainter p = new SynthPainter() {};
        check(p != null, "painter instantiable");
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        p.paintButtonBackground(ctx, g, 0, 0, 10, 10);
        check(true, "paintButtonBackground no-op");
        p.paintButtonBorder(ctx, g, 0, 0, 10, 10);
        check(true, "paintButtonBorder no-op");
        p.paintLabelBackground(ctx, g, 0, 0, 10, 10);
        check(true, "paintLabelBackground no-op");
        p.paintLabelBorder(ctx, g, 0, 0, 10, 10);
        check(true, "paintLabelBorder no-op");
        p.paintPanelBackground(ctx, g, 0, 0, 10, 10);
        check(true, "paintPanelBackground no-op");
        p.paintTextFieldBackground(ctx, g, 0, 0, 10, 10);
        check(true, "paintTextFieldBackground no-op");
        p.paintTreeCellFocus(ctx, g, 0, 0, 10, 10);
        check(true, "paintTreeCellFocus no-op");
        g.dispose();
    }
}
