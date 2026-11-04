/*
 * @test
 * @summary java.awt.EventQueue conformance: class reflection.
 * @run main EventQueueTest
 */

public class EventQueueTest {
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
        Class<?> c = Class.forName("java.awt.EventQueue");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "postEvent"), "method postEvent");
        check(hasMethod(c, "getNextEvent"), "method getNextEvent");
        check(hasMethod(c, "peekEvent"), "method peekEvent");
        check(hasMethod(c, "getMostRecentEventTime"), "method getMostRecentEventTime");
        check(hasMethod(c, "getCurrentEvent"), "method getCurrentEvent");
        check(hasMethod(c, "push"), "method push");
        check(hasMethod(c, "createSecondaryLoop"), "method createSecondaryLoop");
        check(hasMethod(c, "isDispatchThread"), "method isDispatchThread");
        check(hasMethod(c, "invokeLater"), "method invokeLater");
        check(hasMethod(c, "invokeAndWait"), "method invokeAndWait");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
