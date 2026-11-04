/*
 * @test
 * @summary javax.swing.border.AbstractBorder conformance: white-box structure + black-box behavior.
 * @run main AbstractBorderTest
 */

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;

import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class AbstractBorderTest {
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
        Class<?> c = Class.forName("javax.swing.border.AbstractBorder");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.border.Border"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        AbstractBorder b = new AbstractBorder() {};

        // default opaque is false; default insets are all zero
        check(!b.isBorderOpaque(), "default not opaque");
        check(insetsEq(b.getBorderInsets(null), 0, 0, 0, 0), "default insets zero");

        // reinitialize an existing Insets instance; returns the same object
        Insets reusable = new Insets(9, 9, 9, 9);
        Insets returned = b.getBorderInsets(null, reusable);
        check(returned == reusable, "returns same insets object");
        check(insetsEq(reusable, 0, 0, 0, 0), "reused insets zeroed");

        // null insets argument -> NPE
        boolean npe = false;
        try { b.getBorderInsets(null, null); } catch (NullPointerException e) { npe = true; }
        check(npe, "getBorderInsets(c, null) NPE");

        // interior rectangle with zero insets leaves coordinates unchanged
        Rectangle r = b.getInteriorRectangle(null, 10, 20, 100, 80);
        check(r.equals(new Rectangle(10, 20, 100, 80)), "interior rect unchanged for zero insets");

        // static form subtracts a concrete border's insets
        Rectangle r2 = AbstractBorder.getInteriorRectangle(null, new EmptyBorder(1, 2, 3, 4), 0, 0, 100, 100);
        check(r2.equals(new Rectangle(2, 1, 94, 96)), "static interior rect subtracts insets");

        // null border behaves as zero insets
        Rectangle r3 = AbstractBorder.getInteriorRectangle(null, null, 5, 6, 50, 40);
        check(r3.equals(new Rectangle(5, 6, 50, 40)), "null border == zero insets");

        // baseline defaults to -1; negative width/height rejected
        check(b.getBaseline(null, 10, 20) == -1, "default baseline -1");
        boolean iae1 = false;
        try { b.getBaseline(null, -1, 20); } catch (IllegalArgumentException e) { iae1 = true; }
        check(iae1, "negative width IAE");
        boolean iae2 = false;
        try { b.getBaseline(null, 10, -1); } catch (IllegalArgumentException e) { iae2 = true; }
        check(iae2, "negative height IAE");

        // baseline resize behavior: null component NPE, otherwise OTHER
        boolean npe2 = false;
        try { b.getBaselineResizeBehavior(null); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "baseline resize null NPE");
        Component comp = new Component() {};
        check(b.getBaselineResizeBehavior(comp) == Component.BaselineResizeBehavior.OTHER, "resize behavior OTHER");

        // paintBorder is a documented no-op; smoke-call it headlessly
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        b.paintBorder(comp, g, 0, 0, 4, 4);
        g.dispose();
        check(true, "paintBorder no-op smoke");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.border.AbstractBorder", false, AbstractBorderTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "javax.swing.border.Border"), "implements javax.swing.border.Border");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("paintBorder"), "method paintBorder");
        check(names.contains("getBorderInsets"), "method getBorderInsets");
        check(names.contains("getBorderInsets"), "method getBorderInsets");
        check(names.contains("isBorderOpaque"), "method isBorderOpaque");
        check(names.contains("getInteriorRectangle"), "method getInteriorRectangle");
        check(names.contains("getInteriorRectangle"), "method getInteriorRectangle");
        check(names.contains("getBaseline"), "method getBaseline");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
