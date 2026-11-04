/*
 * @test
 * @summary javax.swing.border.Border conformance: white-box structure + black-box behavior.
 * @run main BorderTest
 */

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import javax.swing.border.Border;
import java.util.HashSet;
import java.util.Set;

public class BorderTest {
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
        Class<?> c = Class.forName("javax.swing.border.Border");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");

        // exactly three abstract methods, each public and abstract
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 3, "method count");
        String[] names = new String[ms.length];
        for (int i = 0; i < ms.length; i++) {
            names[i] = ms[i].getName();
            check(Modifier.isAbstract(ms[i].getModifiers()), "method abstract: " + names[i]);
        }
        Arrays.sort(names);
        check(Arrays.equals(names, new String[]{"getBorderInsets", "isBorderOpaque", "paintBorder"}), "method names");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final Insets fixed = new Insets(1, 2, 3, 4);
        final boolean[] painted = new boolean[1];

        Border b = new Border() {
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                painted[0] = true;
            }
            public Insets getBorderInsets(Component c) {
                return new Insets(fixed.top, fixed.left, fixed.bottom, fixed.right);
            }
            public boolean isBorderOpaque() {
                return true;
            }
        };

        // multi-step: query insets, verify values, verify opaque, then paint
        Insets in = b.getBorderInsets(null);
        check(insetsEq(in, 1, 2, 3, 4), "anonymous border insets round-trip");
        check(b.isBorderOpaque(), "anonymous border opaque");

        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        b.paintBorder(null, g, 0, 0, 4, 4);
        g.dispose();
        check(painted[0], "paintBorder invoked");

        // a second anonymous implementation with different values is independent
        Border b2 = new Border() {
            public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {}
            public Insets getBorderInsets(Component c) { return new Insets(0, 0, 0, 0); }
            public boolean isBorderOpaque() { return false; }
        };
        check(insetsEq(b2.getBorderInsets(null), 0, 0, 0, 0), "second impl zero insets");
        check(!b2.isBorderOpaque(), "second impl transparent");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.border.Border", false, BorderTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("paintBorder"), "method paintBorder");
        check(names.contains("getBorderInsets"), "method getBorderInsets");
        check(names.contains("isBorderOpaque"), "method isBorderOpaque");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
