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
    interface Act { void run() throws Exception; }
    static void expectExact(Class<? extends Throwable> expected, Act a, String msg) {
        try {
            a.run();
            check(false, msg + ": expected " + expected.getSimpleName() + " but nothing thrown");
        } catch (Throwable t) {
            check(t.getClass() == expected,
                  msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName());
        }
    }
    static javax.imageio.metadata.IIOMetadata pngStreamMetadata() {
        javax.imageio.ImageWriter w = javax.imageio.ImageIO.getImageWritersByFormatName("png").next();
        javax.imageio.metadata.IIOMetadata m =
            w.getDefaultStreamMetadata(w.getDefaultWriteParam());
        w.dispose();
        return m;
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

    // ---- Part 3 (deep): thumbnail list is a live reference, null stays null ----
    static void testThumbnailListSemantics() {
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        // empty (non-null) list is kept as-is: count 0 but reference preserved
        List<BufferedImage> empty = new ArrayList<BufferedImage>();
        javax.imageio.IIOImage withEmpty = new javax.imageio.IIOImage(img, empty, null);
        check(withEmpty.getNumThumbnails() == 0, "empty list -> 0 thumbnails");
        check(withEmpty.getThumbnails() == empty, "empty list stored by reference");
        // the stored list is live: external mutation is visible (probed)
        BufferedImage t0 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        BufferedImage t1 = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
        List<BufferedImage> live = new ArrayList<BufferedImage>();
        live.add(t0);
        javax.imageio.IIOImage liv = new javax.imageio.IIOImage(img, live, null);
        check(liv.getNumThumbnails() == 1, "one thumbnail initially");
        live.add(t1);
        check(liv.getNumThumbnails() == 2, "external add is visible through getNumThumbnails (live list)");
        check(liv.getThumbnail(1) == t1, "getThumbnail(1) sees externally added element");
        live.remove(0);
        check(liv.getNumThumbnails() == 1 && liv.getThumbnail(0) == t1, "external remove shifts indices");
        // precise exception classes (probed on this JDK)
        expectExact(IndexOutOfBoundsException.class, () -> liv.getThumbnail(1),
                    "getThumbnail(size) precise class");
        expectExact(IndexOutOfBoundsException.class, () -> liv.getThumbnail(-1),
                    "getThumbnail(-1) precise class");
        // null list: "No thumbnails available!" (probed)
        javax.imageio.IIOImage nullList = new javax.imageio.IIOImage(img, null, null);
        check(nullList.getThumbnails() == null, "null thumbnails stay null (not converted to a list)");
        try {
            nullList.getThumbnail(0);
            check(false, "getThumbnail on null list should throw");
        } catch (IndexOutOfBoundsException e) {
            check("No thumbnails available!".equals(e.getMessage()), "null-list getThumbnail message");
        }
        // setThumbnails with a live list keeps reference semantics too
        List<BufferedImage> fresh = new ArrayList<BufferedImage>();
        nullList.setThumbnails(fresh);
        check(nullList.getThumbnails() == fresh, "setThumbnails stores live reference");
        fresh.add(t0);
        check(nullList.getNumThumbnails() == 1, "setThumbnails list is live as well");
    }

    // ---- Part 3: raster constructor carries thumbnails and metadata ----
    static void testRasterConstructorState() {
        BufferedImage img = new BufferedImage(3, 2, BufferedImage.TYPE_INT_ARGB);
        Raster ras = img.getData();
        List<BufferedImage> thumbs = new ArrayList<BufferedImage>();
        thumbs.add(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
        thumbs.add(new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB));
        javax.imageio.metadata.IIOMetadata md = pngStreamMetadata();
        javax.imageio.IIOImage rasImg = new javax.imageio.IIOImage(ras, thumbs, md);
        check(rasImg.getRaster() == ras, "raster ctor keeps raster identity");
        check(rasImg.getThumbnails() == thumbs, "raster ctor keeps thumbnails reference");
        check(rasImg.getNumThumbnails() == 2, "raster ctor thumbnail count");
        check(rasImg.getThumbnail(1) == thumbs.get(1), "raster ctor getThumbnail(1)");
        check(rasImg.getMetadata() == md, "raster ctor keeps metadata reference");
        // metadata setter round-trip on the same instance
        javax.imageio.metadata.IIOMetadata md2 = pngStreamMetadata();
        rasImg.setMetadata(md2);
        check(rasImg.getMetadata() == md2, "setMetadata replaces reference");
        rasImg.setMetadata(null);
        check(rasImg.getMetadata() == null, "setMetadata(null) tolerated");
        // switching to an image keeps thumbnails and metadata untouched
        rasImg.setRenderedImage(img);
        check(rasImg.getNumThumbnails() == 2 && rasImg.getMetadata() == md2,
              "setRenderedImage leaves thumbnails/metadata intact");
        expectExact(IllegalArgumentException.class, () -> rasImg.setRaster(null),
                    "setRaster(null) precise");
        expectExact(IllegalArgumentException.class, () -> rasImg.setRenderedImage(null),
                    "setRenderedImage(null) precise");
        expectExact(IllegalArgumentException.class,
                    () -> new javax.imageio.IIOImage((RenderedImage) null, thumbs, null),
                    "image ctor null precise");
        expectExact(IllegalArgumentException.class,
                    () -> new javax.imageio.IIOImage((Raster) null, thumbs, null),
                    "raster ctor null precise");
    }

    // ---- Part 3: object identity contracts ----
    static void testMiscContracts() {
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        javax.imageio.IIOImage a = new javax.imageio.IIOImage(img, null, null);
        javax.imageio.IIOImage b = new javax.imageio.IIOImage(img, null, null);
        check(a.equals(b) == false, "equals is not overridden (identity semantics)");
        check(a.equals(a), "equals self");
        check(!java.io.Serializable.class.isAssignableFrom(javax.imageio.IIOImage.class),
              "IIOImage is not Serializable");
        // independent instances do not share state
        List<BufferedImage> la = new ArrayList<BufferedImage>();
        a.setThumbnails(la);
        check(b.getNumThumbnails() == 0, "instance b unaffected by a's setThumbnails");
        check(b.getThumbnails() == null, "instance b thumbnails still null");
        // hasRaster is a view on which slot is filled, not a sticky flag
        javax.imageio.IIOImage c = new javax.imageio.IIOImage(img, null, null);
        check(!c.hasRaster(), "image-only instance has no raster");
        c.setRaster(img.getData());
        check(c.hasRaster(), "after setRaster hasRaster true");
        c.setRenderedImage(img);
        check(!c.hasRaster(), "switching back to image clears raster flag");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testThumbnailListSemantics();
        testRasterConstructorState();
        testMiscContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
