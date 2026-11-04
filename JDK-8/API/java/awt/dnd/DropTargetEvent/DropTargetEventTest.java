/*
 * @test
 * @summary java.awt.dnd.DropTargetEvent conformance: class reflection.
 * @run main DropTargetEventTest
 */

public class DropTargetEventTest {
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
        Class<?> c = Class.forName("java.awt.dnd.DropTargetEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.util.EventObject"), "superclass java.util.EventObject");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getDropTargetContext"), "method getDropTargetContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
