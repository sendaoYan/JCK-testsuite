/*
 * @test
 * @summary java.awt.dnd.DragGestureEvent conformance: class reflection.
 * @run main DragGestureEventTest
 */

public class DragGestureEventTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DragGestureEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.util.EventObject"), "superclass java.util.EventObject");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getSourceAsDragGestureRecognizer"), "method getSourceAsDragGestureRecognizer");
        check(hasMethod(c, "getComponent"), "method getComponent");
        check(hasMethod(c, "getDragSource"), "method getDragSource");
        check(hasMethod(c, "getDragOrigin"), "method getDragOrigin");
        check(hasMethod(c, "iterator"), "method iterator");
        check(hasMethod(c, "toArray"), "method toArray");
        check(hasMethod(c, "getDragAction"), "method getDragAction");
        check(hasMethod(c, "getTriggerEvent"), "method getTriggerEvent");
        check(hasMethod(c, "startDrag"), "method startDrag");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
