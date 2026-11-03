/*
 * @test
 * @summary java.awt.image.MultiResolutionImage conformance: white-box structure plus black-box behavior.
 * @run main MultiResolutionImageTest
 */

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.lang.reflect.*;

public class MultiResolutionImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.MultiResolutionImage");
        int m = c.getModifiers();
        check(Modifier.isInterface(m), "interface");
        check(hasMethod(c, "getResolutionVariant", double.class, double.class), "getResolutionVariant");
        check(hasMethod(c, "getResolutionVariants"), "getResolutionVariants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final Image v = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        MultiResolutionImage mri = new MultiResolutionImage() {
            public Image getResolutionVariant(double w, double h) { return v; }
            public java.util.List<Image> getResolutionVariants() { return Collections.singletonList(v); }
        };
        check(mri.getResolutionVariant(4, 4) == v, "getResolutionVariant");
        check(mri.getResolutionVariants().size() == 1, "getResolutionVariants size 1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
