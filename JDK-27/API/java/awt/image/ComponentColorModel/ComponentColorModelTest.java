/*
 * @test
 * @summary java.awt.image.ComponentColorModel conformance: white-box structure + black-box behavior.
 * @run main ComponentColorModelTest
 */

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.lang.reflect.*;

public class ComponentColorModelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ComponentColorModel", false, ComponentColorModelTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(c.getSuperclass() == ColorModel.class, "extends ColorModel");
        check(hasMethod(c, "getRed", Object.class), "getRed(Object)");
        check(hasMethod(c, "getGreen", Object.class), "getGreen(Object)");
        check(hasMethod(c, "getBlue", Object.class), "getBlue(Object)");
        check(hasMethod(c, "getAlpha", Object.class), "getAlpha(Object)");
        check(hasMethod(c, "getRGB", Object.class), "getRGB(Object)");
        check(hasMethod(c, "getDataElements", int.class, Object.class), "getDataElements(int,Object)");
        check(hasMethod(c, "getComponents", Object.class, int[].class, int.class), "getComponents(Object,int[],int)");
        check(hasMethod(c, "getDataElement", int[].class, int.class), "getDataElement(int[],int)");
        check(hasMethod(c, "coerceData", java.awt.image.WritableRaster.class, boolean.class), "coerceData");
        check(hasMethod(c, "createCompatibleWritableRaster", int.class, int.class), "createCompatibleWritableRaster");
        check(hasMethod(c, "createCompatibleSampleModel", int.class, int.class), "createCompatibleSampleModel");
    }

    static void testBlackBox() {
        ColorSpace srgb = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ComponentColorModel cm = new ComponentColorModel(srgb, new int[] { 8, 8, 8 },
                false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        check(cm.getNumComponents() == 3, "3 components");
        check(cm.getNumColorComponents() == 3, "3 color components");
        check(!cm.hasAlpha(), "no alpha");
        check(!cm.isAlphaPremultiplied(), "non-premultiplied");
        check(cm.getPixelSize() == 24, "pixel size 24");
        check(cm.getTransferType() == DataBuffer.TYPE_BYTE, "transfer type byte");
        check(cm.getTransparency() == Transparency.OPAQUE, "opaque");
        check(cm.getColorSpace() == srgb, "color space sRGB");
        int[] sizes = cm.getComponentSize();
        check(sizes.length == 3 && sizes[0] == 8 && sizes[1] == 8 && sizes[2] == 8, "8-bit components");

        byte[] data = new byte[] { 0x11, 0x22, 0x33 };
        check(cm.getRed(data) == 0x11, "getRed byte[]");
        check(cm.getGreen(data) == 0x22, "getGreen byte[]");
        check(cm.getBlue(data) == 0x33, "getBlue byte[]");
        check(cm.getAlpha(data) == 255, "opaque alpha 255");
        check(cm.getRGB(data) == 0xFF112233, "getRGB byte[] exact");

        Object out = cm.getDataElements(0xFF445566, null);
        check(out instanceof byte[], "getDataElements returns byte[]");
        byte[] b = (byte[]) out;
        check(b[0] == 0x44 && b[1] == 0x55 && b[2] == 0x66, "getDataElements packs bytes");
        check(cm.getRGB(b) == 0xFF445566, "getRGB(getDataElements) round-trip");

        int[] comps = new int[3];
        cm.getComponents(data, comps, 0);
        check(comps[0] == 0x11 && comps[1] == 0x22 && comps[2] == 0x33, "getComponents");
        Object back = cm.getDataElements(comps, 0, null);
        check(cm.getRGB(back) == 0xFF112233, "getDataElements(components) round-trip");

        java.awt.image.WritableRaster wr = cm.createCompatibleWritableRaster(2, 2);
        check(wr.getWidth() == 2 && wr.getHeight() == 2, "compatible raster dims");
        check(wr.getNumBands() == 3, "compatible raster 3 bands");
        java.awt.image.SampleModel sm = cm.createCompatibleSampleModel(2, 2);
        check(sm.getNumBands() == 3 && sm.getDataType() == DataBuffer.TYPE_BYTE, "compatible sample model");
        check(cm.isCompatibleRaster(wr), "isCompatibleRaster");
        check(cm.isCompatibleSampleModel(sm), "isCompatibleSampleModel");

        // premultiplied alpha model divides out alpha on read
        ComponentColorModel pm = new ComponentColorModel(srgb, new int[] { 8, 8, 8, 8 },
                true, true, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
        check(pm.hasAlpha(), "premultiplied has alpha");
        check(pm.isAlphaPremultiplied(), "isAlphaPremultiplied true");
        check(pm.getNumComponents() == 4, "premultiplied 4 components");
        check(pm.getTransparency() == Transparency.TRANSLUCENT, "premultiplied translucent");
        byte[] pd = new byte[] { (byte) 0x80, 0, 0, (byte) 0x80 };
        check(pm.getRed(pd) == 255, "premultiplied red divided out to 255");
        check(pm.getAlpha(pd) == 128, "premultiplied alpha 128");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
