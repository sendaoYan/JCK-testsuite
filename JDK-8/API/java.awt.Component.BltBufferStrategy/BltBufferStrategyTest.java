/*
 * @test
 * @summary java.awt.Component.BltBufferStrategy conformance: class reflection.
 * @run main BltBufferStrategyTest
 */

public class BltBufferStrategyTest {
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
        Class<?> c = Class.forName("java.awt.Component$BltBufferStrategy");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.BufferStrategy"), "superclass java.awt.image.BufferStrategy");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "getCapabilities"), "method getCapabilities");
        check(hasMethod(c, "getDrawGraphics"), "method getDrawGraphics");
        check(hasMethod(c, "show"), "method show");
        check(hasMethod(c, "contentsLost"), "method contentsLost");
        check(hasMethod(c, "contentsRestored"), "method contentsRestored");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
