/*
 * @test
 * @summary java.awt.event.WindowListener conformance: interface reflection.
 * @run main WindowListenerTest
 */

public class WindowListenerTest {
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
        Class<?> c = Class.forName("java.awt.event.WindowListener");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.util.EventListener"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "windowOpened"), "method windowOpened");
        check(hasMethod(c, "windowClosing"), "method windowClosing");
        check(hasMethod(c, "windowClosed"), "method windowClosed");
        check(hasMethod(c, "windowIconified"), "method windowIconified");
        check(hasMethod(c, "windowDeiconified"), "method windowDeiconified");
        check(hasMethod(c, "windowActivated"), "method windowActivated");
        check(hasMethod(c, "windowDeactivated"), "method windowDeactivated");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
