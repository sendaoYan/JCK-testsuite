/*
 * @test
 * @summary java.awt.image.CropImageFilter conformance: white-box structure + black-box behavior.
 * @run main CropImageFilterTest
 */

import java.awt.image.CropImageFilter;
import java.awt.image.ImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.awt.image.ImageObserver;
import java.lang.reflect.*;

public class CropImageFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.CropImageFilter", false, CropImageFilterTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(c.getSuperclass() == ImageFilter.class, "extends ImageFilter");
        check(hasMethod(c, "setDimensions", int.class, int.class), "setDimensions");
        check(hasMethod(c, "setProperties", java.util.Hashtable.class), "setProperties");
        check(hasMethod(c, "setPixels", int.class, int.class, int.class, int.class, java.awt.image.ColorModel.class, int[].class, int.class, int.class), "setPixels int[]");
    }

    static void testBlackBox() throws Exception {
        int w = 4, h = 4;
        int[] pix = new int[w * h];
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                pix[y * w + x] = 0xFF000000 | (x << 16) | (y << 8) | 0x33;

        // Manual drive: crop rect (1,1,2,2) reports dimensions (2,2).
        CropImageFilter cf = new CropImageFilter(1, 1, 2, 2);
        int[] dims = new int[2];
        java.awt.image.ImageConsumer rc = new java.awt.image.ImageConsumer() {
            public void setDimensions(int width, int height) { dims[0] = width; dims[1] = height; }
            public void setProperties(java.util.Hashtable<?, ?> props) { }
            public void setColorModel(java.awt.image.ColorModel model) { }
            public void setHints(int hintflags) { }
            public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel model, byte[] pixels, int off, int scansize) { }
            public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel model, int[] pixels, int off, int scansize) { }
            public void imageComplete(int status) { }
        };
        cf.getFilterInstance(rc).setDimensions(w, h);
        check(dims[0] == 2 && dims[1] == 2, "crop setDimensions reports 2x2");

        // Full chain: MemoryImageSource -> FilteredImageSource(crop) -> PixelGrabber.
        MemoryImageSource mis = new MemoryImageSource(w, h, pix, 0, w);
        FilteredImageSource fis = new FilteredImageSource(mis, new CropImageFilter(1, 1, 2, 2));
        int[] out = new int[4];
        PixelGrabber pg = new PixelGrabber(fis, 0, 0, 2, 2, out, 0, 2);
        boolean ok = pg.grabPixels();
        check(ok, "grabPixels succeeds");
        check((pg.getStatus() & ImageObserver.ALLBITS) != 0, "status ALLBITS");
        check(pg.getWidth() == 2 && pg.getHeight() == 2, "grabbed 2x2");
        check(out[0] == pix[1 * w + 1], "crop top-left");
        check(out[1] == pix[1 * w + 2], "crop top-right");
        check(out[2] == pix[2 * w + 1], "crop bottom-left");
        check(out[3] == pix[2 * w + 2], "crop bottom-right");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
