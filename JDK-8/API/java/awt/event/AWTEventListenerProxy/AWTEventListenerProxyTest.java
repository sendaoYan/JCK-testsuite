/*
 * @test
 * @summary java.awt.event.AWTEventListenerProxy conformance: class reflection.
 * @run main AWTEventListenerProxyTest
 */

public class AWTEventListenerProxyTest {
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
        Class<?> c = Class.forName("java.awt.event.AWTEventListenerProxy");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.util.EventListenerProxy"), "superclass java.util.EventListenerProxy");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.event.AWTEventListener"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "eventDispatched"), "method eventDispatched");
        check(hasMethod(c, "getEventMask"), "method getEventMask");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
