/*
 * @test
 * @summary java.awt.image.BaseMultiResolutionImage conformance: white-box structure plus black-box behavior.
 * @run main BaseMultiResolutionImageTest
 */

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.lang.reflect.*;

public class BaseMultiResolutionImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.BaseMultiResolutionImage");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(AbstractMultiResolutionImage.class.isAssignableFrom(c), "extends AbstractMultiResolutionImage");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Image small = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Image medium = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Image large = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
        BaseMultiResolutionImage mri = new BaseMultiResolutionImage(small, medium, large);

        java.util.List<Image> variants = mri.getResolutionVariants();
        check(variants.size() == 3, "three variants");
        check(variants.get(0) == small && variants.get(2) == large, "variants preserve order");

        // smallest variant >= requested size wins
        check(mri.getResolutionVariant(15, 15) == medium, "15x15 -> medium(20)");
        check(mri.getResolutionVariant(40, 40) == large, "40x40 -> largest(30)");
        check(mri.getResolutionVariant(5, 5) == small, "5x5 -> small(10)");

        check(mri.getWidth(null) == 10 && mri.getHeight(null) == 10, "default width/height from base image");

        BaseMultiResolutionImage withBase = new BaseMultiResolutionImage(2, small, medium, large);
        check(withBase.getResolutionVariants().size() == 3, "base-index constructor keeps all variants");
        check(withBase.getWidth(null) == 30, "base-index 2 selects large as base");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
