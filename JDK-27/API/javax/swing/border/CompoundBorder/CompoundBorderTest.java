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


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        CompoundBorder b = new CompoundBorder();
        check(b.getInsideBorder() == null && b.getOutsideBorder() == null, "empty");
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
        CompoundBorder cb = new CompoundBorder(new LineBorder(Color.RED, 2), new EmptyBorder(1, 2, 3, 4));
        CompoundBorder ccopy = roundtrip(cb);
        check(ccopy != cb, "serialized CompoundBorder distinct instance");
        check(ccopy.getOutsideBorder() instanceof LineBorder, "serialized outside border preserved");
        check(ccopy.getInsideBorder() instanceof EmptyBorder, "serialized inside border preserved");
        LineBorder outer = (LineBorder) ccopy.getOutsideBorder();
        check(outer.getThickness() == 2 && outer.getLineColor().equals(Color.RED), "serialized outer border data preserved");
        check(insetsEq(ccopy.getBorderInsets(null), 3, 4, 5, 6), "serialized compound insets preserved");

        javax.swing.border.Border fc = javax.swing.BorderFactory.createCompoundBorder(
                new EmptyBorder(1, 1, 1, 1), new EmptyBorder(2, 2, 2, 2));
        check(fc instanceof CompoundBorder, "createCompoundBorder(Border,Border) type");
        check(insetsEq(fc.getBorderInsets(null), 3, 3, 3, 3), "createCompoundBorder insets summed");
        javax.swing.border.Border fn = javax.swing.BorderFactory.createCompoundBorder();
        check(fn instanceof CompoundBorder, "createCompoundBorder() no-arg type");
        check(insetsEq(fn.getBorderInsets(null), 0, 0, 0, 0), "createCompoundBorder() zero insets");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        testExtraContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
