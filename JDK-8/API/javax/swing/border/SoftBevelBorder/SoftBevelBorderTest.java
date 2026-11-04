/*
 * @test
 * @summary javax.swing.border.SoftBevelBorder conformance: white-box structure + black-box behavior.
 * @run main SoftBevelBorderTest
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

public class SoftBevelBorderTest {
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
        Class<?> c = Class.forName("javax.swing.border.SoftBevelBorder");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.BevelBorder"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // one-arg constructor: type round-trip, insets 3, not opaque
        SoftBevelBorder s0 = new SoftBevelBorder(BevelBorder.RAISED);
        check(s0.getBevelType() == BevelBorder.RAISED, "raised type");
        check(insetsEq(s0.getBorderInsets(null), 3, 3, 3, 3), "insets 3");
        check(!s0.isBorderOpaque(), "not opaque");

        // two-arg constructor delegates the color derivation to BevelBorder
        Color hi = Color.WHITE;
        Color sh = Color.BLACK;
        SoftBevelBorder s1 = new SoftBevelBorder(BevelBorder.LOWERED, hi, sh);
        check(s1.getBevelType() == BevelBorder.LOWERED, "lowered type");
        check(s1.getHighlightOuterColor().equals(hi.brighter()), "outer highlight brighter");
        check(s1.getHighlightInnerColor() == hi, "inner highlight");
        check(s1.getShadowOuterColor() == sh, "outer shadow");
        check(s1.getShadowInnerColor().equals(sh.brighter()), "inner shadow brighter");

        // five-arg constructor round-trips all four colors exactly
        SoftBevelBorder s2 = new SoftBevelBorder(BevelBorder.RAISED, Color.RED, Color.GREEN, Color.BLUE, Color.WHITE);
        check(s2.getHighlightOuterColor() == Color.RED, "5-arg outer highlight");
        check(s2.getHighlightInnerColor() == Color.GREEN, "5-arg inner highlight");
        check(s2.getShadowOuterColor() == Color.BLUE, "5-arg outer shadow");
        check(s2.getShadowInnerColor() == Color.WHITE, "5-arg inner shadow");

        // paintBorder smoke for raised and lowered softened bevels
        Component comp = new Component() {};
        comp.setBackground(Color.GRAY);
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        s1.paintBorder(comp, g, 0, 0, 16, 16);
        s2.paintBorder(comp, g, 0, 0, 16, 16);
        g.dispose();
        check(true, "paintBorder smoke");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        SoftBevelBorder b = new SoftBevelBorder(SoftBevelBorder.RAISED);
        check(b.getBevelType() == SoftBevelBorder.RAISED, "type");
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
        SoftBevelBorder sb = new SoftBevelBorder(BevelBorder.RAISED, Color.WHITE, Color.BLACK);
        SoftBevelBorder scopy = roundtrip(sb);
        check(scopy != sb, "serialized SoftBevelBorder distinct instance");
        check(scopy.getBevelType() == BevelBorder.RAISED, "serialized soft bevel type preserved");
        check(!scopy.isBorderOpaque(), "serialized soft bevel still not opaque");

        javax.swing.border.Border sr = javax.swing.BorderFactory.createSoftBevelBorder(BevelBorder.RAISED);
        check(sr instanceof SoftBevelBorder, "createSoftBevelBorder(int) type");
        check(((SoftBevelBorder) sr).getBevelType() == BevelBorder.RAISED, "createSoftBevelBorder(int) type round-trip");
        javax.swing.border.Border sl = javax.swing.BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED, Color.WHITE, Color.BLACK);
        check(((SoftBevelBorder) sl).getBevelType() == BevelBorder.LOWERED, "createSoftBevelBorder 4-arg lowered");
        javax.swing.border.Border s4 = javax.swing.BorderFactory.createSoftBevelBorder(BevelBorder.RAISED,
                Color.WHITE, Color.LIGHT_GRAY, Color.DARK_GRAY, Color.BLACK);
        check(((SoftBevelBorder) s4).getBevelType() == BevelBorder.RAISED, "createSoftBevelBorder 5-arg raised");
        check(insetsEq(sr.getBorderInsets(null), 3, 3, 3, 3), "soft bevel factory insets 3");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        testExtraContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
