/*
 * @test
 * @summary java.awt.image.BandedSampleModel conformance: class reflection.
 * @run main BandedSampleModelTest
 */

public class BandedSampleModelTest {
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
        Class<?> c = Class.forName("java.awt.image.BandedSampleModel");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.ComponentSampleModel"), "superclass java.awt.image.ComponentSampleModel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "createCompatibleSampleModel"), "method createCompatibleSampleModel");
        check(hasMethod(c, "createSubsetSampleModel"), "method createSubsetSampleModel");
        check(hasMethod(c, "createDataBuffer"), "method createDataBuffer");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
        check(hasMethod(c, "getPixel"), "method getPixel");
        check(hasMethod(c, "getPixels"), "method getPixels");
        check(hasMethod(c, "getSample"), "method getSample");
        check(hasMethod(c, "getSampleFloat"), "method getSampleFloat");
        check(hasMethod(c, "getSampleDouble"), "method getSampleDouble");
        check(hasMethod(c, "getSamples"), "method getSamples");
        check(hasMethod(c, "setDataElements"), "method setDataElements");
        check(hasMethod(c, "setPixel"), "method setPixel");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "setSample"), "method setSample");
        check(hasMethod(c, "setSamples"), "method setSamples");
        check(hasMethod(c, "hashCode"), "method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
