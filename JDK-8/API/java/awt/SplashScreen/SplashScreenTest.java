/*
 * @test
 * @summary java.awt.SplashScreen conformance: class reflection.
 * @run main SplashScreenTest
 */

public class SplashScreenTest {
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
        Class<?> c = Class.forName("java.awt.SplashScreen");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getSplashScreen"), "method getSplashScreen");
        check(hasMethod(c, "setImageURL"), "method setImageURL");
        check(hasMethod(c, "getImageURL"), "method getImageURL");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "createGraphics"), "method createGraphics");
        check(hasMethod(c, "update"), "method update");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "isVisible"), "method isVisible");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
