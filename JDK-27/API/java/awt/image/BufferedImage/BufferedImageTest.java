/*
 * @test
 * @summary java.awt.image.BufferedImage conformance: white-box structure + black-box behavior.
 * @run main BufferedImageTest
 */

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;
import java.awt.image.ImageProducer;
import java.lang.reflect.*;

public class BufferedImageTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }
    static boolean hasField(Class<?> c, String name) {
        try { c.getField(name); return true; } catch (NoSuchFieldException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.BufferedImage", false, BufferedImageTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!c.isInterface() && !c.isEnum(), "concrete class");
        check(c.getSuperclass() == java.awt.Image.class, "extends java.awt.Image");
        check(WritableRenderedImage.class.isAssignableFrom(c), "implements WritableRenderedImage");
        check(Transparency.class.isAssignableFrom(c), "implements Transparency");
        check(hasField(c, "TYPE_INT_ARGB"), "has TYPE_INT_ARGB constant");
        check(hasField(c, "TYPE_INT_RGB"), "has TYPE_INT_RGB constant");
        check(hasField(c, "TYPE_BYTE_GRAY"), "has TYPE_BYTE_GRAY constant");
        check(hasMethod(c, "getRGB", int.class, int.class), "getRGB(int,int)");
        check(hasMethod(c, "setRGB", int.class, int.class, int.class), "setRGB(int,int,int)");
        check(hasMethod(c, "getRGB", int.class, int.class, int.class, int.class, int[].class, int.class, int.class), "getRGB batch");
        check(hasMethod(c, "setRGB", int.class, int.class, int.class, int.class, int[].class, int.class, int.class), "setRGB batch");
        check(hasMethod(c, "getRaster"), "getRaster()");
        check(hasMethod(c, "getColorModel"), "getColorModel()");
        check(hasMethod(c, "getType"), "getType()");
        check(hasMethod(c, "getSubimage", int.class, int.class, int.class, int.class), "getSubimage");
        check(hasMethod(c, "createGraphics"), "createGraphics()");
        check(hasMethod(c, "getSource"), "getSource()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        BufferedImage img = new BufferedImage(8, 6, BufferedImage.TYPE_INT_ARGB);
        check(img.getWidth() == 8, "width 8");
        check(img.getHeight() == 6, "height 6");
        check(img.getType() == BufferedImage.TYPE_INT_ARGB, "type ARGB");
        check(img.getColorModel().getPixelSize() == 32, "pixel size 32");
        check(img.getMinX() == 0 && img.getMinY() == 0, "min x/y 0");
        check(img.getTransparency() == Transparency.TRANSLUCENT, "ARGB translucent");
        check(!img.isAlphaPremultiplied(), "ARGB not premultiplied");

        int px = 0xFF112233;
        img.setRGB(3, 2, px);
        check(img.getRGB(3, 2) == px, "setRGB/getRGB exact int round-trip");
        check(img.getRGB(0, 0) == 0, "untouched pixel default 0");

        // batch setRGB/getRGB
        int[] in = { 0xFF000001, 0xFF000002, 0xFF000003, 0xFF000004 };
        img.setRGB(1, 1, 4, 1, in, 0, 4);
        int[] out = new int[4];
        img.getRGB(1, 1, 4, 1, out, 0, 4);
        check(out[0] == 0xFF000001 && out[1] == 0xFF000002 && out[2] == 0xFF000003 && out[3] == 0xFF000004,
              "batch setRGB/getRGB round-trip");

        WritableRaster r = img.getRaster();
        check(r != null, "raster non-null");
        check(r.getWidth() == 8 && r.getHeight() == 6, "raster dims");
        check(r.getNumBands() == 4, "ARGB 4 bands");
        check(img.getAlphaRaster() != null, "alpha raster present");
        SampleModel sm = img.getSampleModel();
        check(sm.getNumBands() == 4, "sample model 4 bands");
        check(sm.getWidth() == 8 && sm.getHeight() == 6, "sample model dims");

        Graphics g = img.getGraphics();
        check(g != null, "getGraphics non-null");
        g.dispose();
        Graphics2D g2 = img.createGraphics();
        check(g2 != null, "createGraphics non-null");
        g2.dispose();

        BufferedImage sub = img.getSubimage(1, 1, 3, 2);
        check(sub.getWidth() == 3 && sub.getHeight() == 2, "subimage dims");
        check(sub.getRGB(0, 0) == img.getRGB(1, 1), "subimage pixel matches source");

        ImageProducer src = img.getSource();
        check(src != null, "getSource non-null");

        check(img.getNumXTiles() == 1 && img.getNumYTiles() == 1, "single tile");
        check(img.getTileWidth() == 8 && img.getTileHeight() == 6, "tile dims");
        Raster tile = img.getTile(0, 0);
        check(tile.getWidth() == 8 && tile.getHeight() == 6, "tile(0,0) dims");
        Raster data = img.getData();
        check(data.getWidth() == 8 && data.getHeight() == 6, "getData dims");

        // Type INT_RGB forces opaque alpha on setRGB.
        BufferedImage rgb = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        rgb.setRGB(0, 0, 0x00112233);
        check((rgb.getRGB(0, 0) & 0xFF000000) == 0xFF000000, "INT_RGB forces alpha 255");
        check(rgb.getType() == BufferedImage.TYPE_INT_RGB, "INT_RGB type");
        check(rgb.getTransparency() == Transparency.OPAQUE, "INT_RGB opaque");

        // coerceData is callable and does not change ARGB non-premultiplied flag.
        img.coerceData(false);
        check(!img.isAlphaPremultiplied(), "coerceData(false) keeps non-premultiplied");

        // copyData into a compatible raster copies pixels.
        WritableRaster dst = r.createCompatibleWritableRaster();
        img.copyData(dst);
        check(dst.getSample(3, 2, 0) == r.getSample(3, 2, 0), "copyData copies sample");

        check(img.toString() != null && img.toString().length() > 0, "toString non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
