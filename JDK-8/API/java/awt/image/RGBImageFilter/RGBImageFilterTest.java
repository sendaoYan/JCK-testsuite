/*
 * @test
 * @summary java.awt.image.RGBImageFilter conformance: abstract class reflection.
 * @run main RGBImageFilterTest
 */

public class RGBImageFilterTest {
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
        Class<?> c = Class.forName("java.awt.image.RGBImageFilter");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.awt.image.ImageFilter"), "superclass java.awt.image.ImageFilter");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setColorModel"), "method setColorModel");
        check(hasMethod(c, "substituteColorModel"), "method substituteColorModel");
        check(hasMethod(c, "filterIndexColorModel"), "method filterIndexColorModel");
        check(hasMethod(c, "filterRGBPixels"), "method filterRGBPixels");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "filterRGB"), "method filterRGB");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
