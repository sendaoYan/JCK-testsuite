/*
 * @test
 * @summary javax.swing.border.StrokeBorder conformance: white-box structure + black-box behavior.
 * @run main StrokeBorderTest
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.border.StrokeBorder;

public class StrokeBorderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean insetsEq(Insets in, int top, int left, int bottom, int right) {
        return in != null && in.top == top && in.left == left && in.bottom == bottom && in.right == right;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.border.StrokeBorder");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.AbstractBorder"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // one-arg constructor stores the stroke and leaves paint null
        BasicStroke stroke = new BasicStroke(2.5f);
        StrokeBorder sb = new StrokeBorder(stroke);
        check(sb.getStroke() == stroke, "stroke round-trip");
        check(sb.getPaint() == null, "default paint null");
        check(insetsEq(sb.getBorderInsets(null), 3, 3, 3, 3), "ceil(2.5) == 3");

        // two-arg constructor stores both stroke and paint; whole widths round exactly
        Paint p = Color.RED;
        StrokeBorder sb2 = new StrokeBorder(new BasicStroke(2.0f), p);
        check(sb2.getPaint() == p, "paint round-trip");
        check(sb2.getStroke().getLineWidth() == 2.0f, "stroke width");
        check(insetsEq(sb2.getBorderInsets(null), 2, 2, 2, 2), "ceil(2.0) == 2");

        // zero-width stroke produces zero insets
        StrokeBorder sb3 = new StrokeBorder(new BasicStroke(0.0f));
        check(insetsEq(sb3.getBorderInsets(null), 0, 0, 0, 0), "zero width insets");

        // null stroke -> NPE
        boolean npe = false;
        try { new StrokeBorder(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "null stroke NPE");

        // paintBorder smoke
        Component comp = new Component() {};
        comp.setForeground(Color.BLUE);
        BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        sb.paintBorder(comp, g, 0, 0, 20, 20);
        sb2.paintBorder(comp, g, 0, 0, 20, 20);
        g.dispose();
        check(true, "paintBorder smoke");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
