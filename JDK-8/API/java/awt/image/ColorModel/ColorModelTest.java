/*
 * @test
 * @summary java.awt.image.ColorModel conformance: white-box structure + black-box contract.
 * @run main ColorModelTest
 */

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

public class ColorModelTest {
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
        Class<?> c = Class.forName("java.awt.image.ColorModel");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.Transparency"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getRGBdefault"), "method getRGBdefault");
        check(hasMethod(c, "hasAlpha"), "method hasAlpha");
        check(hasMethod(c, "isAlphaPremultiplied"), "method isAlphaPremultiplied");
        check(hasMethod(c, "getTransferType"), "method getTransferType");
        check(hasMethod(c, "getPixelSize"), "method getPixelSize");
        check(hasMethod(c, "getComponentSize"), "method getComponentSize");
        check(hasMethod(c, "getTransparency"), "method getTransparency");
        check(hasMethod(c, "getNumComponents"), "method getNumComponents");
        check(hasMethod(c, "getNumColorComponents"), "method getNumColorComponents");
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
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "getColorSpace"), "method getColorSpace");
        check(hasMethod(c, "coerceData"), "method coerceData");
        check(hasMethod(c, "isCompatibleRaster"), "method isCompatibleRaster");
        check(hasMethod(c, "createCompatibleWritableRaster"), "method createCompatibleWritableRaster");
        check(hasMethod(c, "createCompatibleSampleModel"), "method createCompatibleSampleModel");
        check(hasMethod(c, "isCompatibleSampleModel"), "method isCompatibleSampleModel");
        check(hasMethod(c, "getAlphaRaster"), "method getAlphaRaster");
        check(hasMethod(c, "toString"), "method toString");
    }

    static void testRGBDefault() {
        // getRGBdefault(): the default RGB model, a DirectColorModel over sRGB.
        ColorModel def = ColorModel.getRGBdefault();
        check(def != null, "getRGBdefault non-null");
        check(def == ColorModel.getRGBdefault(), "getRGBdefault stable reference");
        check(def.getPixelSize() == 32, "default pixel size 32");
        check(def.getNumComponents() == 4, "default num components 4");
        check(def.getNumColorComponents() == 3, "default num color components 3");
        check(def.hasAlpha(), "default has alpha");
        check(!def.isAlphaPremultiplied(), "default not premultiplied");
        check(def.getTransferType() == DataBuffer.TYPE_INT, "default transfer type int");
        check(def.getTransparency() == Transparency.TRANSLUCENT, "default translucent");
        check(def.getColorSpace() == ColorSpace.getInstance(ColorSpace.CS_sRGB), "default color space sRGB");
        // Component extraction is exact for a 8-8-8-8 model.
        int px = 0xff112233;
        check(def.getRed(px) == 0x11, "default getRed");
        check(def.getGreen(px) == 0x22, "default getGreen");
        check(def.getBlue(px) == 0x33, "default getBlue");
        check(def.getAlpha(px) == 0xff, "default getAlpha");
        check(def.getRGB(px) == px, "default getRGB identity");
        int[] cs = def.getComponentSize();
        check(cs.length == 4 && cs[0] == 8 && cs[1] == 8 && cs[2] == 8 && cs[3] == 8, "default component sizes 8/8/8/8");
        check(def.getComponentSize(0) == 8 && def.getComponentSize(3) == 8, "default component size per index");
        check(def instanceof DirectColorModel, "default is a DirectColorModel");
    }

    static void testBaseContractThroughSubclass() {
        // The protected ColorModel(int bits) constructor surfaced via a subclass.
        ColorModel cm = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
        check(cm.getPixelSize() == 24, "pixel size from ctor bits");
        check(!cm.hasAlpha(), "no alpha variant");
        check(!cm.isAlphaPremultiplied(), "never premultiplied for DCM");
        check(cm.getAlpha(0) == 255, "alpha forced 255 without alpha channel");
        check(cm.getNumColorComponents() == 3, "color components 3");
        check(cm.toString() != null && cm.toString().length() > 0, "toString non-empty");

        // createCompatibleWritableRaster through the base type.
        WritableRaster wr = cm.createCompatibleWritableRaster(4, 5);
        check(wr.getWidth() == 4 && wr.getHeight() == 5, "compatible raster dims");
        check(cm.isCompatibleRaster(wr), "own compatible raster accepted");
        BufferedImage other = new BufferedImage(4, 5, BufferedImage.TYPE_INT_ARGB);
        check(!cm.isCompatibleRaster(other.getRaster()), "foreign raster rejected");

        // coerceData on a non-premultiplied DCM returns the model itself.
        check(cm.coerceData(wr, false) == cm, "coerceData(false) returns same model");

        // equals/hashCode contract on the base type.
        ColorModel same = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
        check(cm.equals(same) && same.equals(cm), "equal models symmetric");
        check(cm.hashCode() == same.hashCode(), "equal models same hashCode");
        check(!cm.equals(new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000)), "different masks not equal");
        check(!cm.equals(null), "equals(null) false");
        check(cm.equals(cm), "equals(self) true");
    }

    static void testTransparencyConstants() {
        check(Transparency.OPAQUE == 1 && Transparency.BITMASK == 2 && Transparency.TRANSLUCENT == 3,
              "Transparency constants 1/2/3");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testRGBDefault();
        testBaseContractThroughSubclass();
        testTransparencyConstants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
