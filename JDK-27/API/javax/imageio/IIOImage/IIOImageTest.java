/*
 * @test
 * @summary javax.imageio.IIOImage conformance: white-box structure + black-box behavior.
 * @run main IIOImageTest
 */

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class IIOImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.IIOImage");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getConstructors().length == 2, "two constructors");
        // protected fields
        check(c.getDeclaredField("image").getType().getName().equals("java.awt.image.RenderedImage"), "field image");
        check(c.getDeclaredField("raster").getType().getName().equals("java.awt.image.Raster"), "field raster");
        check(c.getDeclaredField("thumbnails").getType().getName().equals("java.util.List"), "field thumbnails");
        check(c.getDeclaredField("metadata").getType().getName().equals("javax.imageio.metadata.IIOMetadata"), "field metadata");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);

        // RenderedImage constructor: image present, raster absent, thumbnails/metadata null
        javax.imageio.IIOImage iio = new javax.imageio.IIOImage(img, null, null);
        check(iio.getRenderedImage() == img, "getRenderedImage identity");
        check(!iio.hasRaster(), "hasRaster false for image ctor");
        check(iio.getRaster() == null, "getRaster null for image ctor");
        check(iio.getNumThumbnails() == 0, "null thumbnails -> 0 count");
        check(iio.getThumbnails() == null, "getThumbnails null");
        check(iio.getMetadata() == null, "getMetadata null");

        // Raster constructor
        Raster ras = img.getRaster();
        javax.imageio.IIOImage iioRas = new javax.imageio.IIOImage(ras, null, null);
        check(iioRas.hasRaster(), "hasRaster true for raster ctor");
        check(iioRas.getRaster() == ras, "getRaster identity");
        check(iioRas.getRenderedImage() == null, "getRenderedImage null for raster ctor");

        // setRenderedImage discards raster; setRaster discards image
        iioRas.setRenderedImage(img);
        check(iioRas.getRenderedImage() == img, "setRenderedImage sets image");
        check(!iioRas.hasRaster(), "setRenderedImage discards raster");
        iioRas.setRaster(ras);
        check(iioRas.hasRaster(), "setRaster sets raster");
        check(iioRas.getRenderedImage() == null, "setRaster discards image");

        // thumbnail list composition
        BufferedImage t0 = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        BufferedImage t1 = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
        List<BufferedImage> thumbs = new ArrayList<BufferedImage>();
        thumbs.add(t0);
        thumbs.add(t1);
        iio.setThumbnails(thumbs);
        check(iio.getThumbnails() == thumbs, "setThumbnails stores live reference");
        check(iio.getNumThumbnails() == 2, "two thumbnails");
        check(iio.getThumbnail(0) == t0, "getThumbnail 0");
        check(iio.getThumbnail(1) == t1, "getThumbnail 1");
        iio.setThumbnails(null);
        check(iio.getNumThumbnails() == 0, "setThumbnails null clears");

        // metadata round-trip
        check(iio.getMetadata() == null, "metadata initially null");
        iio.setMetadata(null);
        check(iio.getMetadata() == null, "setMetadata null");

        // exceptions
        expectThrows(IllegalArgumentException.class,
                     () -> new javax.imageio.IIOImage((RenderedImage) null, null, null),
                     "image ctor null");
        expectThrows(IllegalArgumentException.class,
                     () -> new javax.imageio.IIOImage((Raster) null, null, null),
                     "raster ctor null");
        expectThrows(IllegalArgumentException.class, () -> iio.setRenderedImage(null), "setRenderedImage null");
        expectThrows(IllegalArgumentException.class, () -> iio.setRaster(null), "setRaster null");
        expectThrows(IndexOutOfBoundsException.class, () -> iio.getThumbnail(0), "getThumbnail with null list");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
