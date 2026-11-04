/*
 * @test
 * @summary java.awt.image.IndexColorModel conformance: white-box structure + black-box behavior.
 * @run main IndexColorModelTest
 */

import java.awt.Transparency;
import java.awt.image.IndexColorModel;
import java.awt.image.ColorModel;
import java.lang.reflect.*;

public class IndexColorModelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.IndexColorModel", false, IndexColorModelTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(c.getSuperclass() == ColorModel.class, "extends ColorModel");
        check(hasMethod(c, "getMapSize"), "getMapSize()");
        check(hasMethod(c, "getTransparentPixel"), "getTransparentPixel()");
        check(hasMethod(c, "getReds", byte[].class), "getReds");
        check(hasMethod(c, "getGreens", byte[].class), "getGreens");
        check(hasMethod(c, "getBlues", byte[].class), "getBlues");
        check(hasMethod(c, "getAlphas", byte[].class), "getAlphas");
        check(hasMethod(c, "getRGBs", int[].class), "getRGBs");
        check(hasMethod(c, "getRed", int.class), "getRed(int)");
        check(hasMethod(c, "getGreen", int.class), "getGreen(int)");
        check(hasMethod(c, "getBlue", int.class), "getBlue(int)");
        check(hasMethod(c, "getAlpha", int.class), "getAlpha(int)");
        check(hasMethod(c, "getRGB", int.class), "getRGB(int)");
        check(hasMethod(c, "getValidPixels"), "getValidPixels()");
        check(hasMethod(c, "isValid", int.class), "isValid(int)");
        check(hasMethod(c, "isValid"), "isValid()");
    }

    static void testBlackBox() {
        byte[] r = new byte[] { (byte) 0xFF, 0x10, 0x20, 0x30 };
        byte[] g = new byte[] { 0x00, (byte) 0xEE, 0x40, 0x50 };
        byte[] b = new byte[] { 0x00, 0x01, (byte) 0xAB, 0x60 };
        IndexColorModel cm = new IndexColorModel(8, 4, r, g, b);
        check(cm.getMapSize() == 4, "map size 4");
        check(cm.getPixelSize() == 8, "pixel size 8");
        check(cm.getNumComponents() == 3, "3 components (opaque)");
        check(cm.getNumColorComponents() == 3, "3 color components");
        check(!cm.hasAlpha(), "no alpha");
        check(cm.getTransparency() == Transparency.OPAQUE, "opaque");
        check(cm.getRed(0) == 255, "getRed unsigned byte");
        check(cm.getGreen(1) == 0xEE, "getGreen");
        check(cm.getBlue(2) == 0xAB, "getBlue");
        check(cm.getAlpha(0) == 255, "opaque alpha 255");
        check(cm.getRGB(0) == 0xFFFF0000, "getRGB(0) 0xFFFF0000");
        check(cm.getRGB(1) == 0xFF10EE01, "getRGB(1)");

        // look-up tables
        byte[] rr = new byte[4], gg = new byte[4], bb = new byte[4];
        cm.getReds(rr); cm.getGreens(gg); cm.getBlues(bb);
        check(rr[0] == (byte) 0xFF && rr[1] == 0x10 && rr[2] == 0x20, "getReds");
        check(gg[1] == (byte) 0xEE, "getGreens");
        check(bb[2] == (byte) 0xAB, "getBlues");
        int[] rgb = new int[4];
        cm.getRGBs(rgb);
        check(rgb[3] == cm.getRGB(3), "getRGBs matches getRGB");

        // with alpha
        byte[] a = new byte[] { (byte) 0xFF, (byte) 0x80, 0x00, 0x40 };
        IndexColorModel cma = new IndexColorModel(8, 4, r, g, b, a);
        check(cma.hasAlpha(), "has alpha");
        check(cma.getNumComponents() == 4, "4 components with alpha");
        check(cma.getAlpha(0) == 255, "alpha 255");
        check(cma.getAlpha(1) == 128, "alpha 128");
        check(cma.getAlpha(2) == 0, "alpha 0");
        check(cma.getRGB(0) == 0xFFFF0000, "getRGB with alpha");
        check(cma.getRGB(1) == 0x8010EE01, "getRGB semi alpha");

        // transparent pixel constructor
        IndexColorModel ctp = new IndexColorModel(8, 3, r, g, b, 1);
        check(ctp.getTransparentPixel() == 1, "transparent pixel 1");
        check(ctp.getAlpha(1) == 0, "transparent index alpha 0");
        check(ctp.getTransparency() == Transparency.BITMASK, "bitmask transparency");

        // data element <-> components
        int[] comps = new int[4];
        cma.getComponents(2, comps, 0);
        check(comps[0] == 0x20 && comps[1] == 0x40 && comps[2] == 0xAB && comps[3] == 0, "getComponents");
        int[] inComps = new int[] { 0x30, 0x50, 0x60, 0x40 };
        int de = cma.getDataElement(inComps, 0);
        check(de == 3, "getDataElement finds index 3");
        check(cma.getDataElement(new int[] { 0x10, 0xEE, 0x01, 0x80 }, 0) == 1, "getDataElement finds index 1");

        // compatible raster is single-band
        java.awt.image.WritableRaster wr = cm.createCompatibleWritableRaster(2, 2);
        check(wr.getNumBands() == 1, "indexed raster 1 band");
        check(cm.isCompatibleRaster(wr), "isCompatibleRaster true");

        // valid pixels
        check(cm.isValid(3), "isValid(3)");
        check(!cm.isValid(4), "!isValid(4)");
        check(cm.isValid(), "isValid()");
        check(cm.getValidPixels() != null, "getValidPixels non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
