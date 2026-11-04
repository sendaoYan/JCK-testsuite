/*
 * @test
 * @summary java.awt.image.AreaAveragingScaleFilter conformance: white-box structure + black-box behavior.
 * @run main AreaAveragingScaleFilterTest
 */

import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.ReplicateScaleFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.awt.image.ImageObserver;
import java.lang.reflect.*;
import java.awt.image.ImageConsumer;
import java.util.Hashtable;

public class AreaAveragingScaleFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.AreaAveragingScaleFilter", false, AreaAveragingScaleFilterTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(c.getSuperclass() == ReplicateScaleFilter.class, "extends ReplicateScaleFilter");
        check(hasMethod(c, "setHints", int.class), "setHints");
        check(hasMethod(c, "setPixels", int.class, int.class, int.class, int.class, java.awt.image.ColorModel.class, int[].class, int.class, int.class), "setPixels int[]");
    }

    static void testBlackBox() throws Exception {
        int A = 0xFF111111, B = 0xFF222222, C = 0xFF333333, D = 0xFF444444;
        int[] pix = new int[] {
            A, A, B, B,
            A, A, B, B,
            C, C, D, D,
            C, C, D, D
        };
        // Downscale 4x4 -> 2x2 averages each constant 2x2 block.
        MemoryImageSource mis = new MemoryImageSource(4, 4, pix, 0, 4);
        FilteredImageSource fis = new FilteredImageSource(mis, new AreaAveragingScaleFilter(2, 2));
        int[] out = new int[4];
        PixelGrabber pg = new PixelGrabber(fis, 0, 0, 2, 2, out, 0, 2);
        check(pg.grabPixels(), "grabPixels succeeds");
        check((pg.getStatus() & ImageObserver.ALLBITS) != 0, "status ALLBITS");
        check(pg.getWidth() == 2 && pg.getHeight() == 2, "grabbed 2x2");
        check(out[0] == A, "block 0 average A");
        check(out[1] == B, "block 1 average B");
        check(out[2] == C, "block 2 average C");
        check(out[3] == D, "block 3 average D");

        // 1:1 scale preserves pixels exactly.
        int[] single = new int[] { 0xFF123456, 0xFF654321, 0xFFAABBCC, 0xFFDDEEFF };
        MemoryImageSource mis2 = new MemoryImageSource(2, 2, single, 0, 2);
        FilteredImageSource fis2 = new FilteredImageSource(mis2, new AreaAveragingScaleFilter(2, 2));
        int[] out2 = new int[4];
        PixelGrabber pg2 = new PixelGrabber(fis2, 0, 0, 2, 2, out2, 0, 2);
        check(pg2.grabPixels(), "1:1 grabPixels");
        check(out2[0] == 0xFF123456 && out2[3] == 0xFFDDEEFF, "1:1 preserved");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        passed += AreaAveragingScaleFilterDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class AreaAveragingScaleFilterDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.AreaAveragingScaleFilter");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.ReplicateScaleFilter"), "superclass java.awt.image.ReplicateScaleFilter");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setHints"), "method setHints");
        check(hasMethod(c, "setPixels"), "method setPixels");
    }

    static class RecordingConsumer implements ImageConsumer {
        int w = -1, h = -1, pixels, first = -1;
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

    static void testUniformAverage() {
        // Averaging a uniform image must reproduce the exact color (no drift).
        int[] uni = new int[16];
        java.util.Arrays.fill(uni, 0xFF204060);
        MemoryImageSource src = new MemoryImageSource(4, 4, uni, 0, 4);
        FilteredImageSource fis = new FilteredImageSource(src, new AreaAveragingScaleFilter(2, 2));
        RecordingConsumer rec = new RecordingConsumer();
        fis.startProduction(rec);
        check(rec.w == 2 && rec.h == 2, "target dims 2x2");
        check(rec.pixels == 4, "4 pixels delivered");
        check(rec.first == 0xFF204060, "uniform color averaged exactly");
    }

    static void testAveragedMix() {
        // A 2x1 image of black and white averaged into 1x1 gives a mid gray.
        int[] bw = { 0xFF000000, 0xFFFFFFFF };
        MemoryImageSource src = new MemoryImageSource(2, 1, bw, 0, 2);
        FilteredImageSource fis = new FilteredImageSource(src, new AreaAveragingScaleFilter(1, 1));
        RecordingConsumer rec = new RecordingConsumer();
        fis.startProduction(rec);
        check(rec.w == 1 && rec.h == 1 && rec.pixels == 1, "single averaged pixel");
        int rgb = rec.first;
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        check(r == 127 || r == 128, "average of 0 and 255 lands on 127/128, got " + r);
        check(r == g && g == b, "gray average keeps channels equal");
        check(((rgb >> 24) & 0xFF) == 255, "alpha stays opaque");
    }

    static void testIdentity() {
        int[] pix = { 0xFF112233, 0xFF445566, 0xFF778899, 0xFFAABBCC };
        MemoryImageSource src = new MemoryImageSource(2, 2, pix, 0, 2);
        FilteredImageSource fis = new FilteredImageSource(src, new AreaAveragingScaleFilter(2, 2));
        RecordingConsumer rec = new RecordingConsumer();
        fis.startProduction(rec);
        check(rec.w == 2 && rec.h == 2 && rec.pixels == 4, "identity keeps geometry");
        check(rec.first == 0xFF112233, "identity keeps pixel values");
    }



    static int run() throws Exception {
        testWhiteBox();
        testUniformAverage();
        testAveragedMix();
        testIdentity();
        return passed;
    }
}
