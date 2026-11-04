/*
 * @test
 * @summary java.awt.GraphicsEnvironment conformance: abstract class reflection.
 * @run main GraphicsEnvironmentTest
 */

public class GraphicsEnvironmentTest {
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
        Class<?> c = Class.forName("java.awt.GraphicsEnvironment");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getLocalGraphicsEnvironment"), "method getLocalGraphicsEnvironment");
        check(hasMethod(c, "isHeadless"), "method isHeadless");
        check(hasMethod(c, "isHeadlessInstance"), "method isHeadlessInstance");
        check(hasMethod(c, "getScreenDevices"), "method getScreenDevices");
        check(hasMethod(c, "getDefaultScreenDevice"), "method getDefaultScreenDevice");
        check(hasMethod(c, "createGraphics"), "method createGraphics");
        check(hasMethod(c, "getAllFonts"), "method getAllFonts");
        check(hasMethod(c, "getAvailableFontFamilyNames"), "method getAvailableFontFamilyNames");
        check(hasMethod(c, "registerFont"), "method registerFont");
        check(hasMethod(c, "preferLocaleFonts"), "method preferLocaleFonts");
        check(hasMethod(c, "preferProportionalFonts"), "method preferProportionalFonts");
        check(hasMethod(c, "getCenterPoint"), "method getCenterPoint");
        check(hasMethod(c, "getMaximumWindowBounds"), "method getMaximumWindowBounds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
