/*
 * @test
 * @summary java.awt.ActiveEvent conformance: interface reflection.
 * @run main ActiveEventTest
 */

public class ActiveEventTest {
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
        Class<?> c = Class.forName("java.awt.ActiveEvent");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "dispatch"), "method dispatch");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        check(java.awt.ActiveEvent.class.isInterface(), "ActiveEvent is an interface");
        boolean hasDispatch = false, hasDispose = false;
        for (java.lang.reflect.Method m : java.awt.ActiveEvent.class.getMethods()) {
            if (m.getName().equals("dispatch")) hasDispatch = true;
            if (m.getName().equals("isDispatched")) hasDispose = true;
        }
        check(hasDispatch, "declares dispatch()");
        // isDispatched has a default implementation (JDK 9+); dispatch is abstract
        // a minimal implementation must run dispatch exactly when asked
        final int[] ran = new int[1];
        java.awt.ActiveEvent ev = new java.awt.ActiveEvent() {
            public void dispatch() { ran[0]++; }
        };
        check(ran[0] == 0, "not dispatched before dispatch()");
        ev.dispatch();
        check(ran[0] == 1, "dispatch() runs the action");
        ev.dispatch();
        check(ran[0] == 2, "dispatch() repeatable");
    }

}
