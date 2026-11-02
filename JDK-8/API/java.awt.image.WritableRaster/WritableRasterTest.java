/*
 * @test
 * @summary java.awt.image.WritableRaster conformance: class reflection.
 * @run main WritableRasterTest
 */

public class WritableRasterTest {
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
        Class<?> c = Class.forName("java.awt.image.WritableRaster");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.Raster"), "superclass java.awt.image.Raster");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getWritableParent"), "method getWritableParent");
        check(hasMethod(c, "createWritableTranslatedChild"), "method createWritableTranslatedChild");
        check(hasMethod(c, "createWritableChild"), "method createWritableChild");
        check(hasMethod(c, "setDataElements"), "method setDataElements");
        check(hasMethod(c, "setRect"), "method setRect");
        check(hasMethod(c, "setPixel"), "method setPixel");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "setSample"), "method setSample");
        check(hasMethod(c, "setSamples"), "method setSamples");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
