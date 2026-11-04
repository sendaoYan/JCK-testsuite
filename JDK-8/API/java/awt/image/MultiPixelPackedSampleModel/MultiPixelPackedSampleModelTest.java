/*
 * @test
 * @summary java.awt.image.MultiPixelPackedSampleModel conformance: class reflection.
 * @run main MultiPixelPackedSampleModelTest
 */

public class MultiPixelPackedSampleModelTest {
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
        Class<?> c = Class.forName("java.awt.image.MultiPixelPackedSampleModel");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.SampleModel"), "superclass java.awt.image.SampleModel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "createCompatibleSampleModel"), "method createCompatibleSampleModel");
        check(hasMethod(c, "createDataBuffer"), "method createDataBuffer");
        check(hasMethod(c, "getNumDataElements"), "method getNumDataElements");
        check(hasMethod(c, "getSampleSize"), "method getSampleSize");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "getBitOffset"), "method getBitOffset");
        check(hasMethod(c, "getScanlineStride"), "method getScanlineStride");
        check(hasMethod(c, "getPixelBitStride"), "method getPixelBitStride");
        check(hasMethod(c, "getDataBitOffset"), "method getDataBitOffset");
        check(hasMethod(c, "getTransferType"), "method getTransferType");
        check(hasMethod(c, "createSubsetSampleModel"), "method createSubsetSampleModel");
        check(hasMethod(c, "getSample"), "method getSample");
        check(hasMethod(c, "setSample"), "method setSample");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
        check(hasMethod(c, "getPixel"), "method getPixel");
        check(hasMethod(c, "setDataElements"), "method setDataElements");
        check(hasMethod(c, "setPixel"), "method setPixel");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "hashCode"), "method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
