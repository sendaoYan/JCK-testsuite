/*
 * @test
 * @summary java.awt.image.BufferedImage conformance: white-box structure + black-box behavior + contract.
 * @run main BufferedImageTest
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;
import java.awt.image.ImageProducer;
import java.lang.reflect.Modifier;

public class BufferedImageTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean throwsEx(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) { return ex.isAssignableFrom(t.getClass()); }
        return false;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.BufferedImage", false, BufferedImageTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!c.isInterface() && !c.isEnum(), "concrete class");
        check(c.getSuperclass() == java.awt.Image.class, "extends java.awt.Image");
        check(WritableRenderedImage.class.isAssignableFrom(c), "implements WritableRenderedImage");
        check(Transparency.class.isAssignableFrom(c), "implements Transparency");
        for (String n : new String[]{"TYPE_INT_ARGB","TYPE_INT_RGB","TYPE_BYTE_GRAY","TYPE_BYTE_BINARY","TYPE_BYTE_INDEXED","TYPE_CUSTOM"})
            try { c.getField(n); check(true, "has constant " + n); } catch (NoSuchFieldException e) { check(false, "has constant " + n); }
        check(hasMethod(c, "getRGB", int.class, int.class), "getRGB(int,int)");
        check(hasMethod(c, "setRGB", int.class, int.class, int.class), "setRGB(int,int,int)");
        check(hasMethod(c, "getRGB", int.class, int.class, int.class, int.class, int[].class, int.class, int.class), "getRGB batch");
        check(hasMethod(c, "setRGB", int.class, int.class, int.class, int.class, int[].class, int.class, int.class), "setRGB batch");
        check(hasMethod(c, "getRaster"), "getRaster()");
        check(hasMethod(c, "getColorModel"), "getColorModel()");
        check(hasMethod(c, "getSubimage", int.class, int.class, int.class, int.class), "getSubimage");
        check(hasMethod(c, "createGraphics"), "createGraphics()");
        check(hasMethod(c, "flush"), "flush()");
        check(hasMethod(c, "isAlphaPremultiplied"), "isAlphaPremultiplied()");
        check(hasMethod(c, "coerceData", boolean.class), "coerceData(boolean)");
    }

    static void testConstructorTypes() {
        // All 13 predefined image types are constructible and report the right type.
        int[] types = { BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_ARGB_PRE,
            BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR,
            BufferedImage.TYPE_4BYTE_ABGR_PRE, BufferedImage.TYPE_USHORT_565_RGB, BufferedImage.TYPE_USHORT_555_RGB,
            BufferedImage.TYPE_BYTE_GRAY, BufferedImage.TYPE_USHORT_GRAY, BufferedImage.TYPE_BYTE_BINARY,
            BufferedImage.TYPE_BYTE_INDEXED };
        check(types.length == 13, "13 predefined image types");
        for (int t : types) {
            BufferedImage im = new BufferedImage(3, 4, t);
            check(im.getWidth() == 3 && im.getHeight() == 4, "dims for type " + t);
            check(im.getType() == t, "getType()==" + t);
            check(im.getColorModel() != null, "color model non-null for type " + t);
            check(im.getRaster() != null, "raster non-null for type " + t);
        }
    }

    static void testConstructorErrors() {
        check(throwsEx(IllegalArgumentException.class, () -> new BufferedImage(0, 5, BufferedImage.TYPE_INT_ARGB)), "width 0 -> IAE");
        check(throwsEx(IllegalArgumentException.class, () -> new BufferedImage(-1, 5, BufferedImage.TYPE_INT_ARGB)), "width -1 -> IAE");
        check(throwsEx(IllegalArgumentException.class, () -> new BufferedImage(5, 0, BufferedImage.TYPE_INT_ARGB)), "height 0 -> IAE");
        check(throwsEx(IllegalArgumentException.class, () -> new BufferedImage(5, -2, BufferedImage.TYPE_INT_ARGB)), "height -2 -> IAE");
        check(throwsEx(IllegalArgumentException.class, () -> new BufferedImage(5, 5, 999)), "unknown type 999 -> IAE");
        check(throwsEx(IllegalArgumentException.class, () -> new BufferedImage(5, 5, -1)), "unknown type -1 -> IAE");
        check(throwsEx(IllegalArgumentException.class, () -> new BufferedImage(5, 5, BufferedImage.TYPE_CUSTOM)), "TYPE_CUSTOM(0) -> IAE");

        IndexColorModel cm = new IndexColorModel(8, 2, new byte[]{0, 0}, new byte[]{0, 0}, new byte[]{0, 0});
        // The (w,h,type,IndexColorModel) constructor only accepts BYTE_BINARY / BYTE_INDEXED.
        check(throwsEx(IllegalArgumentException.class, () -> new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB, cm)), "indexed ctor with INT_ARGB -> IAE");
        check(!throwsEx(Throwable.class, () -> new BufferedImage(5, 5, BufferedImage.TYPE_BYTE_INDEXED, cm)), "BYTE_INDEXED + cm ok");
        check(!throwsEx(Throwable.class, () -> new BufferedImage(5, 5, BufferedImage.TYPE_BYTE_BINARY, cm)), "BYTE_BINARY + cm ok");
        check(throwsEx(NullPointerException.class, () -> new BufferedImage(5, 5, BufferedImage.TYPE_BYTE_INDEXED, (IndexColorModel) null)), "BYTE_INDEXED null cm -> NPE");

        BufferedImage src = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        check(!throwsEx(Throwable.class, () -> new BufferedImage(src.getColorModel(), src.getRaster(), true, null)), "cm+raster ctor ok");
        check(throwsEx(NullPointerException.class, () -> new BufferedImage(null, src.getRaster(), false, null)), "null cm -> NPE");
    }

    static void testPixelRoundTrip() {
        BufferedImage img = new BufferedImage(10, 8, BufferedImage.TYPE_INT_ARGB);
        check(img.getMinX() == 0 && img.getMinY() == 0, "min x/y 0");
        check(img.getType() == BufferedImage.TYPE_INT_ARGB, "type ARGB");
        check(img.getColorModel().getPixelSize() == 32, "ARGB pixel size 32");
        check(img.getTransparency() == Transparency.TRANSLUCENT, "ARGB translucent");
        check(!img.isAlphaPremultiplied(), "ARGB not premultiplied");

        int px = 0xFF112233;
        img.setRGB(3, 2, px);
        check(img.getRGB(3, 2) == px, "setRGB/getRGB exact round-trip (alpha kept)");
        check(img.getRGB(0, 0) == 0, "untouched pixel default 0");

        // Alpha is preserved through getRGB/setRGB for ARGB.
        img.setRGB(1, 1, 0x00112233);
        check(img.getRGB(1, 1) == 0x00112233, "transparent-alpha round-trip");

        // Batch setRGB/getRGB.
        int[] in = { 0xFF000001, 0xFF000002, 0xFF000003, 0xFF000004 };
        img.setRGB(1, 1, 4, 1, in, 0, 4);
        int[] out = new int[4];
        img.getRGB(1, 1, 4, 1, out, 0, 4);
        check(out[0] == 0xFF000001 && out[1] == 0xFF000002 && out[2] == 0xFF000003 && out[3] == 0xFF000004, "batch setRGB/getRGB round-trip");

        // Batch getRGB with null array allocates a fresh array (scansize = width).
        int[] allocated = img.getRGB(1, 1, 2, 1, null, 0, 2);
        check(allocated != null && allocated.length >= 2, "getRGB null array allocates");
        check(allocated[0] == 0xFF000001 && allocated[1] == 0xFF000002, "allocated batch values");

        // Batch setRGB with null data array -> NPE.
        check(throwsEx(NullPointerException.class, () -> img.setRGB(0, 0, 2, 1, null, 0, 2)), "setRGB batch null -> NPE");

        // Out-of-bounds getRGB(x,y).
        check(throwsEx(ArrayIndexOutOfBoundsException.class, () -> img.getRGB(10, 0)), "getRGB x out of bounds -> AIOOBE");
        check(throwsEx(ArrayIndexOutOfBoundsException.class, () -> img.getRGB(0, 8)), "getRGB y out of bounds -> AIOOBE");
        check(throwsEx(ArrayIndexOutOfBoundsException.class, () -> img.getRGB(-1, 0)), "getRGB x negative -> AIOOBE");
        check(throwsEx(ArrayIndexOutOfBoundsException.class, () -> img.getRGB(0, -1)), "getRGB y negative -> AIOOBE");
    }

    static void testRasterAndData() {
        BufferedImage img = new BufferedImage(8, 6, BufferedImage.TYPE_INT_ARGB);
        WritableRaster r = img.getRaster();
        check(r != null, "raster non-null");
        check(r.getWidth() == 8 && r.getHeight() == 6, "raster dims");
        check(r.getNumBands() == 4, "ARGB 4 bands");
        check(img.getAlphaRaster() != null, "alpha raster present");
        SampleModel sm = img.getSampleModel();
        check(sm.getNumBands() == 4, "sample model 4 bands");
        check(sm.getWidth() == 8 && sm.getHeight() == 6, "sample model dims");

        Raster data = img.getData();
        check(data.getWidth() == 8 && data.getHeight() == 6, "getData dims");

        // getData(Rectangle) crops.
        Raster crop = img.getData(new java.awt.Rectangle(1, 1, 3, 2));
        check(crop.getWidth() == 3 && crop.getHeight() == 2, "getData(Rectangle) dims");

        // copyData into a compatible raster copies samples.
        WritableRaster dst = r.createCompatibleWritableRaster();
        img.copyData(dst);
        img.setRGB(3, 2, 0xFF010203);
        img.copyData(dst);
        check(dst.getSample(3, 2, 0) == r.getSample(3, 2, 0), "copyData copies sample");

        // Non-alpha image has null alpha raster.
        BufferedImage rgb = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        check(rgb.getAlphaRaster() == null, "INT_RGB alpha raster null");

        // Tiles.
        check(img.getNumXTiles() == 1 && img.getNumYTiles() == 1, "single tile");
        check(img.getTileWidth() == 8 && img.getTileHeight() == 6, "tile dims");
        Raster tile = img.getTile(0, 0);
        check(tile.getWidth() == 8 && tile.getHeight() == 6, "tile(0,0) dims");
    }

    static void testGraphics() {
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        check(g != null, "getGraphics non-null");
        g.dispose();
        Graphics2D g2 = img.createGraphics();
        check(g2 != null, "createGraphics non-null");
        g2.setColor(Color.RED);
        g2.fillRect(0, 0, 2, 2);
        g2.dispose();
        check((img.getRGB(0, 0) & 0xFFFFFF) == 0xFF0000, "createGraphics fillRect writes pixels");
        check((img.getRGB(3, 3) & 0xFFFFFF) == 0, "untouched pixel after fill");
    }

    static void testSubimage() {
        BufferedImage img = new BufferedImage(8, 6, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(1, 1, 0xFF123456);
        BufferedImage sub = img.getSubimage(1, 1, 3, 2);
        check(sub.getWidth() == 3 && sub.getHeight() == 2, "subimage dims");
        check(sub.getRGB(0, 0) == img.getRGB(1, 1), "subimage pixel matches source");
        // Subimage shares the source raster: writing through it is visible in the parent.
        sub.setRGB(0, 0, 0xFFFF0000);
        check(img.getRGB(1, 1) == 0xFFFF0000, "subimage shares source data");

        check(throwsEx(java.awt.image.RasterFormatException.class, () -> img.getSubimage(4, 4, 5, 5)), "subimage overflow -> RasterFormatException");
        check(throwsEx(java.awt.image.RasterFormatException.class, () -> img.getSubimage(0, 0, 0, 5)), "subimage zero width -> RasterFormatException");
        check(throwsEx(java.awt.image.RasterFormatException.class, () -> img.getSubimage(-1, 0, 1, 1)), "subimage negative x -> RasterFormatException");
    }

    static void testFlushAndTransparency() {
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        img.flush();
        check(img.getWidth() == 4 && img.getHeight() == 4, "dimensions survive flush()");

        BufferedImage rgb = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        rgb.setRGB(0, 0, 0x00112233);
        check((rgb.getRGB(0, 0) & 0xFF000000) == 0xFF000000, "INT_RGB forces alpha 255");
        check(rgb.getTransparency() == Transparency.OPAQUE, "INT_RGB opaque");

        BufferedImage pre = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB_PRE);
        check(pre.isAlphaPremultiplied(), "ARGB_PRE premultiplied");
        BufferedImage bin = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_BINARY);
        check(bin.getTransparency() == Transparency.OPAQUE, "BYTE_BINARY opaque (default 2-entry cm)");
        check(bin.getColorModel().getPixelSize() == 1, "BYTE_BINARY 1-bit pixel size");

        // coerceData is callable on ARGB and does not change the non-premultiplied flag.
        img.coerceData(false);
        check(!img.isAlphaPremultiplied(), "coerceData(false) keeps non-premultiplied");

        // Grayscale: raster sample round-trips exactly; getRGB yields equal R/G/B (gray).
        BufferedImage gray = new BufferedImage(3, 3, BufferedImage.TYPE_BYTE_GRAY);
        gray.getRaster().setSample(1, 1, 0, 128);
        check(gray.getRaster().getSample(1, 1, 0) == 128, "BYTE_GRAY sample round-trip");
        int gpx = gray.getRGB(1, 1);
        int gr = (gpx >> 16) & 0xFF, gg = (gpx >> 8) & 0xFF, gb = gpx & 0xFF;
        check((gpx & 0xFF000000) == 0xFF000000, "BYTE_GRAY getRGB alpha 255");
        check(gr == gg && gg == gb, "BYTE_GRAY getRGB R==G==B");
    }

    static void testMisc() {
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        ImageProducer src = img.getSource();
        check(src != null, "getSource non-null");
        check(img.toString() != null && img.toString().length() > 0, "toString non-empty");
        check(img.getProperty("anything") == java.awt.Image.UndefinedProperty, "getProperty unknown -> UndefinedProperty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructorTypes();
        testConstructorErrors();
        testPixelRoundTrip();
        testRasterAndData();
        testGraphics();
        testSubimage();
        testFlushAndTransparency();
        testMisc();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
