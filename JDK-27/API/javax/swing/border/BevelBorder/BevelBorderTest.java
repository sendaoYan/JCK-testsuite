/*
 * @test
 * @summary javax.swing.border.BevelBorder conformance: white-box structure + black-box behavior.
 * @run main BevelBorderTest
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.border.BevelBorder;

public class BevelBorderTest {
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
        Class<?> c = Class.forName("javax.swing.border.BevelBorder");
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

        check(BevelBorder.RAISED == 0 && BevelBorder.LOWERED == 1, "bevel type constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // one-arg constructor stores the type and leaves all colors null
        BevelBorder b0 = new BevelBorder(BevelBorder.RAISED);
        check(b0.getBevelType() == BevelBorder.RAISED, "raised type");
        check(b0.getHighlightOuterColor() == null && b0.getHighlightInnerColor() == null
                && b0.getShadowOuterColor() == null && b0.getShadowInnerColor() == null, "default null colors");
        check(insetsEq(b0.getBorderInsets(null), 2, 2, 2, 2), "insets 2");
        check(b0.isBorderOpaque(), "opaque");

        // two-arg constructor derives the four colors from highlight/shadow
        Color hi = Color.WHITE;
        Color sh = Color.BLACK;
        BevelBorder b1 = new BevelBorder(BevelBorder.LOWERED, hi, sh);
        check(b1.getBevelType() == BevelBorder.LOWERED, "lowered type");
        check(b1.getHighlightOuterColor().equals(hi.brighter()), "outer highlight = brighter");
        check(b1.getHighlightInnerColor() == hi, "inner highlight = highlight");
        check(b1.getShadowOuterColor() == sh, "outer shadow = shadow");
        check(b1.getShadowInnerColor().equals(sh.brighter()), "inner shadow = brighter");

        // five-arg constructor round-trips all four colors exactly
        BevelBorder b2 = new BevelBorder(BevelBorder.RAISED, Color.RED, Color.GREEN, Color.BLUE, Color.WHITE);
        check(b2.getHighlightOuterColor() == Color.RED, "5-arg outer highlight");
        check(b2.getHighlightInnerColor() == Color.GREEN, "5-arg inner highlight");
        check(b2.getShadowOuterColor() == Color.BLUE, "5-arg outer shadow");
        check(b2.getShadowInnerColor() == Color.WHITE, "5-arg inner shadow");

        // derived colors come from the component background
        Component comp = new Component() {};
        comp.setBackground(Color.GRAY);
        check(b0.getHighlightOuterColor(comp).equals(Color.GRAY.brighter().brighter()), "derived outer highlight");
        check(b0.getHighlightInnerColor(comp).equals(Color.GRAY.brighter()), "derived inner highlight");
        check(b0.getShadowOuterColor(comp).equals(Color.GRAY.darker().darker()), "derived outer shadow");
        check(b0.getShadowInnerColor(comp).equals(Color.GRAY.darker()), "derived inner shadow");
        check(b1.getHighlightOuterColor(comp).equals(hi.brighter()), "explicit outer highlight wins");

        // paintBorder smoke for raised and lowered bevels
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        b1.paintBorder(comp, g, 0, 0, 16, 16);
        b2.paintBorder(comp, g, 0, 0, 16, 16);
        g.dispose();
        check(true, "paintBorder smoke");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        BevelBorder b = new BevelBorder(BevelBorder.RAISED);
        check(b.getBevelType() == BevelBorder.RAISED, "type");
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
        BevelBorder bb = new BevelBorder(BevelBorder.LOWERED,
                new Color(1, 1, 1), new Color(2, 2, 2), new Color(3, 3, 3), new Color(4, 4, 4));
        BevelBorder bcopy = roundtrip(bb);
        check(bcopy != bb, "serialized BevelBorder distinct instance");
        check(bcopy.getBevelType() == BevelBorder.LOWERED, "serialized bevel type preserved");
        check(bcopy.getHighlightOuterColor().equals(new Color(1, 1, 1))
              && bcopy.getHighlightInnerColor().equals(new Color(2, 2, 2)), "serialized highlight colors preserved");
        check(bcopy.getShadowOuterColor().equals(new Color(3, 3, 3))
              && bcopy.getShadowInnerColor().equals(new Color(4, 4, 4)), "serialized shadow colors preserved");

        javax.swing.border.Border raised = javax.swing.BorderFactory.createBevelBorder(BevelBorder.RAISED);
        check(raised instanceof BevelBorder, "createBevelBorder(int) type");
        check(((BevelBorder) raised).getBevelType() == BevelBorder.RAISED, "createBevelBorder(int) type round-trip");
        javax.swing.border.Border lowered = javax.swing.BorderFactory.createLoweredBevelBorder();
        check(((BevelBorder) lowered).getBevelType() == BevelBorder.LOWERED, "createLoweredBevelBorder lowered");
        javax.swing.border.Border wc = javax.swing.BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.WHITE, Color.BLACK);
        check(((BevelBorder) wc).getHighlightInnerColor() == Color.WHITE
              && ((BevelBorder) wc).getShadowOuterColor() == Color.BLACK, "createBevelBorder(int,Color,Color) colors");
        javax.swing.border.Border w4 = javax.swing.BorderFactory.createBevelBorder(BevelBorder.LOWERED,
                Color.WHITE, Color.GRAY, Color.DARK_GRAY, Color.BLACK);
        check(((BevelBorder) w4).getHighlightOuterColor() == Color.WHITE, "createBevelBorder 5-arg outer highlight");
        check(((BevelBorder) w4).getShadowInnerColor() == Color.BLACK, "createBevelBorder 5-arg inner shadow");
        check(insetsEq(raised.getBorderInsets(null), 2, 2, 2, 2), "bevel factory insets 2");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        testExtraContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
