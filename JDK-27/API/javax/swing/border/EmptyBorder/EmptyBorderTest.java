/*
 * @test
 * @summary javax.swing.border.EmptyBorder conformance: white-box structure + black-box behavior.
 * @run main EmptyBorderTest
 */

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.border.EmptyBorder;

public class EmptyBorderTest {
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
        Class<?> c = Class.forName("javax.swing.border.EmptyBorder");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.border.AbstractBorder"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.io.Serializable"), "interface 0");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // int constructor round-trips top/left/bottom/right into getBorderInsets()
        EmptyBorder eb = new EmptyBorder(1, 2, 3, 4);
        check(insetsEq(eb.getBorderInsets(), 1, 2, 3, 4), "int ctor round-trip");
        check(!eb.isBorderOpaque(), "not opaque");

        // Insets constructor copies the supplied insets
        Insets src = new Insets(5, 6, 7, 8);
        EmptyBorder eb2 = new EmptyBorder(src);
        check(insetsEq(eb2.getBorderInsets(), 5, 6, 7, 8), "insets ctor round-trip");

        // reinitialize an existing Insets object in place and return it
        Insets reusable = new Insets(9, 9, 9, 9);
        Insets returned = eb.getBorderInsets(null, reusable);
        check(returned == reusable, "returns same insets object");
        check(insetsEq(reusable, 1, 2, 3, 4), "reused insets overwritten");

        // getBorderInsets() allocates a fresh Insets each call
        Insets a = eb.getBorderInsets();
        Insets b = eb.getBorderInsets();
        check(a != b && insetsEq(a, 1, 2, 3, 4) && insetsEq(b, 1, 2, 3, 4), "fresh insets per call");

        // null Insets constructor -> NPE
        boolean npe1 = false;
        try { new EmptyBorder((Insets) null); } catch (NullPointerException e) { npe1 = true; }
        check(npe1, "null insets ctor NPE");

        // null insets argument -> NPE
        boolean npe2 = false;
        try { eb.getBorderInsets(null, null); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "getBorderInsets(c, null) NPE");

        // paintBorder is a documented no-op; smoke-call it headlessly
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        eb.paintBorder(new Component() {}, g, 0, 0, 4, 4);
        g.dispose();
        check(true, "paintBorder no-op smoke");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static void test() throws Exception {
        EmptyBorder b = new EmptyBorder(1, 2, 3, 4);
        java.awt.Insets i = b.getBorderInsets(null);
        check(i.top == 1 && i.left == 2 && i.bottom == 3 && i.right == 4, "insets");
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
        EmptyBorder eb = new EmptyBorder(4, 5, 6, 7);
        EmptyBorder copy = roundtrip(eb);
        check(copy != eb && copy instanceof EmptyBorder, "serialized EmptyBorder distinct instance");
        check(insetsEq(copy.getBorderInsets(), 4, 5, 6, 7), "serialized insets preserved");
        check(!copy.isBorderOpaque(), "serialized border still not opaque");

        javax.swing.border.Border f1 = javax.swing.BorderFactory.createEmptyBorder(2, 3, 4, 5);
        check(f1 instanceof EmptyBorder, "createEmptyBorder(int,int,int,int) type");
        check(insetsEq(((EmptyBorder) f1).getBorderInsets(), 2, 3, 4, 5), "createEmptyBorder(int x4) insets");
        javax.swing.border.Border f0 = javax.swing.BorderFactory.createEmptyBorder();
        check(insetsEq(((EmptyBorder) f0).getBorderInsets(), 0, 0, 0, 0), "createEmptyBorder() zero insets");
        check(!f1.isBorderOpaque(), "createEmptyBorder not opaque");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        test();
        testExtraContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
