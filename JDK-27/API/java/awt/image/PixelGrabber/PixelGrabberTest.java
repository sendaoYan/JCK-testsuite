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
        passed += PixelGrabberDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class PixelGrabberDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.PixelGrabber");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.ImageConsumer"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "startGrabbing"), "method startGrabbing");
        check(hasMethod(c, "abortGrabbing"), "method abortGrabbing");
        check(hasMethod(c, "grabPixels"), "method grabPixels");
        check(hasMethod(c, "getStatus"), "method getStatus");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getPixels"), "method getPixels");
        check(hasMethod(c, "getColorModel"), "method getColorModel");
        check(hasMethod(c, "setDimensions"), "method setDimensions");
        check(hasMethod(c, "setHints"), "method setHints");
        check(hasMethod(c, "setProperties"), "method setProperties");
        check(hasMethod(c, "setColorModel"), "method setColorModel");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "imageComplete"), "method imageComplete");
        check(hasMethod(c, "status"), "method status");
    }

    static BufferedImage sampleImage() {
        BufferedImage bi = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        bi.setRGB(0, 0, 0xFF000000);
        bi.setRGB(1, 1, 0xFF336699);
        bi.setRGB(2, 0, 0xFFFFFFFF);
        return bi;
    }

    static void testGrabIntPixels() throws Exception {
        BufferedImage bi = sampleImage();
        // forceRGB=true always produces an int[] in the default RGB model.
        PixelGrabber pg = new PixelGrabber(bi, 0, 0, 3, 2, true);
        check(pg.getStatus() == 0, "status is 0 before any grabbing");
        check(pg.getWidth() == 3 && pg.getHeight() == 2, "dims known from constructor");
        check(pg.getPixels() == null, "no pixels before grab");
        boolean ok = pg.grabPixels();
        check(ok, "grabPixels returns true on success");
        int st = pg.getStatus();
        check((st & java.awt.image.ImageObserver.WIDTH) != 0
              && (st & java.awt.image.ImageObserver.HEIGHT) != 0
              && (st & java.awt.image.ImageObserver.SOMEBITS) != 0
              && (st & java.awt.image.ImageObserver.FRAMEBITS) != 0,
              "status reports WIDTH|HEIGHT|SOMEBITS|FRAMEBITS after grab");
        check(pg.getWidth() == 3 && pg.getHeight() == 2, "grabbed dims 3x2");
        Object pixels = pg.getPixels();
        check(pixels instanceof int[], "forceRGB grab yields int[]");
        int[] p = (int[]) pixels;
        check(p.length == 6, "3x2 grab yields 6 pixels");
        check(p[0] == 0xFF000000, "pixel (0,0) exact");
        check(p[4] == 0xFF336699, "pixel (1,1) exact (row-major layout)");
        check(p[2] == 0xFFFFFFFF, "pixel (2,0) exact");
        check(pg.getColorModel() != null, "color model available after grab");
    }

    static void testGrabRegionAndSync() throws Exception {
        BufferedImage bi = sampleImage();
        // A sub-region grab of one pixel.
        PixelGrabber pg = new PixelGrabber(bi, 1, 1, 1, 1, true);
        check(pg.grabPixels(1000L), "timed grabPixels succeeds");
        int[] p = (int[]) pg.getPixels();
        check(p.length == 1 && p[0] == 0xFF336699, "1x1 region grab exact");
        // Timed grab with a negative timeout returns as soon as data arrives.
        PixelGrabber pg2 = new PixelGrabber(bi, 0, 0, 2, 1, true);
        check(pg2.grabPixels(-1), "negative timeout accepted");
        // Asynchronous startGrabbing + join via grabPixels.
        PixelGrabber pg3 = new PixelGrabber(bi, 0, 0, 2, 2, true);
        pg3.startGrabbing();
        check(pg3.grabPixels(500L), "startGrabbing followed by timed grab succeeds");
        check(((int[]) pg3.getPixels())[0] == 0xFF000000, "async grab reads pixels");
        // abortGrabbing after completion only adds the ABORT flag.
        int before = pg3.getStatus();
        pg3.abortGrabbing();
        check(pg3.getStatus() == (before | java.awt.image.ImageObserver.ABORT),
              "abortGrabbing ORs the ABORT flag");
    }

    static void testNonForcedGrab() throws Exception {
        BufferedImage bi = sampleImage();
        // forceRGB=false keeps the image's own (default RGB) model here.
        PixelGrabber pg = new PixelGrabber(bi, 0, 0, 3, 2, false);
        check(pg.grabPixels(), "non-forced grab succeeds");
        check(pg.getPixels() instanceof int[], "int image still yields int[]");
    }

    static void testProducerGrabber() throws Exception {
        // The ImageProducer-based constructor fills a caller-supplied buffer.
        java.awt.image.MemoryImageSource src = new java.awt.image.MemoryImageSource(
                2, 2, new int[] { 0xFF010203, 0xFF040506, 0xFF070809, 0xFF0A0B0C }, 0, 2);
        int[] buf = new int[4];
        PixelGrabber pg = new PixelGrabber(src, 0, 0, 2, 2, buf, 0, 2);
        check(pg.grabPixels(), "producer grab succeeds");
        check(pg.getWidth() == 2 && pg.getHeight() == 2, "producer grab dims");
        check(buf[0] == 0xFF010203 && buf[3] == 0xFF0A0B0C, "producer grab fills buffer exact");
        check(pg.getPixels() == buf, "getPixels returns the caller buffer");
    }

    static void testBufferGrabber() throws Exception {
        // The Image-based buffer constructor honors offset and scansize:
        // buffer index = off + y*scansize + x for image pixel (x, y).
        BufferedImage bi = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        bi.setRGB(2, 1, 0xFFABCDEF);
        int[] buf = new int[8];
        PixelGrabber pg = new PixelGrabber(bi, 0, 0, 3, 2, buf, 1, 3);
        check(pg.grabPixels(), "buffer grab succeeds");
        check(buf[6] == 0xFFABCDEF, "pixel (2,1) lands at off+scansize+2");
        check(buf[0] == 0, "offset bytes before the image stay untouched");
        check(pg.getPixels() == buf, "getPixels exposes the provided buffer");
    }

    static void testStatusAliasAndDefaultCtor() throws Exception {
        BufferedImage bi = sampleImage();
        // forceRGB=false keeps the image's own model.
        PixelGrabber pg = new PixelGrabber(bi, 0, 0, 3, 2, false);
        check(pg.grabPixels(), "default-flag grab succeeds");
        check(pg.getPixels() instanceof int[], "int image yields int[] without forceRGB");
        // status() is the documented alias of getStatus().
        check(pg.status() == pg.getStatus(), "status() alias equals getStatus()");
        check(pg.status() != 0, "status() nonzero after successful grab");
    }



    static int run() throws Exception {
        testWhiteBox();
        testGrabIntPixels();
        testGrabRegionAndSync();
        testNonForcedGrab();
        testProducerGrabber();
        testBufferGrabber();
        testStatusAliasAndDefaultCtor();
        return passed;
    }
}
