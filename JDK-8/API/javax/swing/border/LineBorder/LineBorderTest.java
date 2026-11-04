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


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        LineBorder b = new LineBorder(java.awt.Color.BLACK, 2);
        check(java.awt.Color.BLACK.equals(b.getLineColor()), "color");
        check(b.getThickness() == 2, "thickness");
        check(b.getBorderInsets(null) != null, "insets");
    }




    @SuppressWarnings("unchecked")
    static <T> T roundtrip(T obj) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(obj);
        java.io.ObjectInputStream oin = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return (T) oin.readObject();
    }

    // ---- Part 2b: serialization round-trip + BorderFactory data contracts ----
    static void testExtraContracts() throws Exception {
        LineBorder lb = new LineBorder(new Color(10, 20, 30), 3, true);
        LineBorder copy = roundtrip(lb);
        check(copy != lb, "serialized LineBorder distinct instance");
        check(copy.getLineColor().equals(new Color(10, 20, 30)), "serialized line color preserved");
        check(copy.getThickness() == 3, "serialized thickness preserved");
        check(copy.getRoundedCorners(), "serialized rounded corners preserved");

        javax.swing.border.Border c1 = javax.swing.BorderFactory.createLineBorder(Color.MAGENTA);
        check(c1 instanceof LineBorder, "createLineBorder(Color) type");
        check(((LineBorder) c1).getThickness() == 1, "createLineBorder(Color) default thickness 1");
        check(!((LineBorder) c1).getRoundedCorners(), "createLineBorder(Color) square corners");
        check(((LineBorder) c1).getLineColor() == Color.MAGENTA, "createLineBorder(Color) color");
        javax.swing.border.Border c2 = javax.swing.BorderFactory.createLineBorder(Color.MAGENTA, 5);
        check(((LineBorder) c2).getThickness() == 5, "createLineBorder(Color,int) thickness");
        javax.swing.border.Border c3 = javax.swing.BorderFactory.createLineBorder(Color.MAGENTA, 5, true);
        check(((LineBorder) c3).getRoundedCorners(), "createLineBorder(Color,int,boolean) rounded");
        check(!c3.isBorderOpaque(), "createLineBorder rounded not opaque");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        testExtraContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
