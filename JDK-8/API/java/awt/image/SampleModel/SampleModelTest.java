/*
 * @test
 * @summary java.awt.image.SampleModel conformance: abstract class reflection.
 * @run main SampleModelTest
 */

public class SampleModelTest {
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
        Class<?> c = Class.forName("java.awt.image.SampleModel");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getNumBands"), "method getNumBands");
        check(hasMethod(c, "getNumDataElements"), "method getNumDataElements");
        check(hasMethod(c, "getDataType"), "method getDataType");
        check(hasMethod(c, "getTransferType"), "method getTransferType");
        check(hasMethod(c, "getPixel"), "method getPixel");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
        check(hasMethod(c, "setDataElements"), "method setDataElements");
        check(hasMethod(c, "getPixels"), "method getPixels");
        check(hasMethod(c, "getSample"), "method getSample");
        check(hasMethod(c, "getSampleFloat"), "method getSampleFloat");
        check(hasMethod(c, "getSampleDouble"), "method getSampleDouble");
        check(hasMethod(c, "getSamples"), "method getSamples");
        check(hasMethod(c, "setPixel"), "method setPixel");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "setSample"), "method setSample");
        check(hasMethod(c, "setSamples"), "method setSamples");
        check(hasMethod(c, "createCompatibleSampleModel"), "method createCompatibleSampleModel");
        check(hasMethod(c, "createSubsetSampleModel"), "method createSubsetSampleModel");
        check(hasMethod(c, "createDataBuffer"), "method createDataBuffer");
        check(hasMethod(c, "getSampleSize"), "method getSampleSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
