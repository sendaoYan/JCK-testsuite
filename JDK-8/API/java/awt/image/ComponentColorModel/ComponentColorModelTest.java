/*
 * @test
 * @summary java.awt.image.ComponentColorModel conformance: white-box structure + black-box contract.
 * @run main ComponentColorModelTest
 */

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

public class ComponentColorModelTest {
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
        Class<?> c = Class.forName("java.awt.image.ComponentColorModel");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.ColorModel"), "superclass java.awt.image.ColorModel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getRed"), "method getRed");
        check(hasMethod(c, "getGreen"), "method getGreen");
        check(hasMethod(c, "getBlue"), "method getBlue");
        check(hasMethod(c, "getAlpha"), "method getAlpha");
        check(hasMethod(c, "getRGB"), "method getRGB");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
        check(hasMethod(c, "getComponents"), "method getComponents");
        check(hasMethod(c, "getUnnormalizedComponents"), "method getUnnormalizedComponents");
        check(hasMethod(c, "getNormalizedComponents"), "method getNormalizedComponents");
        check(hasMethod(c, "getDataElement"), "method getDataElement");
        check(hasMethod(c, "coerceData"), "method coerceData");
        check(hasMethod(c, "isCompatibleRaster"), "method isCompatibleRaster");
        check(hasMethod(c, "createCompatibleWritableRaster"), "method createCompatibleWritableRaster");
        check(hasMethod(c, "createCompatibleSampleModel"), "method createCompatibleSampleModel");
        check(hasMethod(c, "isCompatibleSampleModel"), "method isCompatibleSampleModel");
        check(hasMethod(c, "getAlphaRaster"), "method getAlphaRaster");
        check(hasMethod(c, "equals"), "method equals");
    }

    static void testByteRGB() {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ComponentColorModel m = new ComponentColorModel(cs, new int[] { 8, 8, 8 }, false, false,
                Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        check(m.getPixelSize() == 24, "pixel size 24");
        check(m.getNumComponents() == 3, "num components 3");
        check(m.getNumColorComponents() == 3, "num color components 3");
        check(!m.hasAlpha(), "no alpha");
        check(m.getAlpha(0) == 255, "alpha forced 255 without alpha");
        check(m.getTransparency() == Transparency.OPAQUE, "opaque");
        check(m.getTransferType() == DataBuffer.TYPE_BYTE, "byte transfer");
        int[] csz = m.getComponentSize();
        check(csz.length == 3 && csz[0] == 8 && csz[1] == 8 && csz[2] == 8, "component sizes 8/8/8");

        // Component getters on byte[] data elements are exact.
        byte[] px = new byte[] { (byte) 255, (byte) 128, (byte) 64 };
        check(m.getRed(px) == 255, "getRed(byte[]) exact");
        check(m.getGreen(px) == 128, "getGreen(byte[]) exact");
        check(m.getBlue(px) == 64, "getBlue(byte[]) exact");
        check(m.getAlpha(px) == 255, "getAlpha(byte[]) forced 255");
        check(m.getRGB(px) == 0xffff8040, "getRGB(byte[]) exact");

        // getComponents(Object,...) inverse of getDataElements(int[],...).
        int[] comps = m.getComponents(px, null, 0);
        check(comps.length == 3 && comps[0] == 255 && comps[1] == 128 && comps[2] == 64, "getComponents exact");
        Object de = m.getDataElements(comps, 0, null);
        check(de instanceof byte[] && m.getRed(de) == 255 && m.getGreen(de) == 128 && m.getBlue(de) == 64,
              "getDataElements(int[]) inverts getComponents");

        // Normalized <-> unnormalized conversions are inverse.
        float[] norm = m.getNormalizedComponents(comps, 0, null, 0);
        check(norm.length == 3 && norm[0] == 1.0f && Math.abs(norm[1] - 128f / 255f) < 1e-6f
              && Math.abs(norm[2] - 64f / 255f) < 1e-6f, "getNormalizedComponents exact");
        int[] back = m.getUnnormalizedComponents(norm, 0, null, 0);
        check(back[0] == 255 && back[1] == 128 && back[2] == 64, "getUnnormalizedComponents inverts");

        // getDataElements(float[],...) path.
        Object deF = m.getDataElements(norm, 0, null);
        check(m.getRed(deF) == 255 && m.getGreen(deF) == 128 && m.getBlue(deF) == 64,
              "getDataElements(float[]) exact");
    }

    static void testGray() {
        ColorSpace gs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ComponentColorModel m = new ComponentColorModel(gs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        check(m.getNumComponents() == 1, "gray single component");
        check(m.getNumColorComponents() == 1, "gray single color component");
        // getRed(int) is only defined for single-component models.
        int v = m.getRed(100);
        check(v == m.getGreen(100) && v == m.getBlue(100), "gray r==g==b");
        check(m.getAlpha(100) == 255, "gray alpha 255");
        check(m.getRGB(100) == (0xff000000 | (v << 16) | (v << 8) | v), "gray getRGB gray expansion");
    }

    static void testWithAlpha() {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ComponentColorModel m = new ComponentColorModel(cs, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        check(m.hasAlpha(), "has alpha");
        check(m.getNumComponents() == 4, "num components 4");
        check(m.getNumColorComponents() == 3, "num color components 3");
        check(!m.isAlphaPremultiplied(), "not premultiplied");
        check(m.getTransparency() == Transparency.TRANSLUCENT, "translucent");
        byte[] px = new byte[] { (byte) 255, (byte) 128, (byte) 64, (byte) 77 };
        check(m.getAlpha(px) == 77, "alpha exact");
        check(m.getRGB(px) == 0x4dff8040, "getRGB with alpha");
        // premultiplied variant
        ComponentColorModel mp = new ComponentColorModel(cs, true, true, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        check(mp.isAlphaPremultiplied(), "premultiplied flag");
        // alpha raster
        WritableRaster wr = m.createCompatibleWritableRaster(2, 2);
        check(wr.getNumBands() == 4, "compatible raster 4 bands");
        check(m.getAlphaRaster(wr) != null, "alpha raster non-null");
        check(m.isCompatibleRaster(wr), "own raster compatible");
        check(!m.isCompatibleRaster(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB).getRaster()),
              "foreign raster incompatible");
        check(m.createCompatibleSampleModel(3, 4).getNumBands() == 4, "compatible sample model 4 bands");
        // coerceData premultiplied converts in place and reports the converted model.
        WritableRaster wrp = mp.createCompatibleWritableRaster(1, 1);
        check(mp.coerceData(wrp, true) == mp, "coerceData to same state returns model");
    }

    static void testEquals() {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ComponentColorModel a = new ComponentColorModel(cs, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        ComponentColorModel b = new ComponentColorModel(cs, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        check(a.equals(b) && b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode consistent with equals");
        ComponentColorModel c = new ComponentColorModel(cs, true, true, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        check(!a.equals(c), "different premultiplied not equal");
        ComponentColorModel d = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        check(!a.equals(d), "different alpha not equal");
        check(a.equals(a) && !a.equals(null), "equals self true, null false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testByteRGB();
        testGray();
        testWithAlpha();
        testEquals();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
