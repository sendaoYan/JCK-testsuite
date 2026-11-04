/*
 * @test
 * @summary java.awt.dnd.DragSourceEvent conformance: class reflection.
 * @run main DragSourceEventTest
 */

public class DragSourceEventTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragSourceEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.util.EventObject"), "superclass java.util.EventObject");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getDragSourceContext"), "method getDragSourceContext");
        check(hasMethod(c, "getLocation"), "method getLocation");
        check(hasMethod(c, "getX"), "method getX");
        check(hasMethod(c, "getY"), "method getY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
