/*
 * @test
 * @summary javax.swing.border.LineBorder conformance: white-box structure + black-box behavior.
 * @run main LineBorderTest
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class LineBorderTest {
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
        Class<?> c = Class.forName("javax.swing.border.LineBorder");
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
        // full constructor round-trips color, thickness and rounded corners
        LineBorder lb = new LineBorder(Color.RED, 2, true);
        check(lb.getLineColor() == Color.RED, "line color");
        check(lb.getThickness() == 2, "thickness");
        check(lb.getRoundedCorners(), "rounded corners");
        check(insetsEq(lb.getBorderInsets(null), 2, 2, 2, 2), "insets == thickness");
        check(!lb.isBorderOpaque(), "rounded not opaque");

        // single-arg constructor defaults to thickness 1 and square corners
        LineBorder lb2 = new LineBorder(Color.BLUE);
        check(lb2.getThickness() == 1, "default thickness 1");
        check(!lb2.getRoundedCorners(), "default square corners");
        check(lb2.isBorderOpaque(), "square opaque");

        // two-arg constructor round-trips thickness and keeps square corners
        LineBorder lb3 = new LineBorder(Color.GREEN, 4);
        check(insetsEq(lb3.getBorderInsets(null), 4, 4, 4, 4), "thickness 4 insets");
        check(!lb3.getRoundedCorners(), "square corners");
        check(lb3.isBorderOpaque(), "square opaque");

        // shared black/gray factories are cached singletons with the expected color/thickness
        Border bl = LineBorder.createBlackLineBorder();
        check(bl instanceof LineBorder, "black factory type");
        check(((LineBorder) bl).getLineColor() == Color.BLACK, "black color");
        check(((LineBorder) bl).getThickness() == 1, "black thickness");
        Border gl = LineBorder.createGrayLineBorder();
        check(((LineBorder) gl).getLineColor() == Color.GRAY, "gray color");
        check(bl == LineBorder.createBlackLineBorder(), "black shared singleton");
        check(gl == LineBorder.createGrayLineBorder(), "gray shared singleton");

        // paintBorder smoke (rounded and square)
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Component comp = new Component() {};
        lb.paintBorder(comp, g, 0, 0, 16, 16);
        lb3.paintBorder(comp, g, 0, 0, 16, 16);
        g.dispose();
        check(true, "paintBorder smoke");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
