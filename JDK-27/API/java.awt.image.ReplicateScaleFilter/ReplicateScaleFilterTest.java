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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
