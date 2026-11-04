/*
 * @test
 * @summary java.awt.image.ReplicateScaleFilter conformance: class reflection + scaling contract.
 * @run main ReplicateScaleFilterTest
 */

import java.awt.image.FilteredImageSource;
import java.awt.image.ImageConsumer;
import java.awt.image.MemoryImageSource;
import java.awt.image.ReplicateScaleFilter;
import java.util.Hashtable;

public class ReplicateScaleFilterTest {
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
        Class<?> c = Class.forName("java.awt.image.ReplicateScaleFilter");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.ImageFilter"), "superclass java.awt.image.ImageFilter");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setProperties"), "method setProperties");
        check(hasMethod(c, "setDimensions"), "method setDimensions");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(java.lang.reflect.Modifier.isPublic(c.getConstructor(int.class, int.class).getModifiers()),
              "public constructor (destWidth, destHeight)");
    }

    static class RecordingConsumer implements ImageConsumer {
        int w = -1, h = -1, pixels;
        int[] first8 = new int[8];
        public void setDimensions(int w, int h) { this.w = w; this.h = h; }
        public void setProperties(Hashtable<?,?> p) { }
        public void setColorModel(java.awt.image.ColorModel m) { }
        public void setHints(int h) { }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel m,
                              byte[] pix, int off, int scansize) { }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel m,
                              int[] pix, int off, int scansize) {
            if (pixels == 0) System.arraycopy(pix, off, first8, 0, Math.min(8, w));
            pixels += w * h;
        }
        public void imageComplete(int status) { }
    }

    static int[] sourcePixels() {
        return new int[] { 0xFF100000, 0xFF200000, 0xFF300000, 0xFF400000,
                           0xFF500000, 0xFF600000, 0xFF700000, 0xFF800000 };
    }

    static RecordingConsumer scale(int sw, int sh, int dw, int dh) {
        MemoryImageSource src = new MemoryImageSource(sw, sh, sourcePixels(), 0, sw);
        FilteredImageSource fis = new FilteredImageSource(src, new ReplicateScaleFilter(dw, dh));
        RecordingConsumer rec = new RecordingConsumer();
        fis.startProduction(rec);
        return rec;
    }

    static void testUpscale() {
        // Doubling 4x2 -> 8x4 replicates each pixel into a 2x2 block.
        RecordingConsumer rec = scale(4, 2, 8, 4);
        check(rec.w == 8 && rec.h == 4, "target dims 8x4");
        check(rec.pixels == 32, "32 pixels delivered");
        check(rec.first8[0] == 0xFF100000 && rec.first8[1] == 0xFF100000,
              "pixel replicated horizontally");
        check(rec.first8[2] == 0xFF200000 && rec.first8[3] == 0xFF200000,
              "next source pixel replicated");
    }

    static void testIdentity() {
        RecordingConsumer rec = scale(4, 2, 4, 2);
        check(rec.w == 4 && rec.h == 2 && rec.pixels == 8, "identity scale keeps geometry");
        check(rec.first8[0] == 0xFF100000, "identity scale keeps pixels");
    }

    static void testDownscale() {
        // Nearest-neighbor downscaling samples source pixel centers:
        // dest x=0,y=0 maps to source (0,1) for 4x2 -> 2x1.
        RecordingConsumer rec = scale(4, 2, 2, 1);
        check(rec.w == 2 && rec.h == 1 && rec.pixels == 2, "downscale dims/pixels");
        check(rec.first8[0] == 0xFF600000, "first sample maps to source (0,1)");
        check(rec.first8[1] == 0xFF800000, "second sample maps to source (3,1)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testUpscale();
        testIdentity();
        testDownscale();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
