/*
 * @test
 * @summary java.awt.event.WindowFocusListener conformance: interface reflection.
 * @run main WindowFocusListenerTest
 */

public class WindowFocusListenerTest {
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
        Class<?> c = Class.forName("java.awt.event.WindowFocusListener");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.util.EventListener"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "windowGainedFocus"), "method windowGainedFocus");
        check(hasMethod(c, "windowLostFocus"), "method windowLostFocus");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
