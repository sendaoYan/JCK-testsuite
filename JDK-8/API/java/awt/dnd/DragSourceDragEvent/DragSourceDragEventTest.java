/*
 * @test
 * @summary java.awt.dnd.DragSourceDragEvent conformance: class reflection.
 * @run main DragSourceDragEventTest
 */

public class DragSourceDragEventTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragSourceDragEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.dnd.DragSourceEvent"), "superclass java.awt.dnd.DragSourceEvent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getTargetActions"), "method getTargetActions");
        check(hasMethod(c, "getGestureModifiers"), "method getGestureModifiers");
        check(hasMethod(c, "getGestureModifiersEx"), "method getGestureModifiersEx");
        check(hasMethod(c, "getUserAction"), "method getUserAction");
        check(hasMethod(c, "getDropAction"), "method getDropAction");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
