/*
 * @test
 * @summary java.awt.image.BufferStrategy conformance: abstract class reflection.
 * @run main BufferStrategyTest
 */

public class BufferStrategyTest {
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
        Class<?> c = Class.forName("java.awt.image.BufferStrategy");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getCapabilities"), "method getCapabilities");
        check(hasMethod(c, "getDrawGraphics"), "method getDrawGraphics");
        check(hasMethod(c, "contentsLost"), "method contentsLost");
        check(hasMethod(c, "contentsRestored"), "method contentsRestored");
        check(hasMethod(c, "show"), "method show");
        check(hasMethod(c, "dispose"), "method dispose");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
