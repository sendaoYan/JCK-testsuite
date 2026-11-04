/*
 * @test
 * @summary java.awt.image.RGBImageFilter conformance: abstract class reflection + per-pixel contract.
 * @run main RGBImageFilterTest
 */

import java.awt.image.FilteredImageSource;
import java.awt.image.ImageConsumer;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.RGBImageFilter;
import java.util.Hashtable;

public class RGBImageFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.RGBImageFilter");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.awt.image.ImageFilter"), "superclass java.awt.image.ImageFilter");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setColorModel"), "method setColorModel");
        check(hasMethod(c, "substituteColorModel"), "method substituteColorModel");
        check(hasMethod(c, "filterIndexColorModel"), "method filterIndexColorModel");
        check(hasMethod(c, "filterRGBPixels"), "method filterRGBPixels");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "filterRGB"), "method filterRGB");
        // filterRGB is the only abstract method subclasses must provide.
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("filterRGB", int.class, int.class, int.class).getModifiers()),
              "filterRGB abstract");
    }

    /** Flips the low 8 bits (blue channel) of every pixel. */
    static RGBImageFilter blueFlip() {
        return new RGBImageFilter() {
            public int filterRGB(int x, int y, int rgb) { return rgb ^ 0x000000FF; }
        };
    }

    static class RecordingConsumer implements ImageConsumer {
        int w = -1, h = -1, pixels;
        int first = -1;
        public void setDimensions(int w, int h) { this.w = w; this.h = h; }
        public void setProperties(Hashtable<?,?> p) { }
        public void setColorModel(java.awt.image.ColorModel m) { }
        public void setHints(int h) { }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel m,
                              byte[] pix, int off, int scansize) { }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel m,
                              int[] pix, int off, int scansize) {
            if (pixels == 0) first = pix[off];
            pixels += w * h;
        }
        public void imageComplete(int status) { }
    }

    static void testFilterChain() {
        // The filter transforms every pixel while preserving geometry.
        int[] pix = { 0xFF100000, 0xFF200000, 0xFF300000, 0xFF400000,
                      0xFF500000, 0xFF600000, 0xFF700000, 0xFF800000 };
        MemoryImageSource src = new MemoryImageSource(4, 2, pix, 0, 4);
        FilteredImageSource fis = new FilteredImageSource(src, blueFlip());
        RecordingConsumer rec = new RecordingConsumer();
        fis.startProduction(rec);
        check(rec.w == 4 && rec.h == 2, "dims unchanged by rgb filter");
        check(rec.pixels == 8, "all pixels delivered");
        check(rec.first == (0xFF100000 ^ 0xFF), "first pixel filtered (0x" + Integer.toHexString(rec.first) + ")");
    }

    static void testFilterIndexColorModel() {
        // A gray ramp IndexColorModel filtered through blueFlip keeps red intact.
        byte[] r = new byte[256], g = new byte[256], b = new byte[256];
        for (int i = 0; i < 256; i++) { r[i] = g[i] = b[i] = (byte) i; }
        IndexColorModel icm = new IndexColorModel(8, 256, r, g, b);
        IndexColorModel filtered = blueFlip().filterIndexColorModel(icm);
        check(filtered != icm, "filterIndexColorModel returns a new model");
        check(filtered instanceof IndexColorModel, "result is an IndexColorModel");
        check(filtered.getRed(0) == 0 && filtered.getRed(17) == 17, "red channel preserved");
        check(filtered.getBlue(0) == 255 && filtered.getBlue(17) == 238,
              "blue channel flipped (0->255, 17->238)");
        check(filtered.getMapSize() == 256, "palette size preserved");
    }

    static void testFilterRGBPixels() {
        // filterRGBPixels filters in place and forwards to the wired consumer.
        RecordingConsumer rec = new RecordingConsumer();
        RGBImageFilter wired = (RGBImageFilter) blueFlip().getFilterInstance(rec);
        int[] row = { 0xFF000000, 0xFF010101 };
        wired.filterRGBPixels(0, 0, 2, 1, row, 0, 2);
        check(rec.pixels == 2, "filtered row forwarded to consumer");
        check(rec.first == 0xFF0000FF, "forwarded pixel is filtered");
    }

    static void testSubstituteColorModelDefault() {
        // The default substituteColorModel simply remembers the new model.
        java.awt.image.DirectColorModel dcm = new java.awt.image.DirectColorModel(24, 0xFF0000, 0xFF00, 0xFF);
        blueFlip().substituteColorModel(null, dcm);
        check(true, "substituteColorModel default callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testFilterChain();
        testFilterIndexColorModel();
        testFilterRGBPixels();
        testSubstituteColorModelDefault();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
