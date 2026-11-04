/*
 * @test
 * @summary java.awt.GraphicsConfiguration conformance: abstract class reflection.
 * @run main GraphicsConfigurationTest
 */

public class GraphicsConfigurationTest {
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
        Class<?> c = Class.forName("java.awt.GraphicsConfiguration");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getDevice"), "method getDevice");
        check(hasMethod(c, "createCompatibleImage"), "method createCompatibleImage");
        check(hasMethod(c, "createCompatibleVolatileImage"), "method createCompatibleVolatileImage");
        check(hasMethod(c, "getColorModel"), "method getColorModel");
        check(hasMethod(c, "getDefaultTransform"), "method getDefaultTransform");
        check(hasMethod(c, "getNormalizingTransform"), "method getNormalizingTransform");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "getBufferCapabilities"), "method getBufferCapabilities");
        check(hasMethod(c, "getImageCapabilities"), "method getImageCapabilities");
        check(hasMethod(c, "isTranslucencyCapable"), "method isTranslucencyCapable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
