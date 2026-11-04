/*
 * @test
 * @summary java.awt.image.DirectColorModel conformance: class reflection.
 * @run main DirectColorModelTest
 */

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

    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
