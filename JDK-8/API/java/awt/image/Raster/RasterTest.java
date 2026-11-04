/*
 * @test
 * @summary java.awt.image.Raster conformance: class reflection.
 * @run main RasterTest
 */

public class RasterTest {
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
        Class<?> c = Class.forName("java.awt.image.Raster");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "createInterleavedRaster"), "method createInterleavedRaster");
        check(hasMethod(c, "createBandedRaster"), "method createBandedRaster");
        check(hasMethod(c, "createPackedRaster"), "method createPackedRaster");
        check(hasMethod(c, "createRaster"), "method createRaster");
        check(hasMethod(c, "createWritableRaster"), "method createWritableRaster");
        check(hasMethod(c, "getParent"), "method getParent");
        check(hasMethod(c, "getSampleModelTranslateX"), "method getSampleModelTranslateX");
        check(hasMethod(c, "getSampleModelTranslateY"), "method getSampleModelTranslateY");
        check(hasMethod(c, "createCompatibleWritableRaster"), "method createCompatibleWritableRaster");
        check(hasMethod(c, "createTranslatedChild"), "method createTranslatedChild");
        check(hasMethod(c, "createChild"), "method createChild");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "getMinX"), "method getMinX");
        check(hasMethod(c, "getMinY"), "method getMinY");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getNumBands"), "method getNumBands");
        check(hasMethod(c, "getNumDataElements"), "method getNumDataElements");
        check(hasMethod(c, "getTransferType"), "method getTransferType");
        check(hasMethod(c, "getDataBuffer"), "method getDataBuffer");
        check(hasMethod(c, "getSampleModel"), "method getSampleModel");
        check(hasMethod(c, "getDataElements"), "method getDataElements");
        check(hasMethod(c, "getPixel"), "method getPixel");
        check(hasMethod(c, "getPixels"), "method getPixels");
        check(hasMethod(c, "getSample"), "method getSample");
        check(hasMethod(c, "getSampleFloat"), "method getSampleFloat");
        check(hasMethod(c, "getSampleDouble"), "method getSampleDouble");
        check(hasMethod(c, "getSamples"), "method getSamples");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
