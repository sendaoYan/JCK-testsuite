/*
 * @test
 * @summary java.awt.image.ColorModel conformance: white-box structure + black-box behavior.
 * @run main ColorModelTest
 */

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.lang.reflect.*;

public class ColorModelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ColorModel", false, ColorModelTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isAbstract(m), "abstract class");
        check(Transparency.class.isAssignableFrom(c), "implements Transparency");
        check(hasMethod(c, "getRGBdefault"), "getRGBdefault()");
        check(hasMethod(c, "getRed", int.class), "getRed(int)");
        check(hasMethod(c, "getGreen", int.class), "getGreen(int)");
        check(hasMethod(c, "getBlue", int.class), "getBlue(int)");
        check(hasMethod(c, "getAlpha", int.class), "getAlpha(int)");
        check(hasMethod(c, "getRGB", int.class), "getRGB(int)");
        check(hasMethod(c, "getPixelSize"), "getPixelSize()");
        check(hasMethod(c, "getNumComponents"), "getNumComponents()");
        check(hasMethod(c, "getNumColorComponents"), "getNumColorComponents()");
        check(hasMethod(c, "hasAlpha"), "hasAlpha()");
        check(hasMethod(c, "isAlphaPremultiplied"), "isAlphaPremultiplied()");
        check(hasMethod(c, "getTransferType"), "getTransferType()");
        check(hasMethod(c, "getColorSpace"), "getColorSpace()");
        check(hasMethod(c, "getTransparency"), "getTransparency()");
        check(hasMethod(c, "getComponentSize", int.class), "getComponentSize(int)");
        check(hasMethod(c, "getComponentSize"), "getComponentSize()");
        check(hasMethod(c, "createCompatibleWritableRaster", int.class, int.class), "createCompatibleWritableRaster");
        check(hasMethod(c, "createCompatibleSampleModel", int.class, int.class), "createCompatibleSampleModel");
    }

    static void testBlackBox() {
        ColorModel cm = ColorModel.getRGBdefault();
        check(cm != null, "getRGBdefault non-null");
        check(cm.getPixelSize() == 32, "default pixel size 32");
        check(cm.getNumComponents() == 4, "default 4 components");
        check(cm.getNumColorComponents() == 3, "default 3 color components");
        check(cm.hasAlpha(), "default has alpha");
        check(!cm.isAlphaPremultiplied(), "default non-premultiplied");
        check(cm.getTransferType() == DataBuffer.TYPE_INT, "default transfer type INT");
        check(cm.getTransparency() == Transparency.TRANSLUCENT, "default translucent");
        check(cm.getColorSpace().getType() == ColorSpace.TYPE_RGB, "default sRGB color space");

        int px = 0xFF336699;
        check(cm.getRed(px) == 0x33, "getRed 0x33");
        check(cm.getGreen(px) == 0x66, "getGreen 0x66");
        check(cm.getBlue(px) == 0x99, "getBlue 0x99");
        check(cm.getAlpha(px) == 0xFF, "getAlpha 0xFF");
        check(cm.getRGB(px) == px, "getRGB round-trip exact int");

        int[] sizes = cm.getComponentSize();
        check(sizes.length == 4, "component size array length 4");
        check(sizes[0] == 8 && sizes[1] == 8 && sizes[2] == 8 && sizes[3] == 8, "all 8-bit components");
        check(cm.getComponentSize(0) == 8, "getComponentSize(0) 8");

        // components <-> dataElements round trip
        int[] comps = new int[4];
        cm.getComponents(px, comps, 0);
        check(comps[0] == 0x33 && comps[1] == 0x66 && comps[2] == 0x99 && comps[3] == 0xFF, "getComponents extracts");
        int dataElem = cm.getDataElement(comps, 0);
        check(cm.getRGB(dataElem) == px, "getDataElement(getComponents) round-trip");

        // createCompatibleWritableRaster / SampleModel
        java.awt.image.WritableRaster wr = cm.createCompatibleWritableRaster(4, 4);
        check(wr.getWidth() == 4 && wr.getHeight() == 4, "compatible raster dims");
        check(wr.getNumBands() == 4, "compatible raster 4 bands");
        java.awt.image.SampleModel sm = cm.createCompatibleSampleModel(4, 4);
        check(sm.getWidth() == 4 && sm.getHeight() == 4, "compatible sample model dims");
        check(cm.isCompatibleSampleModel(sm), "isCompatibleSampleModel true");
        check(cm.isCompatibleRaster(wr), "isCompatibleRaster true");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
