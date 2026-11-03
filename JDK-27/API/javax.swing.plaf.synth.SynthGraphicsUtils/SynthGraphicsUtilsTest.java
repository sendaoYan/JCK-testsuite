/*
 * @test
 * @summary javax.swing.plaf.synth.SynthGraphicsUtils conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main SynthGraphicsUtilsTest
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthGraphicsUtils;
import javax.swing.plaf.synth.SynthStyle;

public class SynthGraphicsUtilsTest {
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
        runOnEDT(SynthGraphicsUtilsTest::testWhiteBox);
        runOnEDT(SynthGraphicsUtilsTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthGraphicsUtils");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        SynthGraphicsUtils g = new SynthGraphicsUtils();
        check(g != null, "constructor");

        JComponent comp = new JLabel("hello");
        SynthContext ctx = new SynthContext(comp, Region.LABEL, minimalStyle(), SynthConstants.ENABLED);
        Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
        FontMetrics fm = comp.getFontMetrics(font);
        check(fm != null, "font metrics");

        int width = g.computeStringWidth(ctx, font, fm, "hello");
        check(width > 0, "computeStringWidth positive");
        int charHeight = g.getMaximumCharHeight(ctx);
        check(charHeight > 0, "getMaximumCharHeight positive");

        Dimension pref = g.getPreferredSize(ctx, font, "hello", null, 0, 0, 0, 0, 0, -1);
        check(pref != null && pref.width >= 0 && pref.height >= 0, "preferred size");
        Dimension min = g.getMinimumSize(ctx, font, "hello", null, 0, 0, 0, 0, 0, -1);
        check(min != null && min.width >= 0 && min.height >= 0, "minimum size");
        Dimension max = g.getMaximumSize(ctx, font, "hello", null, 0, 0, 0, 0, 0, -1);
        check(max != null && max.width >= 0 && max.height >= 0, "maximum size");

        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g.paintText(ctx, g2, "hello", 10, 20, -1);
        g.drawLine(ctx, "key", g2, 0, 0, 50, 50);
        check(true, "paintText and drawLine complete");

        Icon icon = new ImageIcon(img);
        check(SynthGraphicsUtils.getIconWidth(icon, ctx) == 100, "getIconWidth");
        check(SynthGraphicsUtils.getIconHeight(icon, ctx) == 50, "getIconHeight");
        check(SynthGraphicsUtils.getIconWidth(null, ctx) == 0, "getIconWidth null");
        check(SynthGraphicsUtils.getIconHeight(null, ctx) == 0, "getIconHeight null");
        g2.dispose();
    }
}
