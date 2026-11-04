/*
 * @test
 * @summary javax.swing.border.CompoundBorder conformance: white-box structure + black-box behavior.
 * @run main CompoundBorderTest
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class CompoundBorderTest {
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
        Class<?> c = Class.forName("javax.swing.border.CompoundBorder");
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
        // default constructor yields null borders, zero insets, and opaque (null == opaque)
        CompoundBorder cb = new CompoundBorder();
        check(cb.getOutsideBorder() == null && cb.getInsideBorder() == null, "default null borders");
        check(insetsEq(cb.getBorderInsets(null), 0, 0, 0, 0), "default insets zero");
        check(cb.isBorderOpaque(), "default opaque (null counts opaque)");

        // composing two EmptyBorders sums their insets; empty borders are not opaque
        EmptyBorder out = new EmptyBorder(1, 2, 3, 4);
        EmptyBorder in = new EmptyBorder(10, 20, 30, 40);
        CompoundBorder cb2 = new CompoundBorder(out, in);
        check(cb2.getOutsideBorder() == out, "outside round-trip");
        check(cb2.getInsideBorder() == in, "inside round-trip");
        check(insetsEq(cb2.getBorderInsets(null), 11, 22, 33, 44), "insets summed");
        check(!cb2.isBorderOpaque(), "empty borders not opaque");

        // two opaque line borders compose into an opaque border with summed thickness
        LineBorder lout = new LineBorder(Color.RED, 2);
        LineBorder lin = new LineBorder(Color.BLUE, 3);
        CompoundBorder cb3 = new CompoundBorder(lout, lin);
        check(insetsEq(cb3.getBorderInsets(null), 5, 5, 5, 5), "line insets summed");
        check(cb3.isBorderOpaque(), "line borders opaque");

        // a null inside border is treated as opaque
        CompoundBorder cb4 = new CompoundBorder(lout, null);
        check(cb4.getInsideBorder() == null, "null inside");
        check(cb4.isBorderOpaque(), "null inside counted opaque");

        // paintBorder smoke: composes two no-op borders headlessly
        BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        cb2.paintBorder(new Component() {}, g, 0, 0, 20, 20);
        g.dispose();
        check(true, "compound paintBorder smoke");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
