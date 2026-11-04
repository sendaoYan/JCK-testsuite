/*
 * @test
 * @summary java.awt.Image conformance: abstract class reflection.
 * @run main ImageTest
 */

public class ImageTest {
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
        Class<?> c = Class.forName("java.awt.Image");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getSource"), "method getSource");
        check(hasMethod(c, "getGraphics"), "method getGraphics");
        check(hasMethod(c, "getProperty"), "method getProperty");
        check(hasMethod(c, "getScaledInstance"), "method getScaledInstance");
        check(hasMethod(c, "flush"), "method flush");
        check(hasMethod(c, "getCapabilities"), "method getCapabilities");
        check(hasMethod(c, "setAccelerationPriority"), "method setAccelerationPriority");
        check(hasMethod(c, "getAccelerationPriority"), "method getAccelerationPriority");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
