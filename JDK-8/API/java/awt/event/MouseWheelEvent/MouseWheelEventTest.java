/*
 * @test
 * @summary java.awt.event.MouseWheelEvent conformance: class reflection.
 * @run main MouseWheelEventTest
 */

public class MouseWheelEventTest {
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
        Class<?> c = Class.forName("java.awt.event.MouseWheelEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.event.MouseEvent"), "superclass java.awt.event.MouseEvent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getScrollType"), "method getScrollType");
        check(hasMethod(c, "getScrollAmount"), "method getScrollAmount");
        check(hasMethod(c, "getWheelRotation"), "method getWheelRotation");
        check(hasMethod(c, "getPreciseWheelRotation"), "method getPreciseWheelRotation");
        check(hasMethod(c, "getUnitsToScroll"), "method getUnitsToScroll");
        check(hasMethod(c, "paramString"), "method paramString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
