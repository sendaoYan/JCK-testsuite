/*
 * @test
 * @summary java.awt.image.DirectColorModel conformance: white-box structure + black-box contract.
 * @run main DirectColorModelTest
 */

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

public class DirectColorModelTest {
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
        Class<?> c = Class.forName("java.awt.image.DirectColorModel");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.PackedColorModel"), "superclass java.awt.image.PackedColorModel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getRedMask"), "method getRedMask");
        check(hasMethod(c, "getGreenMask"), "method getGreenMask");
        check(hasMethod(c, "getBlueMask"), "method getBlueMask");
        check(hasMethod(c, "getAlphaMask"), "method getAlphaMask");
        check(hasMethod(c, "getRed"), "method getRed");
        check(hasMethod(c, "getGreen"), "method getGreen");
        check(hasMethod(c, "getBlue"), "method getBlue");
        check(hasMethod(c, "getAlpha"), "method getAlpha");
        check(hasMethod(c, "getRGB"), "method getRGB");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
        check(hasMethod(c, "getComponents"), "method getComponents");
        check(hasMethod(c, "createCompatibleWritableRaster"), "method createCompatibleWritableRaster");
        check(hasMethod(c, "getDataElement"), "method getDataElement");
        check(hasMethod(c, "coerceData"), "method coerceData");
        check(hasMethod(c, "isCompatibleRaster"), "method isCompatibleRaster");
        check(hasMethod(c, "toString"), "method toString");
    }

    static void test32BitModel() {
        DirectColorModel dcm = new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000);
        check(dcm.getRedMask() == 0xff0000, "red mask");
        check(dcm.getGreenMask() == 0xff00, "green mask");
        check(dcm.getBlueMask() == 0xff, "blue mask");
        check(dcm.getAlphaMask() == 0xff000000, "alpha mask");
        check(dcm.getPixelSize() == 32, "pixel size 32");
        check(dcm.hasAlpha(), "has alpha");
        check(!dcm.isAlphaPremultiplied(), "not premultiplied");
        check(dcm.getNumComponents() == 4, "num components 4");
        check(dcm.getNumColorComponents() == 3, "num color components 3");
        check(dcm.getTransferType() == DataBuffer.TYPE_INT, "transfer type int");
        check(dcm.getTransparency() == Transparency.TRANSLUCENT, "translucent");
        int[] cs = dcm.getComponentSize();
        check(cs.length == 4 && cs[0] == 8 && cs[1] == 8 && cs[2] == 8 && cs[3] == 8, "component sizes 8/8/8/8");
        check(dcm.getColorSpace() == ColorSpace.getInstance(ColorSpace.CS_sRGB), "sRGB default color space");

        // Exact component extraction from a packed pixel.
        int px = 0xff112233;
        check(dcm.getRed(px) == 0x11, "getRed exact");
        check(dcm.getGreen(px) == 0x22, "getGreen exact");
        check(dcm.getBlue(px) == 0x33, "getBlue exact");
        check(dcm.getAlpha(px) == 0xff, "getAlpha exact");
        check(dcm.getRGB(px) == px, "getRGB identity");

        // getComponents(int)/getDataElement(int[]) inverse round-trip.
        int[] comps = dcm.getComponents(px, null, 0);
        check(comps.length == 4 && comps[0] == 0x11 && comps[1] == 0x22 && comps[2] == 0x33 && comps[3] == 0xff,
              "getComponents exact");
        int[] reused = new int[4];
        check(dcm.getComponents(px, reused, 0) == reused, "getComponents reuses provided array");
        check(dcm.getDataElement(comps, 0) == px, "getDataElement inverts getComponents");

        // getDataElements(int,Object)/getRGB(Object) round-trip through int[] data elements.
        Object de = dcm.getDataElements(px, null);
        check(de instanceof int[] && ((int[]) de).length == 1, "getDataElements int[]");
        check(((int[]) de)[0] == px, "data element value");
        int[] buf = new int[1];
        check(dcm.getDataElements(px, buf) == buf && buf[0] == px, "getDataElements reuses array");
        check(dcm.getRGB(de) == px, "getRGB(Object) inverts getDataElements");
        check(dcm.getRed(de) == 0x11 && dcm.getGreen(de) == 0x22 && dcm.getBlue(de) == 0x33 && dcm.getAlpha(de) == 0xff,
              "component getters on data element");
    }

    static void test24BitModel() {
        DirectColorModel dcm = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
        check(!dcm.hasAlpha(), "24-bit no alpha");
        check(dcm.getAlphaMask() == 0, "24-bit zero alpha mask");
        check(dcm.getAlpha(0) == 255, "24-bit alpha forced 255");
        check(dcm.getNumComponents() == 3, "24-bit num components 3");
        check(dcm.getTransparency() == Transparency.OPAQUE, "24-bit opaque");
        int px = 0x112233;
        check(dcm.getRed(px) == 0x11 && dcm.getGreen(px) == 0x22 && dcm.getBlue(px) == 0x33, "24-bit components");
        check((dcm.getRGB(px) & 0xffffff) == 0x112233 && dcm.getRGB(px) == 0xff112233, "24-bit getRGB sets opaque alpha");
    }

    static void testColorSpaceVariant() {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        DirectColorModel dcm = new DirectColorModel(cs, 32, 0xff0000, 0xff00, 0xff, 0xff000000, false, DataBuffer.TYPE_INT);
        check(dcm.getPixelSize() == 32 && dcm.hasAlpha(), "ColorSpace ctor basics");
        check(dcm.getColorSpace() == cs, "ColorSpace ctor keeps color space");
        check(dcm.getRed(0xff112233) == 0x11, "ColorSpace ctor getRed");
    }

    static void testRasterCompatibility() {
        DirectColorModel dcm = new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000);
        WritableRaster wr = dcm.createCompatibleWritableRaster(4, 5);
        check(wr.getWidth() == 4 && wr.getHeight() == 5, "compatible raster dims");
        check(dcm.isCompatibleRaster(wr), "own raster compatible");
        check(!dcm.isCompatibleRaster(new BufferedImage(4, 5, BufferedImage.TYPE_3BYTE_BGR).getRaster()),
              "byte raster incompatible");
        SampleModel sm = dcm.createCompatibleSampleModel(2, 3);
        check(sm.getWidth() == 2 && sm.getHeight() == 3, "compatible sample model dims");
        check(dcm.isCompatibleSampleModel(sm), "own sample model compatible");
        check(!dcm.isCompatibleSampleModel(new BufferedImage(2, 2, BufferedImage.TYPE_3BYTE_BGR).getSampleModel()),
              "foreign sample model rejected");
        // coerceData on non-premultiplied data returns the model itself.
        check(dcm.coerceData(wr, false) == dcm, "coerceData(false) same model");
        // Alpha raster for a model with alpha is non-null; null for model without.
        check(dcm.getAlphaRaster(wr) != null, "alpha raster for alpha model");
        DirectColorModel d24 = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
        check(d24.getAlphaRaster(d24.createCompatibleWritableRaster(2, 2)) == null, "no alpha raster for alpha-less model");
        check(dcm.toString() != null && dcm.toString().length() > 0, "toString non-empty");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        test32BitModel();
        test24BitModel();
        testColorSpaceVariant();
        testRasterCompatibility();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
