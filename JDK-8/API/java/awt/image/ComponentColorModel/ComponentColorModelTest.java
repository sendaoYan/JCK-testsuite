/*
 * @test
 * @summary java.awt.image.ComponentColorModel conformance: class reflection.
 * @run main ComponentColorModelTest
 */

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

    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
