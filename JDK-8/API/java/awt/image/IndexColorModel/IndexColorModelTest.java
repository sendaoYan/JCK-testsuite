/*
 * @test
 * @summary java.awt.image.IndexColorModel conformance: white-box structure + black-box contract.
 * @run main IndexColorModelTest
 */

import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.math.BigInteger;

public class IndexColorModelTest {
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
        Class<?> c = Class.forName("java.awt.image.IndexColorModel");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.ColorModel"), "superclass java.awt.image.ColorModel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getTransparency"), "method getTransparency");
        check(hasMethod(c, "getComponentSize"), "method getComponentSize");
        check(hasMethod(c, "getMapSize"), "method getMapSize");
        check(hasMethod(c, "getTransparentPixel"), "method getTransparentPixel");
        check(hasMethod(c, "getReds"), "method getReds");
        check(hasMethod(c, "getGreens"), "method getGreens");
        check(hasMethod(c, "getBlues"), "method getBlues");
        check(hasMethod(c, "getAlphas"), "method getAlphas");
        check(hasMethod(c, "getRGBs"), "method getRGBs");
        check(hasMethod(c, "getRed"), "method getRed");
        check(hasMethod(c, "getGreen"), "method getGreen");
        check(hasMethod(c, "getBlue"), "method getBlue");
        check(hasMethod(c, "getAlpha"), "method getAlpha");
        check(hasMethod(c, "getRGB"), "method getRGB");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
        check(hasMethod(c, "getComponents"), "method getComponents");
        check(hasMethod(c, "getDataElement"), "method getDataElement");
        check(hasMethod(c, "createCompatibleWritableRaster"), "method createCompatibleWritableRaster");
        check(hasMethod(c, "isCompatibleRaster"), "method isCompatibleRaster");
        check(hasMethod(c, "createCompatibleSampleModel"), "method createCompatibleSampleModel");
        check(hasMethod(c, "isCompatibleSampleModel"), "method isCompatibleSampleModel");
        check(hasMethod(c, "convertToIntDiscrete"), "method convertToIntDiscrete");
        check(hasMethod(c, "isValid"), "method isValid");
        check(hasMethod(c, "getValidPixels"), "method getValidPixels");
        check(hasMethod(c, "toString"), "method toString");
    }

    static byte[] b(int... v) {
        byte[] r = new byte[v.length];
        for (int i = 0; i < v.length; i++) r[i] = (byte) v[i];
        return r;
    }

    static void testBasics() {
        IndexColorModel m = new IndexColorModel(8, 3, b(10, 20, 30), b(40, 50, 60), b(70, 80, 90));
        check(m.getMapSize() == 3, "map size 3");
        check(m.getPixelSize() == 8, "pixel size 8");
        check(m.getTransparentPixel() == -1, "no transparent pixel by default");
        check(m.getNumComponents() == 3, "num components 3");
        check(m.getNumColorComponents() == 3, "num color components 3");
        check(!m.hasAlpha(), "no alpha without alpha array");
        check(m.getAlpha(1) == 255, "alpha forced 255 without alpha array");
        check(m.getTransparency() == Transparency.OPAQUE, "opaque without alpha");
        check(m.getTransferType() == DataBuffer.TYPE_BYTE, "byte transfer type");
        int[] cs = m.getComponentSize();
        check(cs.length == 3 && cs[0] == 8 && cs[1] == 8 && cs[2] == 8, "component sizes 8/8/8");

        // Exact palette lookups.
        check(m.getRed(1) == 20, "getRed exact");
        check(m.getGreen(1) == 50, "getGreen exact");
        check(m.getBlue(1) == 80, "getBlue exact");
        check(m.getRGB(1) == 0xff143250, "getRGB exact (alpha 255)");

        // Bulk palette copies: getReds/getGreens/getBlues/getRGBs.
        byte[] reds = new byte[3], greens = new byte[3], blues = new byte[3];
        m.getReds(reds); m.getGreens(greens); m.getBlues(blues);
        check((reds[1] & 0xff) == 20 && (greens[1] & 0xff) == 50 && (blues[1] & 0xff) == 80, "palette bulk copy exact");
        int[] rgbs = new int[3];
        m.getRGBs(rgbs);
        check(rgbs[0] == 0xff0a2846 && rgbs[1] == 0xff143250 && rgbs[2] == 0xff1e3c5a, "getRGBs exact");
    }

    static void testAlphaAndTransparent() {
        // Separate alpha array -> TRANSLUCENT and exact alpha lookup.
        IndexColorModel ma = new IndexColorModel(8, 2, b(10, 20), b(40, 50), b(70, 80), b(255, 128));
        check(ma.hasAlpha(), "alpha array gives alpha");
        check(ma.getNumComponents() == 4, "alpha array num components 4");
        check(ma.getAlpha(1) == 128, "alpha exact from alpha array");
        check(ma.getTransparency() == Transparency.TRANSLUCENT, "alpha array translucent");
        byte[] alphas = new byte[2];
        ma.getAlphas(alphas);
        check((alphas[0] & 0xff) == 255 && (alphas[1] & 0xff) == 128, "getAlphas exact");

        // Explicit transparent index -> BITMASK.
        IndexColorModel mt = new IndexColorModel(8, 2, b(10, 20), b(40, 50), b(70, 80), 1);
        check(mt.getTransparentPixel() == 1, "transparent pixel index");
        check(mt.getTransparency() == Transparency.BITMASK, "transparent index bitmask");
        check(mt.getAlpha(1) == 0, "transparent pixel alpha 0");
        check(mt.getAlpha(0) == 255, "non-transparent alpha 255");

        // getDataElements/getRGB exact round-trip when rgb matches a palette entry.
        IndexColorModel m = new IndexColorModel(8, 3, b(10, 20, 30), b(40, 50, 60), b(70, 80, 90));
        Object de = m.getDataElements(0xff143250, null);
        check(de instanceof byte[] && (((byte[]) de)[0] & 0xff) == 1, "getDataElements finds exact index");
        check(m.getRGB(de) == 0xff143250, "getRGB(Object) round-trip");
    }

    static void testPackedCmap() {
        // (bits, size, byte[] cmap packed r/g/b, start, hasalpha) ctor: 3 bytes per entry.
        byte[] cmap = new byte[] { 10, 40, 70, 20, 50, 80, 30, 60, 90 };
        IndexColorModel m = new IndexColorModel(8, 3, cmap, 0, false);
        check(m.getMapSize() == 3, "packed cmap map size");
        check(m.getRed(1) == 20 && m.getGreen(1) == 50 && m.getBlue(1) == 80, "packed cmap components exact");
        // with hasalpha=true the cmap is r/g/b/alpha quadruplets.
        byte[] cmapA = new byte[] { 10, 40, 70, (byte) 255, 20, 50, 80, (byte) 128 };
        IndexColorModel ma = new IndexColorModel(8, 2, cmapA, 0, true);
        check(ma.getAlpha(1) == 128, "packed cmap with alpha exact");

        // (bits, size, int[] cmap, start, hasalpha, trans, transferType) ctor.
        IndexColorModel mi = new IndexColorModel(8, 2, new int[] { 0xff102030, 0xff405060 }, 0, false,
                Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        check(mi.getMapSize() == 2, "int cmap map size");
        check(mi.getRGB(0) == 0xff102030, "int cmap getRGB exact");
    }

    static void testValidPixels() {
        BigInteger valid = BigInteger.valueOf(0x2);
        IndexColorModel m = new IndexColorModel(8, 2, new int[] { 0xff102030, 0xff405060 }, 0,
                DataBuffer.TYPE_BYTE, valid);
        check(!m.isValid(), "not all valid when validBits has gaps");
        check(!m.isValid(0), "invalid index 0");
        check(m.isValid(1), "valid index 1");
        check(m.getValidPixels() != null && m.getValidPixels().testBit(1), "getValidPixels reflects ctor bits");

        IndexColorModel all = new IndexColorModel(8, 2, b(10, 20), b(40, 50), b(70, 80));
        check(all.isValid(), "all valid by default");
        check(all.isValid(0) && all.isValid(1), "each index valid by default");
    }

    static void testConvertAndCompatibility() {
        IndexColorModel m = new IndexColorModel(8, 3, b(10, 20, 30), b(40, 50, 60), b(70, 80, 90));
        WritableRaster wr = m.createCompatibleWritableRaster(2, 1);
        wr.setSample(0, 0, 0, 0);
        wr.setSample(1, 0, 0, 1);
        BufferedImage disc = m.convertToIntDiscrete(wr, false);
        check(disc.getType() == BufferedImage.TYPE_INT_RGB, "convertToIntDiscrete TYPE_INT_RGB");
        check(disc.getColorModel() instanceof DirectColorModel, "convertToIntDiscrete DirectColorModel");
        check(disc.getRGB(0, 0) == m.getRGB(0) && disc.getRGB(1, 0) == m.getRGB(1), "convertToIntDiscrete maps indices");
        check(m.isCompatibleRaster(wr), "own raster compatible");
        check(!m.isCompatibleRaster(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB).getRaster()),
              "int raster incompatible");
        SampleModel sm = m.createCompatibleSampleModel(3, 5);
        check(sm.getWidth() == 3 && sm.getHeight() == 5, "compatible sample model dims");
        check(m.isCompatibleSampleModel(sm), "own sample model compatible");
        check(m.toString() != null && m.toString().length() > 0, "toString non-empty");
    }

    static void testErrors() {
        // map size 0 / negative -> IAE.
        boolean threw = false;
        try { new IndexColorModel(2, 0, new byte[0], new byte[0], new byte[0]); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "map size 0 -> IAE");
        threw = false;
        try { new IndexColorModel(2, -1, new byte[0], new byte[0], new byte[0]); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "map size -1 -> IAE");
        // bits 0 -> IAE.
        threw = false;
        try { new IndexColorModel(0, 8, new byte[8], new byte[8], new byte[8]); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "pixel bits 0 -> IAE");
        // short component arrays -> AIOOBE.
        threw = false;
        try { new IndexColorModel(2, 8, new byte[] { 0 }, new byte[] { 0, 0 }, new byte[] { 0, 0, 0 }); }
        catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "short r array -> AIOOBE");
        threw = false;
        try { new IndexColorModel(2, 8, new byte[] { 0, 0 }, new byte[] { 0, 0 }, new byte[] { 0, 0 }, -1); }
        catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "transparent index beyond map -> AIOOBE");
        // getRGBs into a too-short array -> AIOOBE.
        IndexColorModel m = new IndexColorModel(2, 4, new byte[] { 0, 0, 0, 0 }, new byte[] { 0, 0, 0, 0 },
                new byte[] { 0, 0, 0, 0 });
        threw = false;
        try { m.getRGBs(new int[3]); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getRGBs short array -> AIOOBE");
        threw = false;
        try { m.getReds(new byte[3]); } catch (ArrayIndexOutOfBoundsException e) { threw = true; }
        check(threw, "getReds short array -> AIOOBE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBasics();
        testAlphaAndTransparent();
        testPackedCmap();
        testValidPixels();
        testConvertAndCompatibility();
        testErrors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
