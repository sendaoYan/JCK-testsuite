/*
 * @test
 * @summary java.awt.image.PixelGrabber conformance: white-box structure + black-box behavior.
 * @run main PixelGrabberTest
 */

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.lang.reflect.*;

public class PixelGrabberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.PixelGrabber", false, PixelGrabberTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(ImageConsumer.class.isAssignableFrom(c), "implements ImageConsumer");
        check(hasMethod(c, "grabPixels"), "grabPixels()");
        check(hasMethod(c, "grabPixels", long.class), "grabPixels(long)");
        check(hasMethod(c, "getStatus"), "getStatus()");
        check(hasMethod(c, "getWidth"), "getWidth()");
        check(hasMethod(c, "getHeight"), "getHeight()");
        check(hasMethod(c, "getPixels"), "getPixels()");
        check(hasMethod(c, "getColorModel"), "getColorModel()");
        check(hasMethod(c, "status"), "status()");
        check(hasMethod(c, "abortGrabbing"), "abortGrabbing()");
        check(hasMethod(c, "startGrabbing"), "startGrabbing()");
    }

    static void testBlackBox() throws Exception {
        BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                img.setRGB(x, y, 0xFF000000 | (x << 16) | (y << 8) | 0x33);

        int[] out = new int[9];
        PixelGrabber pg = new PixelGrabber(img, 0, 0, 3, 3, out, 0, 3);
        boolean ok = pg.grabPixels();
        check(ok, "grabPixels succeeds");
        check((pg.getStatus() & (ImageObserver.ALLBITS | ImageObserver.FRAMEBITS)) != 0, "status has ALLBITS or FRAMEBITS");
        check(pg.getWidth() == 3 && pg.getHeight() == 3, "dimensions 3x3");
        check(out[0] == img.getRGB(0, 0), "grabbed (0,0)");
        check(out[4] == img.getRGB(1, 1), "grabbed (1,1)");
        check(out[8] == img.getRGB(2, 2), "grabbed (2,2)");
        check(pg.getColorModel() != null, "color model non-null");
        check(pg.status() == pg.getStatus(), "status() == getStatus()");

        // getPixels returns the same int array when forceRGB not set (int model).
        Object px = pg.getPixels();
        check(px instanceof int[], "getPixels int[]");

        // Sub-region grab via ImageProducer.
        int[] src = new int[] { 0xFF101010, 0xFF202020, 0xFF303030, 0xFF404040 };
        MemoryImageSource mis = new MemoryImageSource(2, 2, src, 0, 2);
        int[] sub = new int[4];
        PixelGrabber pg2 = new PixelGrabber(mis, 0, 0, 2, 2, sub, 0, 2);
        check(pg2.grabPixels(), "producer grabPixels");
        check(sub[0] == 0xFF101010 && sub[3] == 0xFF404040, "producer grabbed pixels");

        // forceRGB constructor grabs a sub-rectangle.
        PixelGrabber pg3 = new PixelGrabber(img, 1, 1, 2, 2, true);
        check(pg3.grabPixels(), "forceRGB grabPixels");
        check(pg3.getWidth() == 2 && pg3.getHeight() == 2, "forceRGB dims 2x2");
        Object p3 = pg3.getPixels();
        check(p3 instanceof int[], "forceRGB getPixels int[]");

        // timed grabPixels.
        int[] t = new int[9];
        PixelGrabber pg4 = new PixelGrabber(img, 0, 0, 3, 3, t, 0, 3);
        check(pg4.grabPixels(2000), "grabPixels(timeout) succeeds");

        // abortGrabbing is callable after a finished grab.
        pg4.abortGrabbing();
        check(true, "abortGrabbing callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
