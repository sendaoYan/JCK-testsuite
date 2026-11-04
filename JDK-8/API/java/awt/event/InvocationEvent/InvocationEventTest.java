/*
 * @test
 * @summary java.awt.event.InvocationEvent conformance: class reflection.
 * @run main InvocationEventTest
 */

public class InvocationEventTest {
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
        Class<?> c = Class.forName("java.awt.event.InvocationEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.AWTEvent"), "superclass java.awt.AWTEvent");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.ActiveEvent"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "dispatch"), "method dispatch");
        check(hasMethod(c, "getException"), "method getException");
        check(hasMethod(c, "getThrowable"), "method getThrowable");
        check(hasMethod(c, "getWhen"), "method getWhen");
        check(hasMethod(c, "isDispatched"), "method isDispatched");
        check(hasMethod(c, "paramString"), "method paramString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
