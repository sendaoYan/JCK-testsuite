/*
 * @test
 * @summary java.awt.image.RGBImageFilter conformance: white-box structure + black-box behavior.
 * @run main RGBImageFilterTest
 */

import java.awt.image.RGBImageFilter;
import java.awt.image.ImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.lang.reflect.*;

public class RGBImageFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.RGBImageFilter", false, RGBImageFilterTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isAbstract(m), "abstract class");
        check(c.getSuperclass() == ImageFilter.class, "extends ImageFilter");
        check(hasMethod(c, "filterRGB", int.class, int.class, int.class), "filterRGB abstract");
        check(hasMethod(c, "setColorModel", java.awt.image.ColorModel.class), "setColorModel");
        check(hasMethod(c, "substituteColorModel", java.awt.image.ColorModel.class, java.awt.image.ColorModel.class), "substituteColorModel");
        check(hasMethod(c, "filterIndexColorModel", IndexColorModel.class), "filterIndexColorModel");
        check(hasMethod(c, "filterRGBPixels", int.class, int.class, int.class, int.class, int[].class, int.class, int.class), "filterRGBPixels");
    }

    static class OpaqueFilter extends RGBImageFilter {
        public int filterRGB(int x, int y, int rgb) {
            return rgb | 0xFF000000;
        }
    }

    static class ConstantFilter extends RGBImageFilter {
        public int filterRGB(int x, int y, int rgb) {
            return 0xFF00FF00;
        }
    }

    static void testBlackBox() throws Exception {
        // Alpha-forcing filter is a real observable transform.
        int[] pix = new int[] { 0x80112233, 0xFF445566, 0x40000000, 0xFF778899 };
        MemoryImageSource mis = new MemoryImageSource(2, 2, pix, 0, 2);
        FilteredImageSource fis = new FilteredImageSource(mis, new OpaqueFilter());
        int[] out = new int[4];
        PixelGrabber pg = new PixelGrabber(fis, 0, 0, 2, 2, out, 0, 2);
        check(pg.grabPixels(), "grabPixels succeeds");
        check((pg.getStatus() & ImageObserver.ALLBITS) != 0, "status ALLBITS");
        check((out[0] & 0xFF000000) == 0xFF000000, "semi-transparent forced opaque");
        check(out[0] == 0xFF112233, "rgb preserved after forcing alpha");
        check(out[1] == 0xFF445566, "opaque preserved");

        // Constant filter proves per-pixel filtering.
        MemoryImageSource mis2 = new MemoryImageSource(2, 2, pix, 0, 2);
        FilteredImageSource fis2 = new FilteredImageSource(mis2, new ConstantFilter());
        int[] out2 = new int[4];
        PixelGrabber pg2 = new PixelGrabber(fis2, 0, 0, 2, 2, out2, 0, 2);
        check(pg2.grabPixels(), "constant grabPixels");
        check(out2[0] == 0xFF00FF00 && out2[3] == 0xFF00FF00, "constant filter green");

        // filterIndexColorModel produces a new IndexColorModel.
        byte[] r = new byte[] { 0x10, 0x20 };
        byte[] g = new byte[] { 0x30, 0x40 };
        byte[] b = new byte[] { 0x50, 0x60 };
        IndexColorModel icm = new IndexColorModel(8, 2, r, g, b);
        RGBImageFilter f = new OpaqueFilter();
        IndexColorModel filtered = f.filterIndexColorModel(icm);
        check(filtered != null && filtered != icm, "filterIndexColorModel returns new model");
        check(filtered.getMapSize() == 2, "filtered map size 2");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
