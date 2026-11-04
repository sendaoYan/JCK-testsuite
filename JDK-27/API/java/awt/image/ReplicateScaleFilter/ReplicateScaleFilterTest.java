/*
 * @test
 * @summary java.awt.image.ReplicateScaleFilter conformance: white-box structure + black-box behavior.
 * @run main ReplicateScaleFilterTest
 */

import java.awt.image.ReplicateScaleFilter;
import java.awt.image.ImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.awt.image.ImageObserver;
import java.lang.reflect.*;
import java.awt.image.ImageConsumer;
import java.util.Hashtable;

public class ReplicateScaleFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ReplicateScaleFilter", false, ReplicateScaleFilterTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(c.getSuperclass() == ImageFilter.class, "extends ImageFilter");
        check(hasMethod(c, "setDimensions", int.class, int.class), "setDimensions");
        check(hasMethod(c, "setProperties", java.util.Hashtable.class), "setProperties");
        check(hasMethod(c, "setPixels", int.class, int.class, int.class, int.class, java.awt.image.ColorModel.class, int[].class, int.class, int.class), "setPixels int[]");
    }

    static void testBlackBox() throws Exception {
        // Upscale 2x2 -> 4x4: each source pixel replicates into a 2x2 block.
        int[] pix = new int[] { 0xFF111111, 0xFF222222, 0xFF333333, 0xFF444444 };
        MemoryImageSource mis = new MemoryImageSource(2, 2, pix, 0, 2);
        ReplicateScaleFilter rsf = new ReplicateScaleFilter(4, 4);
        FilteredImageSource fis = new FilteredImageSource(mis, rsf);

        int[] out = new int[16];
        PixelGrabber pg = new PixelGrabber(fis, 0, 0, 4, 4, out, 0, 4);
        check(pg.grabPixels(), "grabPixels succeeds");
        check((pg.getStatus() & ImageObserver.ALLBITS) != 0, "status ALLBITS");
        check(pg.getWidth() == 4 && pg.getHeight() == 4, "grabbed 4x4");

        int[] expected = new int[16];
        for (int y = 0; y < 4; y++)
            for (int x = 0; x < 4; x++)
                expected[y * 4 + x] = pix[(y / 2) * 2 + (x / 2)];
        for (int i = 0; i < 16; i++)
            check(out[i] == expected[i], "replicated pixel " + i);

        // 1:1 scale passes through unchanged.
        MemoryImageSource mis2 = new MemoryImageSource(2, 2, pix, 0, 2);
        FilteredImageSource fis2 = new FilteredImageSource(mis2, new ReplicateScaleFilter(2, 2));
        int[] out2 = new int[4];
        PixelGrabber pg2 = new PixelGrabber(fis2, 0, 0, 2, 2, out2, 0, 2);
        check(pg2.grabPixels(), "1:1 grabPixels");
        check(out2[0] == 0xFF111111 && out2[3] == 0xFF444444, "1:1 pass-through");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        passed += ReplicateScaleFilterDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class ReplicateScaleFilterDeepContract {
    // Deepened contract coverage ported from the JDK-8 mirror (JDK-27 divergences adjusted).
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



    static int run() throws Exception {
        testWhiteBox();
        testUpscale();
        testIdentity();
        testDownscale();
        return passed;
    }
}
