/*
 * @test
 * @summary java.awt.image.IndexColorModel conformance: class reflection.
 * @run main IndexColorModelTest
 */

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

    public static void main(String[] args) throws Exception {
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
        check(hasMethod(c, "finalize"), "method finalize");
        check(hasMethod(c, "toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
