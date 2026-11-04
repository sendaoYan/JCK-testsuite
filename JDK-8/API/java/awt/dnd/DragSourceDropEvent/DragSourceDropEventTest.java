/*
 * @test
 * @summary java.awt.dnd.DragSourceDropEvent conformance: class reflection.
 * @run main DragSourceDropEventTest
 */

public class DragSourceDropEventTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragSourceDropEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.dnd.DragSourceEvent"), "superclass java.awt.dnd.DragSourceEvent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getDropSuccess"), "method getDropSuccess");
        check(hasMethod(c, "getDropAction"), "method getDropAction");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
