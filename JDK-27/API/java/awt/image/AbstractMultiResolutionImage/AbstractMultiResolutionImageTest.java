/*
 * @test
 * @summary java.awt.image.AbstractMultiResolutionImage conformance: white-box structure plus black-box behavior.
 * @run main AbstractMultiResolutionImageTest
 */

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.lang.reflect.*;

public class AbstractMultiResolutionImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.AbstractMultiResolutionImage");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "public abstract class");
        check(Image.class.isAssignableFrom(c), "extends Image");
        check(MultiResolutionImage.class.isAssignableFrom(c), "implements MultiResolutionImage");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final BufferedImage variant = new BufferedImage(10, 20, BufferedImage.TYPE_INT_ARGB);
        AbstractMultiResolutionImage mri = new AbstractMultiResolutionImage() {
            protected Image getBaseImage() { return variant; }
            public Image getResolutionVariant(double w, double h) { return variant; }
            public java.util.List<Image> getResolutionVariants() { return Collections.singletonList((Image) variant); }
        };
        check(mri.getWidth(null) == 10, "getWidth delegates to variant");
        check(mri.getHeight(null) == 20, "getHeight delegates to variant");
        check(mri.getResolutionVariants().size() == 1, "getResolutionVariants size 1");
        check(mri.getSource() != null, "getSource non-null");
        check(mri.getProperty("x", null) == variant.getProperty("x", null), "getProperty delegates");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
