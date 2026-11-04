/*
 * @test
 * @summary java.awt.image.DirectColorModel conformance: white-box structure + black-box behavior.
 * @run main DirectColorModelTest
 */

import java.awt.Transparency;
import java.awt.image.DirectColorModel;
import java.awt.image.PackedColorModel;
import java.awt.image.DataBuffer;
import java.lang.reflect.*;

public class DirectColorModelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.DirectColorModel", false, DirectColorModelTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(c.getSuperclass() == PackedColorModel.class, "extends PackedColorModel");
        check(hasMethod(c, "getRedMask"), "getRedMask()");
        check(hasMethod(c, "getGreenMask"), "getGreenMask()");
        check(hasMethod(c, "getBlueMask"), "getBlueMask()");
        check(hasMethod(c, "getAlphaMask"), "getAlphaMask()");
        check(hasMethod(c, "getRed", int.class), "getRed(int)");
        check(hasMethod(c, "getGreen", int.class), "getGreen(int)");
        check(hasMethod(c, "getBlue", int.class), "getBlue(int)");
        check(hasMethod(c, "getAlpha", int.class), "getAlpha(int)");
        check(hasMethod(c, "getRGB", int.class), "getRGB(int)");
    }

    static void testBlackBox() {
        DirectColorModel cm = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000);
        check(cm.getRedMask() == 0x00FF0000, "red mask");
        check(cm.getGreenMask() == 0x0000FF00, "green mask");
        check(cm.getBlueMask() == 0x000000FF, "blue mask");
        check(cm.getAlphaMask() == 0xFF000000, "alpha mask");
        check(cm.getPixelSize() == 32, "pixel size 32");
        check(cm.getNumComponents() == 4, "4 components");
        check(cm.getNumColorComponents() == 3, "3 color components");
        check(cm.hasAlpha(), "has alpha");
        check(!cm.isAlphaPremultiplied(), "non-premultiplied");
        check(cm.getTransferType() == DataBuffer.TYPE_INT, "transfer type INT");
        check(cm.getTransparency() == Transparency.TRANSLUCENT, "translucent");

        int px = 0xFF112233;
        check(cm.getRed(px) == 0x11, "getRed");
        check(cm.getGreen(px) == 0x22, "getGreen");
        check(cm.getBlue(px) == 0x33, "getBlue");
        check(cm.getAlpha(px) == 0xFF, "getAlpha");
        check(cm.getRGB(px) == px, "getRGB round-trip");

        // extreme channel values
        check(cm.getRed(0x00FF0000) == 255, "red max 255");
        check(cm.getGreen(0x00008000) == 128, "green mid 128");
        check(cm.getBlue(0x00000000) == 0, "blue zero 0");
        check(cm.getAlpha(0x00000000) == 0, "alpha zero");

        // no-alpha 3-mask model forces opaque alpha
        DirectColorModel noAlpha = new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF);
        check(!noAlpha.hasAlpha(), "3-mask no alpha");
        check(noAlpha.getAlphaMask() == 0, "alpha mask 0");
        check(noAlpha.getAlpha(0x00112233) == 255, "no-alpha alpha 255");
        check(noAlpha.getTransparency() == Transparency.OPAQUE, "no-alpha opaque");
        check(noAlpha.getRGB(0x00112233) == 0xFF112233, "no-alpha getRGB forces 0xFF alpha");

        // 16-bit 5-6-5 model scales components to full range
        DirectColorModel rgb565 = new DirectColorModel(16, 0xF800, 0x07E0, 0x001F);
        check(rgb565.getPixelSize() == 16, "565 pixel size 16");
        check(rgb565.getRed(0xF800) == 255, "565 red max 255");
        check(rgb565.getGreen(0x07E0) == 255, "565 green max 255");
        check(rgb565.getBlue(0x001F) == 255, "565 blue max 255");

        // components <-> data element round trip
        int[] comps = new int[4];
        cm.getComponents(px, comps, 0);
        check(comps[0] == 0x11 && comps[1] == 0x22 && comps[2] == 0x33 && comps[3] == 0xFF, "getComponents");
        int de = cm.getDataElement(comps, 0);
        check(cm.getRGB(de) == px, "getDataElement round-trip");

        // compatible raster
        java.awt.image.WritableRaster wr = cm.createCompatibleWritableRaster(2, 2);
        check(wr.getNumBands() == 4, "compatible raster 4 bands");
        check(cm.isCompatibleRaster(wr), "isCompatibleRaster true");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
