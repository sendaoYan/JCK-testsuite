/*
 * @test
 * @summary java.awt.GraphicsDevice conformance: abstract class reflection.
 * @run main GraphicsDeviceTest
 */

public class GraphicsDeviceTest {
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
        Class<?> c = Class.forName("java.awt.GraphicsDevice");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getType"), "method getType");
        check(hasMethod(c, "getIDstring"), "method getIDstring");
        check(hasMethod(c, "getConfigurations"), "method getConfigurations");
        check(hasMethod(c, "getDefaultConfiguration"), "method getDefaultConfiguration");
        check(hasMethod(c, "getBestConfiguration"), "method getBestConfiguration");
        check(hasMethod(c, "isFullScreenSupported"), "method isFullScreenSupported");
        check(hasMethod(c, "setFullScreenWindow"), "method setFullScreenWindow");
        check(hasMethod(c, "getFullScreenWindow"), "method getFullScreenWindow");
        check(hasMethod(c, "isDisplayChangeSupported"), "method isDisplayChangeSupported");
        check(hasMethod(c, "setDisplayMode"), "method setDisplayMode");
        check(hasMethod(c, "getDisplayMode"), "method getDisplayMode");
        check(hasMethod(c, "getDisplayModes"), "method getDisplayModes");
        check(hasMethod(c, "getAvailableAcceleratedMemory"), "method getAvailableAcceleratedMemory");
        check(hasMethod(c, "isWindowTranslucencySupported"), "method isWindowTranslucencySupported");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
