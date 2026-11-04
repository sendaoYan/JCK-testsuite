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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
